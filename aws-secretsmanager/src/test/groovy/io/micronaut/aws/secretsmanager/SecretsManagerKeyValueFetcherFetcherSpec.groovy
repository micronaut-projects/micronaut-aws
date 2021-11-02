package io.micronaut.aws.secretsmanager

import io.micronaut.context.annotation.BootstrapContextCompatible
import io.micronaut.context.annotation.Primary
import io.micronaut.inject.BeanDefinition
import jakarta.inject.Singleton
import software.amazon.awssdk.awscore.exception.AwsServiceException
import software.amazon.awssdk.core.exception.SdkClientException
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient
import software.amazon.awssdk.services.secretsmanager.model.*

class SecretsManagerKeyValueFetcherFetcherSpec extends ApplicationContextSpecification {

    @Override
    String getSpecName() {
        'SecretsManagerKeyValueFetcherSpec'
    }

    void "SecretsManagerKeyValueFetcher is annotated with BootstrapContextCompatible"() {
        when:
        BeanDefinition<SecretsManagerKeyValueFetcher> beanDefinition = applicationContext.getBeanDefinition(SecretsManagerKeyValueFetcher)

        then:
        beanDefinition.getAnnotationNameByStereotype(BootstrapContextCompatible).isPresent()
    }

    void "bean of type SecretsManagerKeyValueFetcher exists"() {
        when:
        SecretsManagerKeyValueFetcher secretsManagerKeyValueFetcher = applicationContext.getBean(SecretsManagerKeyValueFetcher)

        then:
        noExceptionThrown()

        when:
        Optional<Map> mapOptional = secretsManagerKeyValueFetcher.keyValuesByPrefix("/config/myapp_dev", null)

        then:
        mapOptional.isPresent()

        when:
        Map map = mapOptional.get()

        then:
        map
        map.containsKey('micronaut.security.oauth2.clients.companyauthserver.client-id')
        map['micronaut.security.oauth2.clients.companyauthserver.client-id'] == 'XXX'
        map.containsKey('micronaut.security.oauth2.clients.companyauthserver.client-secret')
        map['micronaut.security.oauth2.clients.companyauthserver.client-secret'] == 'YYY'
    }

    @Primary
    @Singleton
    static class MockSecretsClient implements SecretsManagerClient {

        @Override
        String serviceName() {
            return "secrets manager"
        }

        @Override
        void close() {

        }

        @Override
        GetSecretValueResponse getSecretValue(GetSecretValueRequest getSecretValueRequest) throws ResourceNotFoundException,
                InvalidParameterException, InvalidRequestException, DecryptionFailureException, InternalServiceErrorException,
                AwsServiceException, SdkClientException, SecretsManagerException {
            if (getSecretValueRequest.secretId() == "/config/myapp_dev") {
                return GetSecretValueResponse.builder()
                        .secretString('''\
{
  "micronaut.security.oauth2.clients.companyauthserver.client-id": "XXX",
  "micronaut.security.oauth2.clients.companyauthserver.client-secret": "YYY"
}''')
                        .build()
            }
            throw new UnsupportedOperationException();
        }
    }
}
