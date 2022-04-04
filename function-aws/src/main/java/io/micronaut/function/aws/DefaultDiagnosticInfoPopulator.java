/*
 * Copyright 2017-2022 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micronaut.function.aws;

import io.micronaut.core.annotation.NonNull;
import jakarta.inject.Singleton;
import org.slf4j.MDC;
import com.amazonaws.services.lambda.runtime.Context;

/**
 * @author Sergio del Amo
 * @author Constantine Linnick
 * @since 3.2.2
 */
@Singleton
public class DefaultDiagnosticInfoPopulator implements DiagnosticInfoPopulator {

    public static final String MDC_DEFAULT_AWS_REQUEST_ID = "AWSRequestId";
    public static final String MDC_DEFAULT_FUNCTION_NAME = "AWSFunctionName";
    public static final String MDC_DEFAULT_FUNCTION_VERSION = "AWSFunctionVersion";
    public static final String MDC_DEFAULT_FUNCTION_ARN = "AWSFunctionArn";
    public static final String MDC_DEFAULT_FUNCTION_MEMORY_SIZE = "AWSFunctionMemoryLimit";
    public static final String MDC_DEFAULT_FUNCTION_REMAINING_TIME = "AWSFunctionRemainingTime";
    public static final String MDC_DEFAULT_XRAY_TRACE_ID = "AWS-XRAY-TRACE-ID";

    /**
     * @see <a href="https://docs.aws.amazon.com/lambda/latest/dg/java-logging.html">AWS Lambda function logging in Java</a>
     * @param context The Lambda execution environment context object.
     */
    @Override
    public void populateMappingDiagnosticContextValues(@NonNull Context context) {
        if (context.getAwsRequestId() != null) {
            mdcput(MDC_DEFAULT_AWS_REQUEST_ID, context.getAwsRequestId());
        }
        if (context.getFunctionName() != null) {
            mdcput(MDC_DEFAULT_FUNCTION_NAME, context.getFunctionName());
        }
        if (context.getFunctionVersion() != null) {
            mdcput(MDC_DEFAULT_FUNCTION_VERSION, context.getFunctionVersion());
        }
        if (context.getInvokedFunctionArn() != null) {
            mdcput(MDC_DEFAULT_FUNCTION_ARN, context.getInvokedFunctionArn());
        }
        mdcput(MDC_DEFAULT_FUNCTION_MEMORY_SIZE, String.valueOf(context.getMemoryLimitInMB()));
        mdcput(MDC_DEFAULT_FUNCTION_REMAINING_TIME, String.valueOf(context.getRemainingTimeInMillis()));
    }

    /**
     * Put a diagnostic context value.
     * @param key non-null key
     * @param val value to put in the map
     * @throws IllegalArgumentException in case the "key" parameter is null
     */
    protected void mdcput(@NonNull String key, @NonNull String val) throws IllegalArgumentException {
        MDC.put(key, val);
    }

    @Override
    public void populateMappingDiagnosticContextWithXrayTraceId() {
        XRayUtils.parseXrayTraceId().ifPresent(xrayTraceId -> mdcput(MDC_DEFAULT_XRAY_TRACE_ID, xrayTraceId));
    }
}
