/*
 * Copyright 2017-2021 original authors
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
package io.micronaut.aws.xray;

import edu.umd.cs.findbugs.annotations.NonNull;
import io.micronaut.core.util.Toggleable;

import java.util.Optional;

/**
 * Configuration for AWS X-Ray.
 *
 * @author Pavol Gressa
 * @since 2.7.0
 */

public interface XRayConfiguration extends Toggleable {

    /**
     *
     * @return Sampling Rule
     */
    @NonNull
    Optional<String> getSamplingRule();

    /**
     *
     * @return Segment Name
     */
    @NonNull
    Optional<String> getSegmentName();

    /**
     *
     * @return Whether the X-Ray HTTP Server filter is enabled
     */
    boolean isServerFilter();

    /**
     *
     * @return Whether the X-Ray HTTP Client filter is enabled
     */
    boolean isClientFilter();

    /**
     *
     * @return Whether the X-Ray Cloud Watch Metrics integration is enabled
     */
    boolean isCloudWatchMetrics();

    /**
     *
     * @return Whether X-Ray Tracing Interceptor should be configured for every AWS SDK Client builder.
     */
    boolean isSdkClients();
}
