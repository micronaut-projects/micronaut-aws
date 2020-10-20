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

import com.amazonaws.services.lambda.runtime.Context;
import edu.umd.cs.findbugs.annotations.NonNull;
import io.micronaut.context.annotation.Requires;
import org.slf4j.MDC;

import javax.inject.Singleton;
import javax.validation.constraints.NotNull;
import java.util.Optional;

/**
 * Default implementation which populates {@link org.slf4j.MDC} with {@link Context} values and the Amazon XRay trace id.
 * @since 2.2.2
 * @author Sergio del Amo
 */
@Requires(beans = MappingDiagnosticContextConfiguration.class)
@Singleton
public class DefaultMappingDiagnosticContextSetter implements MappingDiagnosticContextSetter {

    private static final String ENV_X_AMZN_TRACE_ID = "_X_AMZN_TRACE_ID";
    private final MappingDiagnosticContextConfiguration mappingDiagnosticContextConfiguration;

    /**
     *
     * @param mappingDiagnosticContextConfiguration Configuration for MDC Keys
     */
    public DefaultMappingDiagnosticContextSetter(MappingDiagnosticContextConfiguration mappingDiagnosticContextConfiguration) {
        this.mappingDiagnosticContextConfiguration = mappingDiagnosticContextConfiguration;
    }

    /**
     * @see <a href="https://docs.aws.amazon.com/lambda/latest/dg/java-logging.html">AWS Lambda function logging in Java</a>
     * @param context The Lambda execution environment context object.
     */
    @Override
    public void populateMappingDiagnosticContextValues(@NonNull @NotNull Context context) {
        if (context.getAwsRequestId() != null) {
            put(mappingDiagnosticContextConfiguration.getAwsRequestId(), context.getAwsRequestId());
        }
        if (context.getFunctionName() != null) {
            put(mappingDiagnosticContextConfiguration.getFunctionName(), context.getFunctionName());
        }
        if (context.getFunctionVersion() != null) {
            put(mappingDiagnosticContextConfiguration.getFunctionVersion(), context.getFunctionVersion());
        }
        if (context.getInvokedFunctionArn() != null) {
            put(mappingDiagnosticContextConfiguration.getFunctionArn(), context.getInvokedFunctionArn());
        }
        put(mappingDiagnosticContextConfiguration.getMemoryLimit(), String.valueOf(context.getMemoryLimitInMB()));
        put(mappingDiagnosticContextConfiguration.getRemainingTime(), String.valueOf(context.getRemainingTimeInMillis()));
    }

    @Override
    public void populateMappingDiagnosticContextWithXrayTraceId() {
        parseXrayTraceId().ifPresent(xrayTraceId -> put(mappingDiagnosticContextConfiguration.getXrayTraceId(), xrayTraceId));
    }

    /**
     * Put a diagnostic context value.
     * @param key non-null key
     * @param val value to put in the map
     * @throws IllegalArgumentException in case the "key" parameter is null
     */
    protected void put(@NonNull String key, @NonNull String val) throws IllegalArgumentException {
        MDC.put(key, val);
    }

    /**
     * Parses XRay Trace ID from _X_AMZN_TRACE_ID environment variable.
     * @see <a href="https://docs.aws.amazon.com/xray/latest/devguide/xray-sdk-java-configuration.html">Trace ID injection into logs</a>
     * @return Trace id or empty if not found
     */
    @NonNull
    private static Optional<String> parseXrayTraceId() {
        final String X_AMZN_TRACE_ID = System.getenv(ENV_X_AMZN_TRACE_ID);
        if (X_AMZN_TRACE_ID != null) {
            return Optional.of(X_AMZN_TRACE_ID.split(";")[0].replace("Root=", ""));
        }
        return Optional.empty();
    }
}
