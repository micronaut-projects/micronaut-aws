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
package io.micronaut.aws.sdk.v2;

import io.micronaut.aws.ua.UserAgentUtils;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.event.BeanCreatedEvent;
import io.micronaut.context.event.BeanCreatedEventListener;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.core.client.builder.SdkClientBuilder;
import software.amazon.awssdk.core.client.config.SdkAdvancedClientOption;

/**
 * {@link SdkClientBuilder} listener which configures the User-Agent.
 *
 * @author Sergio del Amo
 * @since 3.10.0
 */
@Requires(classes = SdkClientBuilder.class)
@Singleton
public class SdkClientBuilderListener implements BeanCreatedEventListener<SdkClientBuilder<?, ?>> {
    private static final Logger LOG = LoggerFactory.getLogger(SdkClientBuilderListener.class);
    /**
     *
     * @param event bean created event
     * @return sdk client builder
     */
    @Override
    public SdkClientBuilder<?, ?> onCreated(BeanCreatedEvent<SdkClientBuilder<?, ?>> event) {
        if (LOG.isTraceEnabled()) {
            LOG.trace("Setting User-Agent for AWS SDK to {}", UserAgentUtils.userAgent());
        }
        return event.getBean().overrideConfiguration(builder ->
            builder.putAdvancedOption(SdkAdvancedClientOption.USER_AGENT_SUFFIX, UserAgentUtils.userAgent()));
    }
}
