package io.micronaut.aws.lambda.events.serde;

import org.junit.platform.suite.api.ExcludeClassNamePatterns;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

@Suite
@ExcludeClassNamePatterns(
    // Error decoding property [Map messageAttributes] of type [class com.amazonaws.services.lambda.runtime.events.SQSEvent$SQSMessage]: Error decoding property [ByteBuffer binaryValue] of type [class com.amazonaws.services.lambda.runtime.events.SQSEvent$MessageAttribute]
    "io.micronaut.aws.lambda.events.tests.SqsHandlerTest"
)
@SelectPackages({
    "io.micronaut.aws.lambda.events.tests",
    "io.micronaut.aws.lambda.events.serde.tests"
})
@SuiteDisplayName("AWS Lambda Events TCK for Serde")
public class AwsLambdaEventsSerdeSuite {
}
