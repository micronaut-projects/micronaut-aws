/*
 * Copyright 2017-2020 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micronaut.function.aws.runtime;

/**
 * Invocation event response which headers contain additional data about the invocation.
 * @see <a href="https://docs.aws.amazon.com/lambda/latest/dg/runtimes-api.html">Invocation Events response headers</a>
 * @author sdelamo
 */
public interface LambdaRuntimeInvocationResponseHeaders {

    /**
     * The request ID, which identifies the request that triggered the function invocation.
     * For example, 8476a536-e9f4-11e8-9739-2dfe598c3fcd.
     */
    String LAMBDA_RUNTIME_AWS_REQUEST_ID = "Lambda-Runtime-Aws-Request-Id";

    /**
     * The date that the function times out in Unix time milliseconds.
     * For example, 1542409706888.
     */
    String LAMBDA_RUNTIME_DEADLINE_MS = "Lambda-Runtime-Deadline-Ms";

    /**
     * The ARN of the Lambda function, version, or alias that's specified in the invocation.
     * For example, arn:aws:lambda:us-east-2:123456789012:function:custom-runtime.
     */
    String LAMBDA_RUNTIME_INVOKED_FUNCTION_ARN = "Lambda-Runtime-Invoked-Function-Arn";

    /**
     * The AWS X-Ray tracing header.
     * For example, Root=1-5bef4de7-ad49b0e87f6ef6c87fc2e700;Parent=9a9197af755a6419;Sampled=1.
     */
    String LAMBDA_RUNTIME_TRACE_ID = "Lambda-Runtime-Trace-Id";

    /**
     * For invocations from the AWS Mobile SDK, data about the client application and device.
     */
    String LAMBDA_RUNTIME_CLIENT_CONTEXT = "Lambda-Runtime-Client-Context";

    /**
     * For invocations from the AWS Mobile SDK, data about the Amazon Cognito identity provider.
     */
    String LAMBDA_RUNTIME_COGNITO_IDENTITY = "Lambda-Runtime-Cognito-Identity";
}
