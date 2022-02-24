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
package io.micronaut.aws.xray.recorder;

import com.amazonaws.xray.AWSXRay;
import com.amazonaws.xray.AWSXRayRecorder;
import com.amazonaws.xray.AWSXRayRecorderBuilder;
import com.amazonaws.xray.contexts.LambdaSegmentContextResolver;
import com.amazonaws.xray.contexts.SegmentContextResolverChain;
import com.amazonaws.xray.contexts.ThreadLocalSegmentContextResolver;
import com.amazonaws.xray.listeners.SegmentListener;
import com.amazonaws.xray.plugins.Plugin;
import com.amazonaws.xray.strategy.sampling.SamplingStrategy;
import io.micronaut.aws.xray.configuration.XRayConfiguration;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.context.annotation.Factory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.inject.Singleton;
import java.util.Collection;

/**
 * The factory configures and creates the {@link AWSXRayRecorder}.
 *
 * @author Pavol Gressa
 * @author Sergio del Amo
 * @since 3.2.0
 */
@Factory
public class XRayRecorderFactory {

    private static final Logger LOG = LoggerFactory.getLogger(XRayRecorderFactory.class);

    /**
     * Builds a {@link AWSXRayRecorder} and sets the recorder as Global Recorder {@link AWSXRay#setGlobalRecorder(AWSXRayRecorder)}.
     * @param builder X-Ray recorder builder
     * @return A {@link AWSXRayRecorder} singleton.
     */
    @Singleton
    public AWSXRayRecorder build(AWSXRayRecorderBuilder builder) {
        AWSXRayRecorder awsxRayRecorder = builder
                .build();
        AWSXRay.setGlobalRecorder(awsxRayRecorder);
        return awsxRayRecorder;
    }

    /**
     * Creates a standard {@link AWSXRayRecorderBuilder} and registers the following plugins:
     * - The default plugins ({@link com.amazonaws.xray.plugins.EC2Plugin}, {@link com.amazonaws.xray.plugins.ECSPlugin}, {@link com.amazonaws.xray.plugins.EKSPlugin}, {@link com.amazonaws.xray.plugins.ElasticBeanstalkPlugin})
     * - The beans to type {@link Plugin}.
     * It registers the beans of type {@link SegmentListener} as segments listeners.
     * A {@link AWSXRayRecorderBuilder}
     * @param samplingStrategy Sampling Strategy
     * @param plugins Plugins
     * @param segmentListeners Segment Listeners
     * @param xRayConfiguration XRay Configuration
     * @return X-Ray recorder builder
     */
    @Singleton
    protected AWSXRayRecorderBuilder builder(@Nullable SamplingStrategy samplingStrategy,
                                             @NonNull Collection<Plugin> plugins,
                                             @NonNull Collection<SegmentListener> segmentListeners,
                                             @NonNull XRayConfiguration xRayConfiguration
    ) {
        AWSXRayRecorderBuilder builder = AWSXRayRecorderBuilder.standard()
                .withDefaultPlugins();
        for (Plugin plugin : plugins) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Adding plugin {} to AWS X-Ray Recorder builder", plugin.getClass().getSimpleName());
            }
            builder.withPlugin(plugin);
        }
        for (SegmentListener segmentListener : segmentListeners) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Adding segment Listener {} to AWS X-Ray Recorder builder", segmentListener.getClass().getSimpleName());
            }
            builder.withSegmentListener(segmentListener);
        }
        if (samplingStrategy != null) {
            builder.withSamplingStrategy(samplingStrategy);
        }

        if (xRayConfiguration.isServerFilter()) {
            builder.withSegmentContextResolverChain(createSegmentContextResolverChain());
        }
        return builder;
    }

    private SegmentContextResolverChain createSegmentContextResolverChain() {
        SegmentContextResolverChain segmentContextResolverChain = new SegmentContextResolverChain();
        segmentContextResolverChain.addResolver(new HttpRequestAttributeSegmentContextResolver());
        segmentContextResolverChain.addResolver(new LambdaSegmentContextResolver());
        segmentContextResolverChain.addResolver(new ThreadLocalSegmentContextResolver());
        return segmentContextResolverChain;
    }

}
