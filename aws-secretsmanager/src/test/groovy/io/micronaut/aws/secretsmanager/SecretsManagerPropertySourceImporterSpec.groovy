package io.micronaut.aws.secretsmanager

import io.micronaut.context.ApplicationContext
import io.micronaut.context.annotation.Primary
import io.micronaut.context.annotation.Requires
import io.micronaut.context.env.Environment
import io.micronaut.context.env.PropertySource
import io.micronaut.context.env.PropertySourceImporter
import io.micronaut.context.exceptions.ConfigurationException
import io.micronaut.aws.secretsmanager.imports.SecretsManagerPropertySourceImporter
import io.micronaut.discovery.config.RetryablePropertySourceImporter
import io.micronaut.retry.RetryOperations
import io.micronaut.retry.RetryOperationsFactory
import io.micronaut.retry.RetryPolicy
import jakarta.inject.Singleton
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse
import software.amazon.awssdk.services.secretsmanager.model.ListSecretsRequest
import software.amazon.awssdk.services.secretsmanager.model.ListSecretsResponse
import software.amazon.awssdk.services.secretsmanager.model.SecretListEntry
import spock.lang.Specification

import java.time.Duration

class SecretsManagerPropertySourceImporterSpec extends Specification {

    void "secrets manager importer supports scalar and structured declarations with auth options"() {
        given:
        ApplicationContext context = ApplicationContext.run(['spec.name': 'SecretsManagerPropertySourceImporterSpec'])
        SecretsManagerPropertySourceImporter importer = new SecretsManagerPropertySourceImporter()

        when:
        def scalar = importer.newImportDeclaration(io.micronaut.core.util.ConnectionString.parse('aws-secretsmanager://AKIA123:SECRET456@localhost/config/myapp_dev?aws.region=eu-west-1'))
        def structured = importer.newImportDeclaration(io.micronaut.core.convert.value.ConvertibleValues.of([
            path: '/config/myapp_dev',
            'aws.access-key-id': 'AKIA123',
            'aws.secret-access-key': 'SECRET456',
            'aws.region': 'eu-west-1',
            secrets: [[ 'secret-name': 'oauthcompanyauthserver', prefix: 'datasources.default' ]]
        ]))

        then:
        scalar.declaration().path() == '/config/myapp_dev'
        scalar.declaration().providerProperties()['aws.access-key-id'] == 'AKIA123'
        scalar.declaration().providerProperties()['aws.secret-access-key'] == 'SECRET456'
        scalar.declaration().providerProperties()['aws.region'] == 'eu-west-1'
        structured.declaration().path() == '/config/myapp_dev'
        structured.declaration().providerProperties()['aws.access-key-id'] == 'AKIA123'
        structured.declaration().providerProperties()['aws.secret-access-key'] == 'SECRET456'
        structured.declaration().providerProperties()['aws.region'] == 'eu-west-1'
        structured.declaration().providerProperties()['aws.secretsmanager.secrets'] instanceof List

        cleanup:
        context.close()
    }

    void "secrets manager importer accepts documented structured aliases"() {
        given:
        ApplicationContext context = ApplicationContext.run(['spec.name': 'SecretsManagerPropertySourceImporterSpec'])
        SecretsManagerPropertySourceImporter importer = new SecretsManagerPropertySourceImporter()

        when:
        def structured = importer.newImportDeclaration(io.micronaut.core.convert.value.ConvertibleValues.of([
            path: 'config/myapp_dev',
            'access-key-id': 'AKIA123',
            'secret-access-key': 'SECRET456',
            'secret-key': 'SECRET789',
            'session-token': 'TOKEN123',
            region: 'eu-west-1',
            secrets: [[ 'secret-name': 'oauthcompanyauthserver', prefix: 'datasources.default' ]]
        ]))

        then:
        structured.declaration().path() == '/config/myapp_dev'
        structured.declaration().providerProperties()['aws.access-key-id'] == 'AKIA123'
        structured.declaration().providerProperties()['aws.secret-access-key'] == 'SECRET456'
        structured.declaration().providerProperties()['aws.secret-key'] == 'SECRET789'
        structured.declaration().providerProperties()['aws.session-token'] == 'TOKEN123'
        structured.declaration().providerProperties()['aws.region'] == 'eu-west-1'
        structured.declaration().providerProperties()['aws.secretsmanager.secrets'] instanceof List

        cleanup:
        context.close()
    }

