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
package io.micronaut.aws.xray.sampling;

import com.amazonaws.xray.strategy.sampling.CentralizedSamplingStrategy;
import com.amazonaws.xray.strategy.sampling.SamplingStrategy;
import io.micronaut.aws.xray.configuration.XRayConfiguration;
import io.micronaut.aws.xray.configuration.XRayConfigurationProperties;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.exceptions.DisabledBeanException;
import io.micronaut.core.io.ResourceResolver;
import jakarta.inject.Singleton;
import java.net.URL;
import java.util.Optional;

/**
 * Builds a {@link CentralizedSamplingStrategy} from a static resource.
 * @author Sergio del Amo
 * @since 2.7.0
 */
@Requires(property = XRayConfigurationProperties.PREFIX + ".sampling-rule")
@Factory
public class CentralizedSamplingStrategyFactory {

    /**
     *
     * @param resourceResolver Resource Resolver
     * @param xRayConfiguration X-Ray Configuration
     * @return a {@link CentralizedSamplingStrategy}.
     */
    @Singleton
    public SamplingStrategy buildSamplingStrategy(ResourceResolver resourceResolver,
                                                  XRayConfiguration xRayConfiguration) {
        if (xRayConfiguration.getSamplingRule().isPresent()) {
            Optional<URL> urlOptional = resourceResolver.getResource(xRayConfiguration.getSamplingRule().get());
            if (urlOptional.isPresent()) {
                return new CentralizedSamplingStrategy(urlOptional.get());
            }
            throw new DisabledBeanException("could not load resource for " + xRayConfiguration.getSamplingRule());
        }
        throw new DisabledBeanException("could not load CentralizedSamplingStrategy for tracing.xray.sampling-rule");

    }
}
