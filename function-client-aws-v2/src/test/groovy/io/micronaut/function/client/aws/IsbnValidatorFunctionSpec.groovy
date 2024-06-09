package io.micronaut.function.client.aws

import io.micronaut.test.extensions.spock.annotation.MicronautTest
import io.micronaut.test.support.TestPropertyProvider
import jakarta.inject.Inject
import org.testcontainers.containers.localstack.LocalStackContainer
import org.testcontainers.spock.Testcontainers
import org.testcontainers.utility.DockerImageName
import spock.lang.Shared
import spock.lang.Specification

import static org.testcontainers.containers.localstack.LocalStackContainer.Service.LAMBDA

@Testcontainers
@MicronautTest
class IsbnValidatorFunctionSpec extends Specification implements TestPropertyProvider {

    @Shared
    private LocalStackContainer localStackContainer = new LocalStackContainer(DockerImageName
            .parse("localstack/localstack:3.4.0"))
            .withServices(LAMBDA);

    @Override
    Map<String, String> getProperties() {
        return Map.of(
                "aws.accessKeyId", localStackContainer.getAccessKey(),
                "aws.secretKey", localStackContainer.getSecretKey(),
                "aws.region", localStackContainer.getRegion(),
                "aws.services.lambda.endpoint-override", localStackContainer.getEndpointOverride(LAMBDA)
        )
    }

    @Inject
    IsbnValidatorClient functionClient;

    def "foo"() {
        given:
        when:
        functionClient.validate(new IsbnValidationRequest())
        then:
        1==1
    }

}
