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
package io.micronaut.tracing.aws.filter;

import com.amazonaws.xray.AWSXRayRecorder;
import io.micronaut.context.annotation.Requires;
import io.micronaut.core.util.StringUtils;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MutableHttpRequest;
import io.micronaut.http.annotation.Filter;
import io.micronaut.http.filter.ClientFilterChain;
import io.micronaut.http.filter.HttpClientFilter;
import io.micronaut.http.filter.ServerFilterPhase;
import io.micronaut.tracing.aws.configuration.XRayConfigurationProperties;
import org.reactivestreams.Publisher;

import javax.validation.constraints.NotNull;

/**
 * {@link HttpClientFilter} that handles creation of AWX x-ray subsegment.
 *
 * @author Pavol Gressa
 * @since 2.5
 */
@Requires(beans = AWSXRayRecorder.class)
@Requires(property = XRayConfigurationProperties.PREFIX + ".http-filter.client.enabled", notEquals = StringUtils.FALSE, defaultValue = StringUtils.TRUE)
@Filter("/**")
public class XRayHttpClientFilter implements HttpClientFilter {
    private final AWSXRayRecorder recorder;

    public XRayHttpClientFilter(@NotNull AWSXRayRecorder recorder) {
        this.recorder = recorder;
    }

    @Override
    public Publisher<? extends HttpResponse<?>> doFilter(MutableHttpRequest<?> request, ClientFilterChain chain) {
        Publisher<? extends HttpResponse<?>> requestPublisher = chain.proceed(request);
        return new XRayClientTracingPublisher(request, requestPublisher, recorder);
    }

    @Override
    public int getOrder() {
        return ServerFilterPhase.TRACING.order();
    }
}
