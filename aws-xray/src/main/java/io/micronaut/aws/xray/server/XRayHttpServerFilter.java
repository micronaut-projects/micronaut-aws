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
package io.micronaut.aws.xray.server;

import com.amazonaws.xray.AWSXRayRecorder;
import com.amazonaws.xray.entities.Segment;
import com.amazonaws.xray.javax.servlet.AWSXRayServletFilter;
import com.amazonaws.xray.strategy.SegmentNamingStrategy;
import edu.umd.cs.findbugs.annotations.NonNull;
import io.micronaut.aws.xray.XRayConfiguration;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.annotation.Filter;
import io.micronaut.http.filter.HttpServerFilter;
import io.micronaut.http.filter.ServerFilterChain;
import io.micronaut.http.filter.ServerFilterPhase;
import io.micronaut.runtime.ApplicationConfiguration;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.Optional;

/**
 * Micronaut AWS X-Ray {@link HttpServerFilter} that forwards request to the {@link AWSXRayServletFilter}.
 *
 * The {@link SegmentNamingStrategy} is evaluated in this order:
 * <ol>
 *  <li>Bean of {@link SegmentNamingStrategy} type.</li>
 *  <li>{@link  SegmentNamingStrategy#fixed(String)} configured by {@code aws.xray.httpfilter.server.fixed-segment-name} property.</li>
 *  <li>{@link  SegmentNamingStrategy#fixed(String)} configured by {@code micronaut.application.name} property.</li>
 *  <li>{@link  SegmentNamingStrategy#fixed(String)} with default of {@code micronaut.xray-http-filter}.</li>
 * </ol>
 *
 * @author Pavol Gressa
 * @since 2.7.0
 */
@Filter("/**")
public class XRayHttpServerFilter implements HttpServerFilter {
    private static final Logger LOG = LoggerFactory.getLogger(XRayHttpServerFilter.class);
    private static final String DEFAULT_FIXED_SEGMENT_NAME = "micronaut.xray-http-filter";

    private static final CharSequence APPLIED = XRayHttpServerFilter.class.getName() + "-applied";
    private static final CharSequence CONTINUE = XRayHttpServerFilter.class.getName() + "-continue";
    private static final CharSequence CURRENT_SEGMENT_CONTEXT = XRayHttpServerFilter.class.getName() + "-segment-ctx";

    private final AWSXRayServletFilter delegate;
    private final ConversionService<?> conversionService;

    public XRayHttpServerFilter(
            ApplicationConfiguration applicationConfiguration,
            XRayConfiguration xRayConfiguration,
            AWSXRayRecorder recorder,
            ConversionService<?> conversionService,
            @Nullable SegmentNamingStrategy segmentNamingStrategy) {

        if (segmentNamingStrategy == null) {
            String fixedSegmentName = resolveFixedSegmentName(applicationConfiguration, xRayConfiguration);
            segmentNamingStrategy = SegmentNamingStrategy.fixed(fixedSegmentName);
        }

        delegate = new AWSXRayServletFilter(segmentNamingStrategy, recorder);
        this.conversionService = conversionService;
    }

    @NonNull
    private String resolveFixedSegmentName(@NonNull ApplicationConfiguration applicationConfiguration,
                                           @NonNull XRayConfiguration xRayConfiguration) {
        Optional<String> fixedSegmentNameOptional = xRayConfiguration.getSegmentName();
        if (fixedSegmentNameOptional.isPresent()) {
            return fixedSegmentNameOptional.get();
        } else if (applicationConfiguration.getName().isPresent()) {
            return applicationConfiguration.getName().get();
        } else {
            return DEFAULT_FIXED_SEGMENT_NAME;
        }
    }

    /**
     * Creates {@link SegmentRequestContext}.
     *
     * @param request  request
     * @param response response
     * @return segment request context
     */
    SegmentRequestContext initSegmentRequestContext(HttpRequest<?> request, MutableHttpResponse<?> response) {
        SegmentRequestContext.HttpRequestAdapter httpRequestAdapter = new SegmentRequestContext.HttpRequestAdapter(request);
        SegmentRequestContext.HttpResponseAdapter httpResponseAdapter = new SegmentRequestContext.HttpResponseAdapter(response);
        Segment segment = delegate.preFilter(httpRequestAdapter, httpResponseAdapter);
        return new SegmentRequestContext(segment, httpRequestAdapter, httpResponseAdapter);
    }

    @Override
    public Publisher<MutableHttpResponse<?>> doFilter(HttpRequest<?> request, ServerFilterChain chain) {

        boolean applied = request.getAttribute(APPLIED, Boolean.class).orElse(false);
        boolean continued = request.getAttribute(CONTINUE, Boolean.class).orElse(false);

        if (applied && !continued) {
            return chain.proceed(request);
        }
        if (LOG.isTraceEnabled()) {
            LOG.trace(String.format("Tracing request %s", request));
        }
        SegmentRequestContext segmentRequestContext;
        if (!continued) {
            segmentRequestContext = initSegmentRequestContext(request, SegmentRequestContext.HttpResponseAdapter.createEmpty(conversionService));
            request.setAttribute(CURRENT_SEGMENT_CONTEXT, segmentRequestContext);
            request.setAttribute(APPLIED, true);
        } else {
            segmentRequestContext = request.getAttribute(CURRENT_SEGMENT_CONTEXT, SegmentRequestContext.class).orElse(null);
        }

        return new XRayServerTracingPublisher(chain.proceed(request), segmentRequestContext, delegate) {

            @Override
            protected void doOnError(@NonNull Throwable throwable, @NonNull SegmentRequestContext segmentRequestContex) {
                if (segmentRequestContext != null && segmentRequestContex.getHttpRequest() != null) {
                    segmentRequestContext.getHttpRequest().setAttribute(CONTINUE, true);
                }
            }
        };
    }

    @Override
    public int getOrder() {
        return ServerFilterPhase.TRACING.order();
    }
}
