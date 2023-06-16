package io.micronaut.aws.lambda.events;

import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

@Suite
@SelectPackages("io.micronaut.aws.lambda.events.tests")
@SuiteDisplayName("AWS Lambda Events TCK for Serde")
public class AwsLambdaEventsSerdeSuite {
}