    void "secrets manager importer rejects blank paths and unsupported query parameters"() {
        given:
        ApplicationContext context = ApplicationContext.run(['spec.name': 'SecretsManagerPropertySourceImporterSpec'])
        SecretsManagerPropertySourceImporter importer = new SecretsManagerPropertySourceImporter()

        when:
        importer.newImportDeclaration(io.micronaut.core.convert.value.ConvertibleValues.of([:]))

        then:
        thrown(ConfigurationException)

        when:
        importer.newImportDeclaration(io.micronaut.core.util.ConnectionString.parse('aws-secretsmanager:///config/myapp_dev?foo=bar'))

        then:
        thrown(ConfigurationException)

        cleanup:
        context.close()
    }


    void "secrets manager importer applies standard retry declarations"() {
        given:
        ApplicationContext context = ApplicationContext.run([
            'spec.name': 'SecretsManagerPropertySourceImporterSpec',
            'aws.secretsmanager.enabled': true,
            'aws.secretsmanager.secrets': [
                ['secret-name': 'oauthcompanyauthserver', 'prefix': 'datasources.default']
            ]
        ], Environment.TEST)
        RecordingRetryOperationsFactory retryOperationsFactory = new RecordingRetryOperationsFactory()
        SecretsManagerPropertySourceImporter importer = new SecretsManagerPropertySourceImporter(
            retryOperationsFactory,
            { environment, providerProperties -> ApplicationContext.run([
                'spec.name': 'SecretsManagerPropertySourceImporterSpec',
                'aws.secretsmanager.enabled': true,
                'aws.secretsmanager.secrets': [
                    ['secret-name': 'oauthcompanyauthserver', 'prefix': 'datasources.default']
                ]
            ] + providerProperties, Environment.TEST) },
            new SecretsManagerPropertySourceImporter.SecretsManagerImportSupport()
        )

        when:
        def declaration = importer.newImportDeclaration(io.micronaut.core.util.ConnectionString.parse('aws-secretsmanager:///config/myapp_dev?retry-attempts=3&retry-delay=25ms'))
        Optional<PropertySource> imported = importer.importPropertySource(new TestImportContext(context.environment, declaration))

        then:
        declaration.declaration().path() == '/config/myapp_dev'
        declaration.retryPolicy().maxAttempts() == 3
        declaration.retryPolicy().delay() == Duration.ofMillis(25)
        retryOperationsFactory.lastPolicy.maxAttempts() == 3
        retryOperationsFactory.lastPolicy.delay() == Duration.ofMillis(25)
        imported.present

        cleanup:
        context.close()
    }

    void "secrets manager importer loads explicit path with grouped prefixes and optional imports end to end"() {
        given:
        ApplicationContext context = ApplicationContext.run([
            'spec.name': 'SecretsManagerPropertySourceImporterSpec',
            'aws.secretsmanager.enabled': true,
            'aws.secretsmanager.secrets': [
                ['secret-name': 'oauthcompanyauthserver', 'prefix': 'datasources.default']
            ]
        ], Environment.TEST)
        SecretsManagerPropertySourceImporter importer = new SecretsManagerPropertySourceImporter(
            { environment, providerProperties -> ApplicationContext.run([
                'spec.name': 'SecretsManagerPropertySourceImporterSpec',
                'aws.secretsmanager.enabled': true,
                'aws.secretsmanager.secrets': [
                    ['secret-name': 'oauthcompanyauthserver', 'prefix': 'datasources.default']
                ]
            ] + providerProperties, Environment.TEST) },
            new SecretsManagerPropertySourceImporter.SecretsManagerImportSupport()
        )
        TestImportContext requiredContext = new TestImportContext(context.environment, new RetryablePropertySourceImporter.RetryableImportDeclaration<>(new SecretsManagerPropertySourceImporter.SecretsManagerImport('/config/myapp_dev', false, [:]), RetryPolicy.builder().build()))
        TestImportContext optionalContext = new TestImportContext(context.environment, new RetryablePropertySourceImporter.RetryableImportDeclaration<>(new SecretsManagerPropertySourceImporter.SecretsManagerImport('/config/does-not-exist', true, [:]), RetryPolicy.builder().build()))

        when:
        Optional<PropertySource> required = importer.importPropertySource(requiredContext)
        Optional<PropertySource> optional = importer.importPropertySource(optionalContext)

        then:
        required.present
        required.get().order == io.micronaut.context.env.EnvironmentPropertySource.POSITION + 300
        required.get().get('datasources.default.micronaut.security.oauth2.clients.companyauthserver.client-id') == 'XXX'
        required.get().get('datasources.default.micronaut.security.oauth2.clients.companyauthserver.client-secret') == 'YYY'
        !optional.present

        cleanup:
        context.close()
    }

