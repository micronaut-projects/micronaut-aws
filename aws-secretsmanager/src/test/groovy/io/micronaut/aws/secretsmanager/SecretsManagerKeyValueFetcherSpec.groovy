package io.micronaut.aws.secretsmanager

import io.micronaut.context.annotation.BootstrapContextCompatible
import io.micronaut.context.annotation.Primary
import io.micronaut.context.annotation.Requires
import io.micronaut.inject.BeanDefinition
import software.amazon.awssdk.awscore.exception.AwsServiceException
import software.amazon.awssdk.core.exception.SdkClientException
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient
import software.amazon.awssdk.services.secretsmanager.model.DecryptionFailureException
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse
import software.amazon.awssdk.services.secretsmanager.model.InternalServiceErrorException
import software.amazon.awssdk.services.secretsmanager.model.InvalidNextTokenException
import software.amazon.awssdk.services.secretsmanager.model.InvalidParameterException
import software.amazon.awssdk.services.secretsmanager.model.InvalidRequestException
import software.amazon.awssdk.services.secretsmanager.model.ListSecretsRequest
import software.amazon.awssdk.services.secretsmanager.model.ListSecretsResponse
import software.amazon.awssdk.services.secretsmanager.model.ResourceNotFoundException
import software.amazon.awssdk.services.secretsmanager.model.SecretListEntry
import software.amazon.awssdk.services.secretsmanager.model.SecretsManagerException
import javax.inject.Singleton

class SecretsManagerKeyValueFetcherSpec extends ApplicationContextSpecification {

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
        Optional<Map> mapOptional = secretsManagerKeyValueFetcher.keyValuesByPrefix("/config/myapp_dev/")

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
        map.containsKey('micronaut.security.oauth2.clients.google.client-id')
        map['micronaut.security.oauth2.clients.google.client-id'] == 'ZZZ'
        map.containsKey('micronaut.security.oauth2.clients.google.client-secret')
        map['micronaut.security.oauth2.clients.google.client-secret'] == 'PPP'
    }

    @Requires(property = 'spec.name', value = 'SecretsManagerKeyValueFetcherSpec')
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
        ListSecretsResponse listSecrets(ListSecretsRequest listSecretsRequest) throws InvalidParameterException,
                InvalidNextTokenException, InternalServiceErrorException, AwsServiceException, SdkClientException,
                SecretsManagerException {
            if (listSecretsRequest.nextToken() == null) {
                return (ListSecretsResponse) ListSecretsResponse.builder()
                        .secretList(SecretListEntry.builder()
                                .name("/config/myapp_dev/oauthcompanyauthserver")
                                .build()
                        )
                        .nextToken('foo')
                        .build()
            } else if (listSecretsRequest.nextToken() == 'foo') {
                return (ListSecretsResponse) ListSecretsResponse.builder()
                        .secretList(SecretListEntry.builder()
                                .name("/config/myapp_dev/oauthgoogle")
                                .build()
                        )
                        .nextToken(null)
                        .build()
            }

        }

        @Override
        GetSecretValueResponse getSecretValue(GetSecretValueRequest getSecretValueRequest) throws ResourceNotFoundException,
                InvalidParameterException, InvalidRequestException, DecryptionFailureException, InternalServiceErrorException,
                AwsServiceException, SdkClientException, SecretsManagerException {
            if (getSecretValueRequest.secretId() == "/config/myapp_dev/oauthcompanyauthserver") {
                return GetSecretValueResponse.builder()
                        .secretString('''\
{
  "micronaut.security.oauth2.clients.companyauthserver.client-id": "XXX",
  "micronaut.security.oauth2.clients.companyauthserver.client-secret": "YYY"
}''')
                        .build()
            } else if (getSecretValueRequest.secretId() == "/config/myapp_dev/oauthgoogle") {
                return GetSecretValueResponse.builder()
                        .secretString('''\
{
  "micronaut.security.oauth2.clients.google.client-id": "ZZZ",
  "micronaut.security.oauth2.clients.google.client-secret": "PPP"
}''')
                        .build()
            }
            throw new UnsupportedOperationException();
        }
    }
}
