package io.micronaut.function.client.aws

import io.micronaut.core.io.ResourceLoader
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import io.micronaut.test.support.TestPropertyProvider
import jakarta.inject.Inject
import org.apache.commons.compress.archivers.ArchiveOutputStream
import org.apache.commons.compress.archivers.ArchiveStreamFactory
import org.testcontainers.containers.localstack.LocalStackContainer
import org.testcontainers.spock.Testcontainers
import org.testcontainers.utility.DockerImageName
import software.amazon.awssdk.core.SdkBytes
import software.amazon.awssdk.services.lambda.LambdaClient
import software.amazon.awssdk.services.lambda.model.Architecture
import software.amazon.awssdk.services.lambda.model.CreateFunctionRequest
import software.amazon.awssdk.services.lambda.model.DeleteFunctionRequest
import software.amazon.awssdk.services.lambda.model.FunctionCode
import software.amazon.awssdk.services.lambda.model.Runtime
import spock.lang.Shared
import spock.lang.Specification

import java.nio.file.Files

import static org.testcontainers.containers.localstack.LocalStackContainer.Service.LAMBDA

@Testcontainers
@MicronautTest
class TestFunctionSpec extends Specification implements TestPropertyProvider {

    private static final String FUNCTION_NAME = "FUNCTION_NAME";

    @Shared
    private LocalStackContainer localStackContainer = new LocalStackContainer(DockerImageName
            .parse("localstack/localstack:3.4.0"))
            .withServices(LAMBDA);

    @Inject
    @Shared
    LambdaClient lambdaClient

    @Inject
    @Shared
    ResourceLoader resourceLoader

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
    TestFunctionClient functionClient;

    def setupSpec() {
        def resource = resourceLoader.getResource("classpath:lambda").orElseThrow()
        def tempFile = File.createTempFile(UUID.randomUUID().toString(), UUID.randomUUID().toString())
        tempFile.deleteOnExit()

        def outputStream = new FileOutputStream(tempFile)

        ArchiveOutputStream archive = new ArchiveStreamFactory()
                .createArchiveOutputStream(ArchiveStreamFactory.ZIP, outputStream);

        Arrays.stream(new File(resource.toURI()).listFiles())
            .forEach { file ->
                archive.createArchiveEntry(file, file.getName())
            }

        archive.finish()

        lambdaClient.createFunction(CreateFunctionRequest.builder()
                .functionName(FUNCTION_NAME)
                .code(FunctionCode.builder()
                        .zipFile(SdkBytes.fromByteArray(Files.readAllBytes(tempFile.toPath())))
                        .build())
                .runtime(Runtime.NODEJS20_X)
                .architectures(Architecture.X86_64)
                .handler("index.handler")
                .build())
    }

    def cleanupSpec() {
        lambdaClient.deleteFunction(DeleteFunctionRequest.builder()
                .functionName(FUNCTION_NAME)
                .build())
    }

    def "foo"() {
        given:
        def aNumber = 1
        def aString = "someString"

        when:
        def result = functionClient
                .invokeFunction(new TestFunctionClientRequest(aNumber, aNumber, new ComplexType(aNumber, aString)))

        then:
        result.aNumber == aNumber
        result.aString == aString
        result.aObject
        result.aObject.aNumber == aNumber
        result.aObject.aString == aString
        result.anArray.size() == 1
        result.anArray[0].aNumber == aNumber
        result.anArray[0].aString == aString
    }

}
