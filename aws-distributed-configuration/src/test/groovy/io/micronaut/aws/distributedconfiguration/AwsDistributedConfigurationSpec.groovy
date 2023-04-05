package io.micronaut.aws.distributedconfiguration

import io.micronaut.context.ApplicationContext
import io.micronaut.context.annotation.BootstrapContextCompatible
import io.micronaut.context.annotation.Requires
import io.micronaut.context.env.Environment
import io.micronaut.core.annotation.NonNull
import io.micronaut.core.util.StringUtils
import io.micronaut.runtime.ApplicationConfiguration
import io.micronaut.runtime.server.EmbeddedServer
import spock.lang.Specification
import spock.util.environment.RestoreSystemProperties

import jakarta.inject.Singleton

class AwsDistributedConfigurationSpec extends Specification {

    void "AwsDistributedConfiguration values are loaded from configuration"() {
        given:
        Map<String, Object> properties = [
                'spec.name': 'AwsDistributedConfigurationSpec',
                'aws.distributed-configuration.delimiter': '-',
                'aws.distributed-configuration.prefix': 'foo',
                'aws.distributed-configuration.prefixes': ['foo', 'bar'],
                'aws.distributed-configuration.common-application-name': 'bar',
        ]
        when:
        ApplicationContext context = ApplicationContext.run(properties)
        AwsDistributedConfiguration awsDistributedConfiguration = context.getBean(AwsDistributedConfiguration)

        then:
        awsDistributedConfiguration.delimiter == '-'
        awsDistributedConfiguration.prefix == 'foo'
        awsDistributedConfiguration.prefixes == ['foo', 'bar']
        awsDistributedConfiguration.commonApplicationName == 'bar'

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
        awsDistributedConfiguration.prefix == '/config/'
        awsDistributedConfiguration.prefixes == []
        awsDistributedConfiguration.commonApplicationName == 'application'

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

    @RestoreSystemProperties
    void "multiple prefixes are used to read from secrets" (String appName,
                                                            String env,
                                                            List<String> prefixes,
                                                            String expectedSecret,
                                                            String expectedToken){
        given:
        System.setProperty(Environment.BOOTSTRAP_CONTEXT_PROPERTY, StringUtils.TRUE)
        Map<String, Object> properties = [
                'spec.name': 'AwsDistributedConfigurationSpec',
                'micronaut.application.name': appName,
                'micronaut.config-client.enabled': true,
                'aws.distributed-configuration.prefixes': prefixes,
                'aws.distributed-configuration.delimiter': '/',
        ]

        when:
        EmbeddedServer embeddedServer = env ? ApplicationContext.run(EmbeddedServer, properties, env) : ApplicationContext.run(EmbeddedServer,  properties)
        ApplicationContext context = embeddedServer.applicationContext
        Optional<String> clientSecretOptional = context.getProperty('micronaut.security.oauth2.clients.companyauthserver.client-secret', String)
        Optional<String> clientIdOptional = context.getProperty('micronaut.security.oauth2.clients.companyauthserver.client-id', String)

        then:
        clientSecretOptional.isPresent()
        clientSecretOptional.get() == expectedSecret
        clientIdOptional.isPresent()
        clientIdOptional.get() == expectedToken

        cleanup:
        context.close()
        embeddedServer.close()

      where:
      appName    | env   | prefixes                | expectedSecret    | expectedToken
      'myapp'    | 'foo' | ['/config/', '/other/'] | 'myapp_fooYYY'    | 'myapp_fooXXX'
      'otherapp' | null  | ['/demo/']              | 'otherapp_fooYYY' | 'otherapp_fooXXX'
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
        protected String adaptPropertyKey(String originalKey, String groupName) {
            return originalKey
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
                        ],
                '/other/myapp_foo/OpenID':
                        [
                                'micronaut.security.oauth2.clients.companyauthserver.client-id': 'myapp_fooXXX'
                        ],
                '/demo/otherapp/OpenID':
                        [
                                'micronaut.security.oauth2.clients.companyauthserver.client-secret': 'otherapp_fooYYY',
                                'micronaut.security.oauth2.clients.companyauthserver.client-id': 'otherapp_fooXXX'
                        ]
        ]

        @Override
        Optional<Map<String, Map>> keyValuesByPrefix(@NonNull String prefix) {
            String k = m.keySet().find { it.startsWith(prefix) }
            (k) ? Optional.of([(k): m[k]] as Map<String, Map>) : Optional.empty()
        }
    }
}
