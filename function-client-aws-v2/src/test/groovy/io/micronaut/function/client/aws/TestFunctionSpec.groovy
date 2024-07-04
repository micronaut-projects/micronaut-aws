package io.micronaut.function.client.aws

import io.micronaut.core.io.ResourceLoader
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import io.micronaut.test.support.TestPropertyProvider
import jakarta.inject.Inject
import org.testcontainers.containers.localstack.LocalStackContainer
import org.testcontainers.spock.Testcontainers
import org.testcontainers.utility.DockerImageName
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.AwsCredentialsProviderChain
import software.amazon.awssdk.core.SdkBytes
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.iam.IamClient
import software.amazon.awssdk.services.iam.model.AttachRolePolicyRequest
import software.amazon.awssdk.services.iam.model.CreatePolicyRequest
import software.amazon.awssdk.services.iam.model.CreateRoleRequest
import software.amazon.awssdk.services.iam.model.GetPolicyRequest
import software.amazon.awssdk.services.iam.model.GetRoleRequest
import software.amazon.awssdk.services.iam.model.Role
import software.amazon.awssdk.services.iam.waiters.IamWaiter
import software.amazon.awssdk.services.lambda.LambdaClient
import software.amazon.awssdk.services.lambda.model.Architecture
import software.amazon.awssdk.services.lambda.model.CreateFunctionRequest
import software.amazon.awssdk.services.lambda.model.DeleteFunctionRequest
import software.amazon.awssdk.services.lambda.model.FunctionCode
import software.amazon.awssdk.services.lambda.model.GetFunctionConcurrencyRequest
import software.amazon.awssdk.services.lambda.model.GetFunctionConfigurationRequest
import software.amazon.awssdk.services.lambda.model.Runtime
import software.amazon.awssdk.services.lambda.model.GetFunctionRequest
import software.amazon.awssdk.services.lambda.model.LambdaRequest
import spock.lang.Shared
import spock.lang.Specification

import java.nio.file.Files
import java.nio.file.Path
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

import static org.testcontainers.containers.localstack.LocalStackContainer.Service.IAM
import static org.testcontainers.containers.localstack.LocalStackContainer.Service.LAMBDA

@Testcontainers
@MicronautTest
class TestFunctionSpec extends Specification implements TestPropertyProvider {

    private static final String FUNCTION_NAME = "TEST_FUNCTION_NAME"

    @Shared
    private LocalStackContainer localStackContainer = new LocalStackContainer(DockerImageName
            .parse("localstack/localstack:3.4.0"))
            .withServices(IAM, LAMBDA)

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
        try {
            lambdaClient.getFunction(GetFunctionRequest.builder()
                    .functionName(FUNCTION_NAME)
                    .build())
        } catch(Exception e) {
            // Create if not exists
            byte[] bytes = lambdaBytes(resourceLoader)
            LambdaRequest lambdaRequest = createFunctionRequest(bytes)
            if (lambdaRequest instanceof CreateFunctionRequest) {
                def waiter = lambdaClient.waiter()

                def function = lambdaClient.createFunction((CreateFunctionRequest) lambdaRequest)
                waiter.waitUntilFunctionExists(GetFunctionRequest.builder()
                        .functionName(function.functionName())
                        .build())
                GetFunctionConfigurationRequest getFunctionConfigurationRequest =
                        GetFunctionConfigurationRequest.builder().functionName(function.functionName()).build()
                waiter.waitUntilFunctionActive(getFunctionConfigurationRequest)
            }
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

    private byte[] lambdaBytes(ResourceLoader resourceLoader) {
        try (InputStream inputStream = resourceLoader.getResourceAsStream("classpath:lambda/index.js").orElseThrow()) {
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

    private Role getLambdaRole() {
        def iamClient = IamClient.builder()
                .region(Region.of(localStackContainer.getRegion()))
                .credentialsProvider(AwsCredentialsProviderChain.of(
                        () -> AwsBasicCredentials.create(localStackContainer.getAccessKey(), localStackContainer.getSecretKey())
                ))
                .endpointOverride(localStackContainer.getEndpointOverride(IAM))
                .build()
        def roleName = "lambda-role";
        try {
            return iamClient.getRole(GetRoleRequest.builder()
                    .roleName(roleName)
                    .build()).role();
        } catch (final Exception e) {
            // Create if not exists
            IamWaiter iamWaiter = iamClient.waiter();

            CreatePolicyRequest request = CreatePolicyRequest.builder()
                    .policyName("lambda-invoke-policy")
                    .policyDocument("""
                    {
                      "Version": "2012-10-17",
                      "Statement": [
                        {
                          "Sid": "LambdaInvoke",
                          "Effect": "Allow",
                          "Action": [
                            "lambda:InvokeFunction"
                          ],
                          "Resource": "*"
                        }
                      ]
                    }
                    """.stripIndent())
                    .build();

            def policy = iamClient.createPolicy(request)
            iamWaiter.waitUntilPolicyExists(GetPolicyRequest.builder()
                    .policyArn(policy.policy().arn())
                    .build());

            def role = iamClient.createRole(CreateRoleRequest.builder()
                    .roleName(roleName)
                    .path("/")
                    .assumeRolePolicyDocument("""
                        {
                         "Version": "2012-10-17",
                         "Statement": [
                           {
                             "Effect": "Allow",
                             "Principal": {
                               "Service": "lambda.amazonaws.com"
                             },
                             "Action": "sts:AssumeRole"
                           }
                         ]
                        }
                        """.stripIndent())
                    .build())

            iamWaiter.waitUntilRoleExists(GetRoleRequest.builder()
                    .roleName(role.role().roleName())
                    .build())

            iamClient.attachRolePolicy(AttachRolePolicyRequest.builder()
                    .roleName(role.role().roleName())
                    .policyArn(policy.policy().arn())
                    .build())

            return role.role();
        }
    }

    private LambdaRequest createFunctionRequest(byte[] arr) {
        def role = getLambdaRole()
        CreateFunctionRequest.builder()
                .functionName(FUNCTION_NAME)
                .role(role.arn())
                .code(FunctionCode.builder()
                        .zipFile(SdkBytes.fromByteArray(arr))
                        .build())
                .runtime(Runtime.NODEJS18_X)
                .architectures(Architecture.X86_64)
                .handler("index.handler")
                .build()
    }

    private LambdaRequest deleteFunctionRequest() {
        DeleteFunctionRequest.builder()
                .functionName(FUNCTION_NAME)
                .build()
    }
}
