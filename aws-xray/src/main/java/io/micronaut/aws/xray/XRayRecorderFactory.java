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

import com.amazonaws.xray.AWSXRay;
import com.amazonaws.xray.AWSXRayRecorder;
import com.amazonaws.xray.AWSXRayRecorderBuilder;
import com.amazonaws.xray.listeners.SegmentListener;
import com.amazonaws.xray.plugins.Plugin;
import com.amazonaws.xray.strategy.sampling.CentralizedSamplingStrategy;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.env.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.net.URL;
import java.util.Collection;

/**
 * The factory configures and creates the {@link AWSXRayRecorder}. Based on the configured
 * {@link Environment} respective {@link Plugin}s are configured to the {@link AWSXRayRecorder}.
 *
 * @author Pavol Gressa
 * @since 2.7.0
 */
@Factory
public class XRayRecorderFactory {

    private static final Logger LOG = LoggerFactory.getLogger(XRayRecorderFactory.class);

    /**
     * Create the {@link AWSXRayRecorderBuilder}. For additional configuration register {@link io.micronaut.context.event.BeanCreatedEventListener}.
     *
     * @return aws xray recorder builder
     */
    @Singleton
    public AWSXRayRecorderBuilder builder() {
        return AWSXRayRecorderBuilder.standard();
    }

    /**
     * Creates {@link AWSXRayRecorder} singleton bean.
     *
     * @param builder              The builder
     * @param awsxRayConfiguration The recorder configuration
     * @param plugins              The {@link Plugin}s to configure
     * @param segmentListeners     The {@link SegmentListener}s to configure
     * @return built {@link AWSXRayRecorder}
     */
    @Singleton
    public AWSXRayRecorder build(AWSXRayRecorderBuilder builder,
                                 XRayConfiguration awsxRayConfiguration,
                                 Collection<Plugin> plugins,
                                 Collection<SegmentListener> segmentListeners
    ) {
        if (awsxRayConfiguration.getSamplingRule().isPresent()) {
            String sampligRule = awsxRayConfiguration.getSamplingRule().get();
            try {
                URL ruleFile = XRayRecorderFactory.class.getResource(sampligRule);
                builder.withSamplingStrategy(new CentralizedSamplingStrategy(ruleFile));
            } catch (Exception e) {
                if (LOG.isWarnEnabled()) {
                    LOG.warn(String.format("Failed to configure sampling rule: %s", sampligRule), e);
                }
            }
        }

        builder.withDefaultPlugins();

        for (Plugin plugin : plugins) {
            builder.withPlugin(plugin);
        }
        for (SegmentListener segmentListener : segmentListeners) {
            builder.withSegmentListener(segmentListener);
        }

        AWSXRayRecorder awsxRayRecorder = builder.build();
        AWSXRay.setGlobalRecorder(awsxRayRecorder);
        return awsxRayRecorder;
    }
}
