package io.micronaut.discovery.aws.parameterstore

import io.micronaut.context.ApplicationContext
import io.micronaut.context.annotation.Primary
import io.micronaut.context.annotation.Requires
import io.micronaut.context.env.Environment
import io.micronaut.context.env.PropertySource
import io.micronaut.context.env.PropertySourceImporter
import io.micronaut.context.exceptions.ConfigurationException
import io.micronaut.discovery.config.RetryablePropertySourceImporter
import io.micronaut.discovery.aws.parameterstore.imports.ParameterStorePropertySourceImporter
import io.micronaut.retry.RetryOperations
import io.micronaut.retry.RetryOperationsFactory
import io.micronaut.retry.RetryPolicy
import jakarta.inject.Singleton
import software.amazon.awssdk.services.ssm.SsmAsyncClient
import software.amazon.awssdk.services.ssm.model.GetParametersByPathRequest
import software.amazon.awssdk.services.ssm.model.GetParametersByPathResponse
import software.amazon.awssdk.services.ssm.model.GetParametersRequest
import software.amazon.awssdk.services.ssm.model.GetParametersResponse
import software.amazon.awssdk.services.ssm.model.Parameter
import spock.lang.Specification

import java.time.Duration
import java.util.concurrent.CompletableFuture

class AWSParameterStorePropertySourceImporterSpec extends Specification {

    private static String previousAwsRegion

    def setupSpec() {
        previousAwsRegion = System.getProperty('aws.region')
        System.setProperty('aws.region', 'us-west-1')
    }

    def cleanupSpec() {
        if (previousAwsRegion == null) {
            System.clearProperty('aws.region')
        } else {
            System.setProperty('aws.region', previousAwsRegion)
        }
    }
    void "parameter store importer supports scalar and structured declarations"() {
        given:
        ApplicationContext context = ApplicationContext.run([
            'spec.name': 'AWSParameterStorePropertySourceImporterSpec'
        ])
        ParameterStorePropertySourceImporter importer = new ParameterStorePropertySourceImporter()

        when:
        def scalar = importer.newImportDeclaration(io.micronaut.core.util.ConnectionString.parse('parameterstore:///config/application_test'))
        def structured = importer.newImportDeclaration(io.micronaut.core.convert.value.ConvertibleValues.of([path: '/config/application_test']))

        then:
        scalar.declaration().path() == '/config/application_test'
        !scalar.declaration().optional()
        structured.declaration().path() == '/config/application_test'
        !structured.declaration().optional()

        cleanup:
        context.close()
    }

    void "parameter store importer accepts documented structured aliases"() {
        given:
        ApplicationContext context = ApplicationContext.run([
            'spec.name': 'AWSParameterStorePropertySourceImporterSpec'
        ])
        ParameterStorePropertySourceImporter importer = new ParameterStorePropertySourceImporter()

        when:
        def structured = importer.newImportDeclaration(io.micronaut.core.convert.value.ConvertibleValues.of([
            path: 'config/application',
            'access-key-id': 'AKIA123',
            'secret-access-key': 'SECRET456',
            'secret-key': 'SECRET789',
            'session-token': 'TOKEN123',
            region: 'eu-west-1',
            'use-secure-parameters': true,
            'search-active-environments': false
        ]))

        then:
        structured.declaration().path() == '/config/application'
        structured.declaration().providerProperties()['aws.access-key-id'] == 'AKIA123'
        structured.declaration().providerProperties()['aws.secret-access-key'] == 'SECRET456'
        structured.declaration().providerProperties()['aws.secret-key'] == 'SECRET789'
        structured.declaration().providerProperties()['aws.session-token'] == 'TOKEN123'
        structured.declaration().providerProperties()['aws.region'] == 'eu-west-1'
        structured.declaration().providerProperties()['aws.client.system-manager.parameterstore.use-secure-parameters'] == true
        structured.declaration().providerProperties()['aws.client.system-manager.parameterstore.search-active-environments'] == false

        cleanup:
        context.close()
    }

    void "parameter store importer rejects blank paths and unknown query parameters"() {
        given:
        ApplicationContext context = ApplicationContext.run([
            'spec.name': 'AWSParameterStorePropertySourceImporterSpec'
        ])
        ParameterStorePropertySourceImporter importer = new ParameterStorePropertySourceImporter()

        when:
        importer.newImportDeclaration(io.micronaut.core.convert.value.ConvertibleValues.of([:]))

        then:
        thrown(ConfigurationException)

        when:
        importer.newImportDeclaration(io.micronaut.core.util.ConnectionString.parse('parameterstore:///config/application?foo=bar'))

        then:
        thrown(ConfigurationException)

        cleanup:
        context.close()
    }

    void "parameter store importer applies standard retry declarations"() {
        given:
        ApplicationContext context = ApplicationContext.run([
            'spec.name': 'AWSParameterStorePropertySourceImporterSpec',
            'micronaut.application.name': 'amazonTest'
        ], Environment.AMAZON_EC2)
        RecordingRetryOperationsFactory retryOperationsFactory = new RecordingRetryOperationsFactory()
        ParameterStorePropertySourceImporter importer = new ParameterStorePropertySourceImporter(
            retryOperationsFactory,
            { environment, providerProperties -> ApplicationContext.run([
                'spec.name': 'AWSParameterStorePropertySourceImporterSpec',
                'micronaut.application.name': environment.getProperty('micronaut.application.name', String).orElse('amazonTest'),
                'aws.client.system-manager.parameterstore.enabled': true,
                'aws.system-manager.parameterstore.useSecureParameters': false
            ] + providerProperties, Environment.AMAZON_EC2) },
            new ParameterStorePropertySourceImporter.ParameterStoreImportSupport()
        )

        when:
        def declaration = importer.newImportDeclaration(io.micronaut.core.util.ConnectionString.parse('parameterstore:///config/application?retry-attempts=4&retry-delay=10ms'))
        Optional<PropertySource> imported = importer.importPropertySource(new TestImportContext(context.environment, declaration))

        then:
        declaration.declaration().path() == '/config/application'
        declaration.retryPolicy().maxAttempts() == 4
        declaration.retryPolicy().delay() == Duration.ofMillis(10)
        retryOperationsFactory.lastPolicy.maxAttempts() == 4
        retryOperationsFactory.lastPolicy.delay() == Duration.ofMillis(10)
        imported.present

        cleanup:
        context.close()
    }

