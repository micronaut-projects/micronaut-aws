/*
 * Copyright 2021 original authors
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
package io.micronaut.tracing.aws.client;

import com.amazonaws.xray.AWSXRayRecorder;
import com.amazonaws.xray.interceptors.TracingInterceptor;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.event.BeanCreatedEvent;
import io.micronaut.context.event.BeanCreatedEventListener;
import io.micronaut.core.util.StringUtils;
import io.micronaut.tracing.aws.XRayConfiguration;
import software.amazon.awssdk.core.client.builder.SdkClientBuilder;

import javax.inject.Singleton;

/**
 * Configures x-ray tracing interceptor for all sdk client builders.
 *
 * @author Pavol Gressa
 * @since 2.5
 */
@Requires(beans = AWSXRayRecorder.class)
@Requires(classes = TracingInterceptor.class)
@Requires(property = XRayConfiguration.SdkClientsConfiguration.PREFIX +".enabled", notEquals = StringUtils.FALSE)
@Singleton
public class SdkClientBuilderListener implements BeanCreatedEventListener<SdkClientBuilder<?, ?>> {

    /**
     * Handle {@link SdkClientBuilder} builder creation.
     * @param event bean created event
     * @return sdk client builder
     */
    @Override
    public SdkClientBuilder<?, ?> onCreated(BeanCreatedEvent<SdkClientBuilder<?, ?>> event) {
        return event.getBean().overrideConfiguration(builder ->
                builder.addExecutionInterceptor(new TracingInterceptor()));
    }
}
