package io.micronaut.discovery.aws.parameterstore

import io.micronaut.context.ApplicationContext
import io.micronaut.context.env.Environment
import io.micronaut.discovery.config.RetryablePropertySourceImporter
import io.micronaut.discovery.aws.parameterstore.imports.ParameterStorePropertySourceImporter
import io.micronaut.retry.RetryPolicy
import org.slf4j.LoggerFactory
import software.amazon.awssdk.services.ssm.SsmAsyncClient
import software.amazon.awssdk.services.ssm.model.GetParametersByPathRequest
import software.amazon.awssdk.services.ssm.model.GetParametersByPathResponse
import software.amazon.awssdk.services.ssm.model.GetParametersRequest
import software.amazon.awssdk.services.ssm.model.GetParametersResponse
import software.amazon.awssdk.services.ssm.model.Parameter
import spock.lang.Specification

import java.util.concurrent.CompletableFuture

class AWSParameterStoreDeprecationAndCoexistenceSpec extends Specification {

    static {
        System.setProperty('aws.region', 'us-west-1')
    }

    void "legacy parameter store still resolves while import order wins over legacy"() {
        given:
        ApplicationContext legacyContext = ApplicationContext.run([
            'spec.name': 'AWSParameterStoreDeprecationAndCoexistenceSpec',
            'micronaut.application.name': 'amazonTest',
            'aws.client.system-manager.parameterstore.enabled': true
        ], Environment.AMAZON_EC2)
        AWSParameterStoreConfigClient legacyClient = legacyContext.getBean(AWSParameterStoreConfigClient)

        when:
        def env = Mock(Environment)
        env.getActiveNames() >> (['test'] as Set)
        def legacySources = reactor.core.publisher.Flux.from(legacyClient.getPropertySources(env)).collectList().block()

        then:
        legacySources*.name == ['route53-application', 'route53-application[test]']
        legacySources[0].get('datasource.url') == 'legacy://value'

        when:
        ApplicationContext importContext = ApplicationContext.run([
            'spec.name': 'AWSParameterStoreDeprecationAndCoexistenceSpec',
            'micronaut.application.name': 'amazonTest'
        ], Environment.AMAZON_EC2)
        ParameterStorePropertySourceImporter importer = new ParameterStorePropertySourceImporter(
            { environment, providerProperties -> ApplicationContext.run([
                'spec.name': 'AWSParameterStoreDeprecationAndCoexistenceSpec',
                'micronaut.application.name': 'amazonTest',
                'aws.client.system-manager.parameterstore.enabled': true
            ] + providerProperties, Environment.AMAZON_EC2) },
            new ParameterStorePropertySourceImporter.ParameterStoreImportSupport()
        )
        def imported = importer.importPropertySource(new AWSParameterStorePropertySourceImporterSpec.TestImportContext(importContext.environment, new RetryablePropertySourceImporter.RetryableImportDeclaration<>(new ParameterStorePropertySourceImporter.ParameterStoreImport('/config/application', false, [:]), RetryPolicy.builder().build())))

        then:
        imported.present
        imported.get().order > legacySources[0].order
        imported.get().get('datasource.url') == 'legacy://value'
        imported.get().get('imported') == 'imported-value'

        cleanup:
        legacyContext.close()
        importContext.close()
    }

    @io.micronaut.context.annotation.Requires(property = 'spec.name', value = 'AWSParameterStoreDeprecationAndCoexistenceSpec')
    @io.micronaut.context.annotation.Primary
    @jakarta.inject.Singleton
    static class MockSsmAsyncClient implements SsmAsyncClient {
        @Override
        CompletableFuture<GetParametersResponse> getParameters(GetParametersRequest getParametersRequest) {
            def parameters = []
            if (getParametersRequest.names().contains('/config/application')) {
                parameters << Parameter.builder().name('/config/application/datasource/url').value('legacy://value').type('String').build()
            }
            if (getParametersRequest.names().contains('/config/application_test')) {
                parameters << Parameter.builder().name('/config/application_test/foo').value('legacy-bar').type('String').build()
            }
            return CompletableFuture.completedFuture(GetParametersResponse.builder().parameters(parameters).build())
        }

        @Override
        CompletableFuture<GetParametersByPathResponse> getParametersByPath(GetParametersByPathRequest getParametersByPathRequest) {
            def parameters = []
            if (getParametersByPathRequest.path() == '/config/application') {
                parameters << Parameter.builder().name('/config/application/imported').value('imported-value').type('String').build()
            }
            return CompletableFuture.completedFuture(GetParametersByPathResponse.builder().parameters(parameters).build())
        }

        @Override
        String serviceName() { 'ssm' }

        @Override
        void close() { }
    }
}