    void "parameter store importer loads explicit path and respects optional imports"() {
        given:
        ApplicationContext context = ApplicationContext.run([
            'spec.name': 'AWSParameterStorePropertySourceImporterSpec',
            'micronaut.application.name': 'amazonTest'
        ], Environment.AMAZON_EC2)
        ParameterStorePropertySourceImporter importer = new ParameterStorePropertySourceImporter(
            { environment, providerProperties -> ApplicationContext.run([
                'spec.name': 'AWSParameterStorePropertySourceImporterSpec',
                'micronaut.application.name': environment.getProperty('micronaut.application.name', String).orElse('amazonTest'),
                'aws.client.system-manager.parameterstore.enabled': true,
                'aws.system-manager.parameterstore.useSecureParameters': false
            ] + providerProperties, Environment.AMAZON_EC2) },
            new ParameterStorePropertySourceImporter.ParameterStoreImportSupport()
        )
        TestImportContext requiredContext = new TestImportContext(context.environment, new RetryablePropertySourceImporter.RetryableImportDeclaration<>(new ParameterStorePropertySourceImporter.ParameterStoreImport('/config/application', false, [:]), RetryPolicy.builder().build()))
        TestImportContext optionalContext = new TestImportContext(context.environment, new RetryablePropertySourceImporter.RetryableImportDeclaration<>(new ParameterStorePropertySourceImporter.ParameterStoreImport('/config/does-not-exist', true, [:]), RetryPolicy.builder().build()))

        when:
        Optional<PropertySource> required = importer.importPropertySource(requiredContext)
        Optional<PropertySource> optional = importer.importPropertySource(optionalContext)

        then:
        required.present
        required.get().name.contains('/config/application')
        required.get().order == io.micronaut.context.env.EnvironmentPropertySource.POSITION + 300
        required.get().get('datasource.url') == 'mysql://blah'
        required.get().get('parameter-1') == 'parameter-value-1'
        !optional.present

        cleanup:
        context.close()
    }

    @Requires(property = 'spec.name', value = 'AWSParameterStorePropertySourceImporterSpec')
    @Primary
    @Singleton
    static class MockSsmAsyncClient implements SsmAsyncClient {
        @Override
        CompletableFuture<GetParametersResponse> getParameters(GetParametersRequest getParametersRequest) {
            def parameters = []
            if (getParametersRequest.names().contains('/config/application')) {
                parameters << Parameter.builder().name('/config/application/datasource/url').value('mysql://blah').type('String').build()
            }
            if (getParametersRequest.names().contains('/config/application_test')) {
                parameters << Parameter.builder().name('/config/application_test/foo').value('bar').type('String').build()
            }
            return CompletableFuture.completedFuture(GetParametersResponse.builder().parameters(parameters).build())
        }

        @Override
        CompletableFuture<GetParametersByPathResponse> getParametersByPath(GetParametersByPathRequest getParametersByPathRequest) {
            def parameters = []
            if (getParametersByPathRequest.path() == '/config/application' && getParametersByPathRequest.nextToken() == null) {
                (1..10).each {
                    parameters << Parameter.builder().name("/config/application/parameter-${it}").value("parameter-value-${it}").type('String').build()
                }
                return CompletableFuture.completedFuture(GetParametersByPathResponse.builder().parameters(parameters).nextToken('nextPage').build())
            }
            if (getParametersByPathRequest.path() == '/config/application' && getParametersByPathRequest.nextToken() != null) {
                (11..20).each {
                    parameters << Parameter.builder().name("/config/application/parameter-${it}").value("parameter-value-${it}").type('String').build()
                }
            }
            return CompletableFuture.completedFuture(GetParametersByPathResponse.builder().parameters(parameters).build())
        }

        @Override
        String serviceName() {
            'ssm'
        }

        @Override
        void close() {
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

    static final class TestImportContext implements PropertySourceImporter.ImportContext<RetryablePropertySourceImporter.RetryableImportDeclaration<ParameterStorePropertySourceImporter.ParameterStoreImport>> {
        private final Environment environment
        private final RetryablePropertySourceImporter.RetryableImportDeclaration<ParameterStorePropertySourceImporter.ParameterStoreImport> declaration

        TestImportContext(Environment environment, RetryablePropertySourceImporter.RetryableImportDeclaration<ParameterStorePropertySourceImporter.ParameterStoreImport> declaration) {
            this.environment = environment
            this.declaration = declaration
        }

        @Override
        Environment environment() {
            environment
        }

        @Override
        io.micronaut.core.util.ConnectionString connectionString() {
            io.micronaut.core.util.ConnectionString.parse("parameterstore://${declaration.declaration().path()}")
        }

        @Override
        RetryablePropertySourceImporter.RetryableImportDeclaration<ParameterStorePropertySourceImporter.ParameterStoreImport> importDeclaration() {
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
