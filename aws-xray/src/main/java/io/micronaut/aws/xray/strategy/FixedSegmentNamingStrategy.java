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
package io.micronaut.aws.xray.strategy;

import io.micronaut.aws.xray.configuration.XRayConfiguration;
import io.micronaut.aws.xray.configuration.XRayConfigurationProperties;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.exceptions.ConfigurationException;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.http.HttpRequest;
import jakarta.inject.Singleton;

/**
 * <p><b>N.B.</b>: This class was forked from AWS X-Ray Java SDK with modifications.</p>
 * <p>As per the Apache 2.0 license, the original copyright notice and all author and copyright information have
 * remained intact.</p>
 *
 * @since 3.2.0
 */
@Requires(property = FixedSegmentNamingStrategy.PROPERTY)
@Singleton
public class FixedSegmentNamingStrategy implements SegmentNamingStrategy {
    public static final int ORDER = SystemPropertySegmentNamingStrategy.ORDER + 100;
    public static final String PROPERTY = XRayConfigurationProperties.PREFIX + ".fixed-name";

    private final String fixedName;

    /**
     *
     * @param xRayConfiguration X-Ray Configuration
     */
    public FixedSegmentNamingStrategy(XRayConfiguration xRayConfiguration) {
        if (!xRayConfiguration.getFixedName().isPresent()) {
            throw new ConfigurationException(FixedSegmentNamingStrategy.PROPERTY + " not present");
        }
        this.fixedName = xRayConfiguration.getFixedName().get();
    }

    @Override
    public int getOrder() {
        return ORDER;
    }

    @Override
    @NonNull
    public String nameForRequest(@NonNull HttpRequest<?> request) {
        return fixedName;
    }
}
