package io.micronaut.function.client.aws

import io.micronaut.core.io.ResourceLoader
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import io.micronaut.test.support.TestPropertyProvider
import jakarta.inject.Inject
import org.testcontainers.containers.localstack.LocalStackContainer
import org.testcontainers.spock.Testcontainers
import org.testcontainers.utility.DockerImageName
import software.amazon.awssdk.core.SdkBytes
import software.amazon.awssdk.services.lambda.LambdaClient
import software.amazon.awssdk.services.lambda.model.Architecture
import software.amazon.awssdk.services.lambda.model.CreateFunctionRequest
import software.amazon.awssdk.services.lambda.model.DeleteFunctionRequest
import software.amazon.awssdk.services.lambda.model.FunctionCode
import software.amazon.awssdk.services.lambda.model.LambdaRequest
import software.amazon.awssdk.services.lambda.model.Runtime
import spock.lang.Shared
import spock.lang.Specification

import java.nio.file.Files
import java.nio.file.Path
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

import static org.testcontainers.containers.localstack.LocalStackContainer.Service.LAMBDA

@Testcontainers
@MicronautTest
class TestFunctionSpec extends Specification implements TestPropertyProvider {

    private static final String FUNCTION_NAME = "TEST_FUNCTION_NAME"

    @Shared
    private LocalStackContainer localStackContainer = new LocalStackContainer(DockerImageName
            .parse("localstack/localstack:3.4.0"))
            .withServices(LAMBDA)

    @Inject
    @Shared
    LambdaClient lambdaClient

    @Inject
    @Shared
    ResourceLoader resourceLoader

    @Override
    Map<String, String> getProperties() {
        Map.of(
                "aws.access-key-id", localStackContainer.getAccessKey(),
                "aws.secret-key", localStackContainer.getSecretKey(),
                "aws.region", localStackContainer.getRegion(),
                "aws.services.lambda.endpoint-override", localStackContainer.getEndpointOverride(LAMBDA).toString()
        ) as Map<String, String>
    }

    @Inject
    TestFunctionClient functionClient

    def setupSpec() {
        byte[] bytes = lambdaBytes(resourceLoader)
        LambdaRequest lambdaRequest = createFunctionRequest(bytes)
        if (lambdaRequest instanceof CreateFunctionRequest) {
            lambdaClient.createFunction((CreateFunctionRequest) lambdaRequest)
        }
    }

    def cleanupSpec() {
        LambdaRequest lambdaRequest = deleteFunctionRequest()
        if (lambdaRequest instanceof DeleteFunctionRequest) {
            lambdaClient.deleteFunction((DeleteFunctionRequest) lambdaRequest)
        }
    }

    def "can invoke a JS Lambda function with the an @FunctionClient"() {
        given:
        Integer aNumber = 1
        String aString = "someString"

        when:
        TestFunctionClientResponse result = functionClient
                .invokeFunction(new TestFunctionClientRequest(aNumber, aString, new ComplexType(aNumber, aString)))

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

    private static byte[] lambdaBytes(ResourceLoader resourceLoader) {
        try (InputStream inputStream = resourceLoader.getResourceAsStream("classpath:lambda/index.js.zip").orElseThrow()) {
            byte[] fileBytes = inputStream.readAllBytes()
            Path tempFile = Files.createTempFile(FUNCTION_NAME, ".zip");
            try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(tempFile))) {
                ZipEntry zipEntry = new ZipEntry("index.js")
                zos.putNextEntry(zipEntry)
                zos.write(fileBytes)
                zos.closeEntry()
            }
            return Files.readAllBytes(tempFile);
        }
    }

    private static LambdaRequest createFunctionRequest(byte[] arr) {
        CreateFunctionRequest.builder()
                .functionName(FUNCTION_NAME)
                .code(FunctionCode.builder()
                        .zipFile(SdkBytes.fromByteArray(arr))
                        .build())
                .runtime(Runtime.NODEJS20_X)
                .architectures(Architecture.X86_64)
                .handler("index.handler")
                .build()
    }

    private static LambdaRequest deleteFunctionRequest() {
        DeleteFunctionRequest.builder()
                .functionName(FUNCTION_NAME)
                .build()
    }
}
