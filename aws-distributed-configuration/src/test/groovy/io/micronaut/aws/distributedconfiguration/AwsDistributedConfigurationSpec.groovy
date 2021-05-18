package io.micronaut.aws.distributedconfiguration

import edu.umd.cs.findbugs.annotations.NonNull
import io.micronaut.context.ApplicationContext
import io.micronaut.context.annotation.BootstrapContextCompatible
import io.micronaut.context.annotation.Requires
import io.micronaut.context.env.Environment
import io.micronaut.core.util.StringUtils
import io.micronaut.runtime.ApplicationConfiguration
import io.micronaut.runtime.server.EmbeddedServer
import spock.lang.Specification
import spock.util.environment.RestoreSystemProperties

import javax.inject.Singleton

class AwsDistributedConfigurationSpec extends Specification {

    void "AwsDistributedConfiguration values are loaded from configuration"() {
        given:
        Map<String, Object> properties = [
                'spec.name': 'AwsDistributedConfigurationSpec',
                'aws.distributed-configuration.delimiter': '-',
                'aws.distributed-configuration.leading-delimiter': '#',
                'aws.distributed-configuration.trailing-delimiter': '*',
                'aws.distributed-configuration.prefix': 'foo',
                'aws.distributed-configuration.shared-configuration-name': 'bar',
        ]
        when:
        ApplicationContext context = ApplicationContext.run(properties)
        AwsDistributedConfiguration awsDistributedConfiguration = context.getBean(AwsDistributedConfiguration)

        then:
        awsDistributedConfiguration.delimiter == '-'
        awsDistributedConfiguration.leadingDelimiter == '#'
        awsDistributedConfiguration.trailingDelimiter == '*'
        awsDistributedConfiguration.prefix == 'foo'
        awsDistributedConfiguration.sharedConfigurationName == 'bar'

        cleanup:
        context.close()
    }

    void "default values for AwsDistributedConfiguration"() {
        given:
        Map<String, Object> properties = [
                'spec.name': 'AwsDistributedConfigurationSpec',
        ]
        when:
        ApplicationContext context = ApplicationContext.run(properties)
        AwsDistributedConfiguration awsDistributedConfiguration = context.getBean(AwsDistributedConfiguration)

        then:
        awsDistributedConfiguration.delimiter == '/'
        awsDistributedConfiguration.leadingDelimiter == '/'
        awsDistributedConfiguration.trailingDelimiter == '/'
        awsDistributedConfiguration.prefix == 'config'
        awsDistributedConfiguration.sharedConfigurationName == 'application'

        cleanup:
        context.close()
    }

    @RestoreSystemProperties
    void "configuration resolution precedence appname_env => appname => application_dev => application"(String appName,
                                                                                                         String env,
                                                                                                         String expected) {
        given:
        System.setProperty(Environment.BOOTSTRAP_CONTEXT_PROPERTY, StringUtils.TRUE)
        Map<String, Object> properties = [
                'spec.name': 'AwsDistributedConfigurationSpec',
                'micronaut.application.name': appName,
                'micronaut.config-client.enabled': true,
        ]

        when:
        EmbeddedServer embeddedServer = env ? ApplicationContext.run(EmbeddedServer, properties, env) : ApplicationContext.run(EmbeddedServer,  properties)
        ApplicationContext context = embeddedServer.applicationContext
        Optional<String> clientSecretOptional = context.getProperty('micronaut.security.oauth2.clients.companyauthserver.client-secret', String)

        then:
        clientSecretOptional.isPresent()
        clientSecretOptional.get() == expected

        cleanup:
        context.close()
        embeddedServer.close()

        where:
        appName     | env   || expected
        'myapp'     | 'foo' || 'myapp_fooYYY'
        'myapp'     | null  || 'myappYYY'
        'otherapp'  | null  || 'applicationYYY'
        'otherapp'  | 'foo' || 'application_fooYYY'
    }

    @Requires(beans = [AwsDistributedConfiguration, KeyValueFetcher])
    @Requires(property = 'spec.name', value = 'AwsDistributedConfigurationSpec')
    @BootstrapContextCompatible
    @Singleton
    static class MockAwsDistributedConfigurationClient extends AwsDistributedConfigurationClient {

        MockAwsDistributedConfigurationClient(AwsDistributedConfiguration awsDistributedConfiguration,
                                              MockKeyValuesFetcher keyValueFetcher,
                                              ApplicationConfiguration applicationConfiguration) {
            super(awsDistributedConfiguration, keyValueFetcher, applicationConfiguration)
        }

        @Override
        @NonNull
        protected String getPropertySourceName() {
             'mocksecretsmanager'
        }

        @Override
        String getDescription() {
            'mock secrets manager'
        }
    }

    @Requires(property = 'spec.name', value = 'AwsDistributedConfigurationSpec')
    @BootstrapContextCompatible
    @Singleton
    static class MockKeyValuesFetcher implements KeyValueFetcher {

        Map<String, Map<String, String>> m = [
                '/config/application/OpenID':
                        [
                                'micronaut.security.oauth2.clients.companyauthserver.client-secret': 'applicationYYY'
                        ],
                '/config/application_foo/OpenID':
                        [
                                'micronaut.security.oauth2.clients.companyauthserver.client-secret': 'application_fooYYY'
                        ],
                '/config/myapp/OpenID':
                        [
                                'micronaut.security.oauth2.clients.companyauthserver.client-secret': 'myappYYY'
                        ],
                '/config/myapp_foo/OpenID':
                        [
                                'micronaut.security.oauth2.clients.companyauthserver.client-secret': 'myapp_fooYYY'
                        ]

        ]

        @Override
        Optional<Map> keyValuesByPrefix(@NonNull String prefix) {
            String k = m.keySet().find { it.startsWith(prefix) }
            (k) ? Optional.of(m[k]) : Optional.empty()
        }
    }
}
