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
import io.micronaut.core.util.Toggleable;

/**
 * Configuration for {@link org.slf4j.MDC} Keys.
 *
 * @since 2.2.2
 * @author Sergio del Amo
 */
public interface MappingDiagnosticContextConfiguration extends Toggleable {

    /**
     *
     * @return MDC key for AWS request id
     */
    @NonNull
    String getAwsRequestId();

    /**
     *
     * @return MDC key for function name
     */
    @NonNull
    String getFunctionName();

    /**
     *
     * @return MDC key for function version
     */
    @NonNull
    String getFunctionVersion();

    /**
     *
     * @return MDC key for function arn
     */
    @NonNull
    String getFunctionArn();

    /**
     *
     * @return MDC key for memory limit
     */
    @NonNull
    String getMemoryLimit();

    /**
     *
     * @return MDC key for remaining time
     */
    @NonNull
    String getRemainingTime();

    /**
     *
     * @return MDC key for XRay Trace-Id
     */
    @NonNull
    String getXrayTraceId();
}
