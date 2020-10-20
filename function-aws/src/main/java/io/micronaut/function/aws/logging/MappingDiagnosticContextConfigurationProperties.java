/*
 * Copyright 2017-2020 original authors
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
package io.micronaut.function.aws.logging;

import edu.umd.cs.findbugs.annotations.NonNull;
import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.context.annotation.Requires;
import io.micronaut.core.util.StringUtils;

import javax.validation.constraints.NotBlank;

/**
 * {@link ConfigurationProperties} implementation of {@link MappingDiagnosticContextConfiguration}.
 * @since 2.2.2
 * @author Sergio del Amo
 */
@Requires(property = MappingDiagnosticContextConfigurationProperties.PREFIX + ".enabled", notEquals = StringUtils.FALSE)
@ConfigurationProperties(MappingDiagnosticContextConfigurationProperties.PREFIX)
public class MappingDiagnosticContextConfigurationProperties implements MappingDiagnosticContextConfiguration {

    public static final String PREFIX = "micronaut.aws.lambda.mdc";

    public static final String DEFAULT_AWS_REQUEST_ID = "AWSRequestId";
    public static final String DEFAULT_FUNCTION_NAME = "AWSFunctionName";
    public static final String DEFAULT_FUNCTION_VERSION = "AWSFunctionVersion";
    public static final String DEFAULT_FUNCTION_ARN = "AWSFunctionArn";
    public static final String DEFAULT_FUNCTION_MEMORY_SIZE = "AWSFunctionMemoryLimit";
    public static final String DEFAULT_FUNCTION_REMAINING_TIME = "AWSFunctionRemainingTime";
    public static final String DEFAULT_XRAY_TRACE_ID = "AWS-XRAY-TRACE-ID";

    private static final boolean DEFAULT_ENABLED = true;

    private boolean enabled = DEFAULT_ENABLED;

    @NotBlank
    @NonNull
    private String xrayTraceId = DEFAULT_XRAY_TRACE_ID;

    @NotBlank
    @NonNull
    private String awsRequestId = DEFAULT_AWS_REQUEST_ID;

    @NotBlank
    @NonNull
    private String functionName = DEFAULT_FUNCTION_NAME;

    @NotBlank
    @NonNull
    private String functionVersion = DEFAULT_FUNCTION_VERSION;

    @NotBlank
    @NonNull
    private String functionArn = DEFAULT_FUNCTION_ARN;

    @NotBlank
    @NonNull
    private String memoryLimit = DEFAULT_FUNCTION_MEMORY_SIZE;

    @NotBlank
    @NonNull
    private String remainingTime = DEFAULT_FUNCTION_REMAINING_TIME;

    @Override
    @NonNull
    public String getAwsRequestId() {
        return awsRequestId;
    }

    /**
     * Sets key for the AWS Request id. Default {@value #DEFAULT_AWS_REQUEST_ID}.
     * @param awsRequestId MDC key for AWS request id
     */
    public void setAwsRequestId(@NonNull String awsRequestId) {
        this.awsRequestId = awsRequestId;
    }

    @Override
    @NonNull
    public String getFunctionName() {
        return functionName;
    }

    /**
     * Sets key for the function name. Default {@value #DEFAULT_FUNCTION_NAME}.
     * @param functionName MDC Key for Function Name
     */
    public void setFunctionName(@NonNull String functionName) {
        this.functionName = functionName;
    }

    @Override
    @NonNull
    public String getFunctionVersion() {
        return functionVersion;
    }

    /**
     * Sets key for the function version. Default {@value #DEFAULT_FUNCTION_VERSION}.
     * @param functionVersion MDC key for function version
     */
    public void setFunctionVersion(@NonNull String functionVersion) {
        this.functionVersion = functionVersion;
    }

    @Override
    @NonNull
    public String getFunctionArn() {
        return functionArn;
    }

    /**
     Sets key for the function arn. Default {@value #DEFAULT_FUNCTION_ARN}.
     * @param functionArn MDC key for function arn
     */
    public void setFunctionArn(@NonNull String functionArn) {
        this.functionArn = functionArn;
    }

    @Override
    @NonNull
    public String getMemoryLimit() {
        return memoryLimit;
    }

    /**
     * Sets key for the function memory limit. Default {@value #DEFAULT_FUNCTION_MEMORY_SIZE}.
     * @param memoryLimit MDC key for memory limit
     */
    public void setMemoryLimit(@NonNull String memoryLimit) {
        this.memoryLimit = memoryLimit;
    }

    /**
     * Sets key for the function remaining time. Default {@value #DEFAULT_FUNCTION_REMAINING_TIME}.
     *
     * @param remainingTime MDC key for remaining time
     */
    public void setRemainingTime(@NonNull String remainingTime) {
        this.remainingTime = remainingTime;
    }

    @Override
    @NonNull
    public String getRemainingTime() {
        return remainingTime;
    }

    @Override
    @NonNull
    public String getXrayTraceId() {
        return xrayTraceId;
    }

    /**
     * Sets key for the XRay trace id. Default {@value #DEFAULT_XRAY_TRACE_ID}.
     *
     * @param xrayTraceId MDC key for XRay Trace-Id
     */
    public void setXrayTraceId(@NonNull String xrayTraceId) {
        this.xrayTraceId = xrayTraceId;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Sets whether the MDX population is enabled. Default {@value #DEFAULT_ENABLED}.
     *
     * @param enabled  Whether the component is enabled
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
