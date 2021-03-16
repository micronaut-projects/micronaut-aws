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
import com.amazonaws.xray.javax.servlet.AWSXRayServletFilter;
import com.amazonaws.xray.strategy.SegmentNamingStrategy;
import io.micronaut.context.annotation.Requires;
import io.micronaut.core.util.StringUtils;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.annotation.Filter;
import io.micronaut.http.filter.HttpServerFilter;
import io.micronaut.http.filter.ServerFilterChain;
import io.micronaut.http.filter.ServerFilterPhase;
import io.micronaut.tracing.aws.XRayConfiguration;
import org.reactivestreams.Publisher;

import javax.annotation.Nullable;

/**
 * Micronaut AWS x-ray filter.
 *
 * @author Pavol Gressa
 * @since 2.3
 */
@Requires(beans = AWSXRayRecorder.class)
@Requires(classes = AWSXRayServletFilter.class)
@Requires(property = XRayConfiguration.XRayHttpFilterConfiguration.PREFIX + ".enabled", notEquals = StringUtils.FALSE)
@Filter("/**")
public class AWSXRayHttpServerFilter implements HttpServerFilter {
    private final AWSXRayServletFilter delegate;

    public AWSXRayHttpServerFilter(@Nullable SegmentNamingStrategy segmentNamingStrategy, @Nullable AWSXRayRecorder recorder) {
        delegate = new AWSXRayServletFilter(segmentNamingStrategy, recorder);
    }

    @Override
    public Publisher<MutableHttpResponse<?>> doFilter(HttpRequest<?> request, ServerFilterChain chain) {
        return new XRayPublisher<>(chain.proceed(request), request, delegate);
    }

    @Override
    public int getOrder() {
        return ServerFilterPhase.TRACING.order();
    }
}