    @Requires(property = 'spec.name', value = 'SecretsManagerPropertySourceImporterSpec')
    @Primary
    @Singleton
    static class MockSecretsClient implements SecretsManagerClient {
        @Override
        String serviceName() {
            'secrets manager'
        }

        @Override
        void close() {
        }

        @Override
        ListSecretsResponse listSecrets(ListSecretsRequest listSecretsRequest) {
            if (listSecretsRequest.nextToken() == null && listSecretsRequest.filters().first().values().contains('/config/myapp_dev')) {
                return ListSecretsResponse.builder()
                    .secretList(SecretListEntry.builder().name('/config/myapp_dev/oauthcompanyauthserver').build())
                    .build()
            }
            return ListSecretsResponse.builder().secretList([]).build()
        }

        @Override
        GetSecretValueResponse getSecretValue(GetSecretValueRequest getSecretValueRequest) {
            if (getSecretValueRequest.secretId() == '/config/myapp_dev/oauthcompanyauthserver') {
                return GetSecretValueResponse.builder()
                    .secretString('{"micronaut.security.oauth2.clients.companyauthserver.client-id":"XXX","micronaut.security.oauth2.clients.companyauthserver.client-secret":"YYY"}')
                    .build()
            }
            throw new UnsupportedOperationException()
        }
    }

    static final class RecordingRetryOperationsFactory implements RetryOperationsFactory {
        RetryPolicy lastPolicy

        @Override
        RetryOperations createRetryOperations(RetryPolicy retryPolicy) {
            lastPolicy = retryPolicy
            return new RetryOperations() {
                @Override
                def <T> T execute(java.util.function.Supplier<T> supplier) {
                    supplier.get()
                }

                @Override
                def <T> java.util.concurrent.CompletionStage<T> executeCompletionStage(java.util.function.Supplier<? extends java.util.concurrent.CompletionStage<T>> supplier) {
                    supplier.get()
                }

                @Override
                def <T> org.reactivestreams.Publisher<T> executePublisher(java.util.function.Supplier<? extends org.reactivestreams.Publisher<T>> supplier) {
                    supplier.get()
                }
            }
        }
    }

    static final class TestImportContext implements PropertySourceImporter.ImportContext<RetryablePropertySourceImporter.RetryableImportDeclaration<SecretsManagerPropertySourceImporter.SecretsManagerImport>> {
        private final Environment environment
        private final RetryablePropertySourceImporter.RetryableImportDeclaration<SecretsManagerPropertySourceImporter.SecretsManagerImport> declaration

        TestImportContext(Environment environment, RetryablePropertySourceImporter.RetryableImportDeclaration<SecretsManagerPropertySourceImporter.SecretsManagerImport> declaration) {
            this.environment = environment
            this.declaration = declaration
        }

        @Override
        Environment environment() {
            environment
        }

        @Override
        io.micronaut.core.util.ConnectionString connectionString() {
            io.micronaut.core.util.ConnectionString.parse("aws-secretsmanager://${declaration.declaration().path()}")
        }

        @Override
        RetryablePropertySourceImporter.RetryableImportDeclaration<SecretsManagerPropertySourceImporter.SecretsManagerImport> importDeclaration() {
            declaration
        }

        @Override
        PropertySource.Origin parentOrigin() {
            PropertySource.Origin.of('test')
        }

        @Override
        Optional<PropertySource> importPropertySource(io.micronaut.core.io.ResourceLoader resourceLoader, String path, String extension, PropertySource.Origin origin) {
            Optional.empty()
        }

        @Override
        Optional<PropertySource> importPropertySource(String path, String extension, String format, PropertySource.Origin origin) {
            Optional.empty()
        }

        @Override
        Optional<PropertySource> importClasspathPropertySource(String path, String extension, PropertySource.Origin origin, boolean optional) {
            Optional.empty()
        }
    }
}
