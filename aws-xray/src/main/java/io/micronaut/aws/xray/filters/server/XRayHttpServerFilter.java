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
package io.micronaut.aws.xray.filters.server;

import com.amazonaws.xray.AWSXRayRecorder;
import com.amazonaws.xray.entities.Segment;
import com.amazonaws.xray.entities.TraceHeader;
import com.amazonaws.xray.entities.TraceID;
import com.amazonaws.xray.strategy.sampling.SamplingRequest;
import com.amazonaws.xray.strategy.sampling.SamplingResponse;
import com.amazonaws.xray.strategy.sampling.SamplingStrategy;
import io.micronaut.aws.xray.configuration.XRayConfiguration;
import io.micronaut.aws.xray.decorators.SegmentDecorator;
import io.micronaut.aws.xray.filters.HttpRequestAttributesCollector;
import io.micronaut.aws.xray.filters.HttpResponseAttributesCollector;
import io.micronaut.aws.xray.recorder.HttpRequestAttributeSegmentContext;
import io.micronaut.aws.xray.sampling.SampleDecisionParser;
import io.micronaut.aws.xray.strategy.SegmentNamingStrategy;
import io.micronaut.aws.xray.tracing.TraceHeaderParser;
import io.micronaut.context.annotation.Requires;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.async.publisher.Publishers;
import io.micronaut.core.util.AntPathMatcher;
import io.micronaut.core.util.CollectionUtils;
import io.micronaut.core.util.PathMatcher;
import io.micronaut.http.HttpAttributes;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.annotation.Filter;
import io.micronaut.http.filter.HttpServerFilter;
import io.micronaut.http.filter.ServerFilterChain;
import io.micronaut.http.filter.ServerFilterPhase;
import io.micronaut.scheduling.TaskExecutors;
import jakarta.inject.Named;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;

/**
 * <p><b>N.B.</b>: This class was forked from AWSXRayServletFilter AWS X-Ray Java SDK with modifications.</p>
 * <p>As per the Apache 2.0 license, the original copyright notice and all author and copyright information have
 * remained intact.</p>
 *
 * @author Sergio del Amo
 * @since 3.2.0
 */
@Requires(beans = AWSXRayRecorder.class)
@Requires(beans = SegmentNamingStrategy.class)
@Filter(Filter.MATCH_ALL_PATTERN)
public class XRayHttpServerFilter implements HttpServerFilter {
    public static final String ATTRIBUTE_X_RAY_TRACE_ENTITY = "X_RAY_TRACE_ENTITY";

    private static final Logger LOG = LoggerFactory.getLogger(XRayHttpServerFilter.class);

    private final AntPathMatcher pathMatcher;

    private final AWSXRayRecorder recorder;

    private final HttpRequestAttributesCollector httpRequestAttributesCollector;

    private final HttpResponseAttributesCollector httpResponseAttributesCollector;

    private final SegmentNamingStrategy<HttpRequest<?>> segmentNamingStrategy;

    private final List<SegmentDecorator> segmentDecorators;

    private final TraceHeaderParser traceHeaderParser;

    private final SampleDecisionParser sampleDecisionParser;

    private final ExecutorService executorService;

    @Nullable
    private final List<String> excludes;

    public XRayHttpServerFilter(XRayConfiguration xRayConfiguration,
                                AWSXRayRecorder awsxRayRecorder,
                                HttpResponseAttributesCollector httpResponseAttributesCollector,
                                HttpRequestAttributesCollector httpRequestAttributesCollector,
                                SegmentNamingStrategy<HttpRequest<?>> segmentNamingStrategy,
                                List<SegmentDecorator> segmentDecorators,
                                TraceHeaderParser traceHeaderParser,
                                SampleDecisionParser sampleDecisionParser,
                                @Named(TaskExecutors.IO) ExecutorService executorService) {
        this.excludes = xRayConfiguration.getExcludes().orElse(Collections.emptyList());
        this.recorder = awsxRayRecorder;
        this.httpResponseAttributesCollector = httpResponseAttributesCollector;
        this.httpRequestAttributesCollector = httpRequestAttributesCollector;
        this.traceHeaderParser = traceHeaderParser;
        this.segmentNamingStrategy = segmentNamingStrategy;
        this.sampleDecisionParser = sampleDecisionParser;
        this.segmentDecorators = segmentDecorators;
        this.pathMatcher = PathMatcher.ANT;
        this.executorService = executorService;
    }

    @Override
    public int getOrder() {
        return ServerFilterPhase.TRACING.order();
    }

