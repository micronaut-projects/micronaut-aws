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
import io.micronaut.core.util.StringUtils;

import java.util.Optional;

/**
 * Utility class to parse X-Ray Trace ID.
 * @author Sergio del Amo
 * @since 3.2.2
 */
public final class XRayUtils {
    private static final String ENV_X_AMZN_TRACE_ID = "_X_AMZN_TRACE_ID";

    /**
     * <a href="https://github.com/aws/aws-xray-sdk-java/issues/251">Read _X_AMZN_TRACE_ID from system properties if environment variable is not set</a>.
     */
    public static final String LAMBDA_TRACE_HEADER_PROP = "com.amazonaws.xray.traceHeader";

    /**
     * Constructor.
     */
    private XRayUtils() {
    }

    /**
     * Parses XRay Trace ID from _X_AMZN_TRACE_ID environment variable.
     * @see <a href="https://docs.aws.amazon.com/xray/latest/devguide/xray-sdk-java-configuration.html">Trace ID injection into logs</a>
     * @return Trace id or empty if not found
     */
    @NonNull
    public static Optional<String> parseXrayTraceId() {
        String lambdaTraceHeaderKey = System.getenv(ENV_X_AMZN_TRACE_ID);
        lambdaTraceHeaderKey = StringUtils.isNotEmpty(lambdaTraceHeaderKey) ? lambdaTraceHeaderKey
                : System.getProperty(LAMBDA_TRACE_HEADER_PROP);
        if (lambdaTraceHeaderKey != null) {
            String[] arr = lambdaTraceHeaderKey.split(";");
            if (arr.length >= 1) {
                return Optional.of(arr[0].replace("Root=", ""));
            }
        }
        return Optional.empty();
    }
}