    @Override
    public Publisher<MutableHttpResponse<?>> doFilter(HttpRequest<?> request, ServerFilterChain chain) {
        if (CollectionUtils.isNotEmpty(excludes)) {
            final String path = request.getUri().getPath();
            if (excludes.stream().anyMatch(exclude -> pathMatcher.matches(exclude, path))) {
                if (LOG.isTraceEnabled()) {
                    LOG.trace(" {} {} Excluding request from AWSXRayServletFilter", request.getMethod(), request.getPath());
                }
                return chain.proceed(request);
            }
        }
        if (LOG.isTraceEnabled()) {
            LOG.trace("Tracing request {} {}", request.getMethod(), request.getPath());
        }
        String segmentName = segmentNamingStrategy.resolveName(request).orElse(null);
        if (segmentName == null) {
            if (LOG.isTraceEnabled()) {
                LOG.trace("segment name not resolved");
            }
            return chain.proceed(request);
        }
        SamplingRequest samplingRequest = createSamplingRequest(request, segmentName);
        SamplingStrategy samplingStrategy = recorder.getSamplingStrategy();
        SamplingResponse samplingResponse = samplingStrategy.shouldTrace(samplingRequest);
        TraceHeader.SampleDecision sampleDecision = sampleDecisionParser.sampleDecision(request, samplingResponse);
        Optional<TraceHeader> incomingTraceHeaderOptional = traceHeaderParser.parseTraceHeader(request);
        TraceHeader incomingTraceHeader = incomingTraceHeaderOptional.orElse(null);
        Map<String, Object>  requestAttributes = httpRequestAttributesCollector.requestAttributes(request);

        Segment segment = createSegment(incomingTraceHeader, sampleDecision, samplingResponse, samplingStrategy, requestAttributes, segmentName);
        request.setAttribute(HttpRequestAttributeSegmentContext.XRAY_SEGMENT_RESOLVER, segment);

        return Flux.from(chain.proceed(request))
                .map(mutableHttpResponse -> {
                    Optional<Throwable> objectOptional = mutableHttpResponse.getAttribute(HttpAttributes.EXCEPTION, Throwable.class);
                    if (objectOptional.isPresent()) {
                        Throwable t = objectOptional.get();
                        if (LOG.isTraceEnabled()) {
                            LOG.trace("adding exception to segment", t);
                        }
                        segment.addException(t);
                    }

                    TraceHeader responseTraceHeader = traceHeaderParser.createResponseTraceHeader((Segment) segment, incomingTraceHeader);
                    String responseTraceHeaderString = responseTraceHeader.toString();
                    mutableHttpResponse.getHeaders().add(TraceHeader.HEADER_KEY, responseTraceHeaderString);
                    if (LOG.isTraceEnabled()) {
                        LOG.trace("populating segment with response");
                    }
                    httpResponseAttributesCollector.populateEntityWithResponse(segment, mutableHttpResponse);

                    if (LOG.isTraceEnabled()) {
                        LOG.trace("decorating segment");
                    }
                    for (SegmentDecorator decorator : segmentDecorators) {
                        decorator.decorate(segment, request);
                    }

                    Object body = mutableHttpResponse.body();

                    if (Publishers.isSingle(body.getClass())) {
                        if (LOG.isTraceEnabled()) {
                            LOG.trace("Body is Publisher single");
                        }
                        mutableHttpResponse.body(
                                Mono.from(Publishers.convertPublisher(body, Publisher.class)).doAfterTerminate(() -> closeSegment(segment)));
                        return mutableHttpResponse;
                    } else if (Publishers.isConvertibleToPublisher(body)) {
                        if (LOG.isTraceEnabled()) {
                            LOG.trace("Body isConvertibleToPublisher");
                        }
                        mutableHttpResponse.body(
                                Flux.from(Publishers.convertPublisher(body, Publisher.class)).doAfterTerminate(() -> closeSegment(segment)));
                        return mutableHttpResponse;
                    } else {
                        closeSegment(segment);
                    }

                    return mutableHttpResponse;
                });
    }

    private void closeSegment(Segment segment) {
        if (LOG.isTraceEnabled()) {
            LOG.trace("closing segment");
        }
        segment.close();
    }

    @NonNull
    private SamplingRequest createSamplingRequest(@NonNull HttpRequest<?> httpRequest, @NonNull String segmentName) {
        return new SamplingRequest(
                segmentName,
                getHost(httpRequest).orElse(null),
                httpRequest.getPath(),
                httpRequest.getMethodName(),
                recorder.getOrigin());
    }

    private Optional<String> getHost(HttpRequest<?> request) {
        return Optional.ofNullable(request.getHeaders().get("Host"));
    }

    @NonNull
    private Segment createSegment(@Nullable TraceHeader incomingTraceHeader,
                                  @NonNull TraceHeader.SampleDecision sampleDecision,
                                  @NonNull SamplingResponse samplingResponse,
                                  @NonNull SamplingStrategy samplingStrategy,
                                  @NonNull Map<String, Object> requestAttributes,
                                  @NonNull String segmentName) {
        TraceID traceId = incomingTraceHeader != null ? incomingTraceHeader.getRootTraceId() : null;
        if (LOG.isTraceEnabled()) {
            if (traceId == null) {
                LOG.trace("No incoming trace header received");
            } else {
                LOG.trace("Incoming trace header received: {}", traceId.toString());
            }
        }
        String parentId = incomingTraceHeader != null ? incomingTraceHeader.getParentId() : null;
        if (LOG.isTraceEnabled() && parentId != null) {
            LOG.trace("Trace parent Id: {}", parentId);
        }
        final Segment created;
        if (TraceHeader.SampleDecision.SAMPLED.equals(sampleDecision)) {
            created = traceId != null
                    ? recorder.beginSegment(segmentName, traceId, parentId)
                    : recorder.beginSegment(segmentName);
            if (samplingResponse.getRuleName().isPresent()) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Sampling strategy decided to use rule named: {}.", samplingResponse.getRuleName().get());
                }
                created.setRuleName(samplingResponse.getRuleName().get());
            }
        } else { //NOT_SAMPLED
            if (samplingStrategy.isForcedSamplingSupported()) {
                created = traceId != null
                        ? recorder.beginSegment(segmentName, traceId, parentId)
                        : recorder.beginSegment(segmentName);
                created.setSampled(false);
            } else {
                if (LOG.isTraceEnabled()) {
                    LOG.trace("Creating Dummy Segment");
                }
                created = traceId != null ? recorder.beginNoOpSegment(traceId) : recorder.beginNoOpSegment();
            }
        }
        created.putHttp("request", requestAttributes);
        return created;
    }
}
