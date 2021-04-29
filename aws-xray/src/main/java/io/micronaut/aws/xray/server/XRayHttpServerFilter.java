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
import com.amazonaws.xray.entities.Entity;
import com.amazonaws.xray.entities.Segment;
import com.amazonaws.xray.entities.TraceHeader;
import com.amazonaws.xray.entities.TraceID;
import com.amazonaws.xray.exceptions.SegmentNotFoundException;
import com.amazonaws.xray.strategy.sampling.SamplingRequest;
import com.amazonaws.xray.strategy.sampling.SamplingResponse;
import com.amazonaws.xray.strategy.sampling.SamplingStrategy;
import io.micronaut.aws.xray.configuration.XRayConfiguration;
import io.micronaut.aws.xray.strategy.SegmentNamingStrategy;
import io.micronaut.context.annotation.Requires;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.util.AntPathMatcher;
import io.micronaut.core.util.CollectionUtils;
import io.micronaut.core.util.PathMatcher;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.annotation.Filter;
import io.micronaut.http.filter.OncePerRequestHttpServerFilter;
import io.micronaut.http.filter.ServerFilterChain;
import io.micronaut.http.filter.ServerFilterPhase;
import io.reactivex.Flowable;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * <p>
 * <p><b>N.B.</b>: This class was forked from AWSXRayServletFilter AWS X-Ray Java SDK with modifications.</p>
 * <p>
 * <p>As per the Apache 2.0 license, the original copyright notice and all author and copyright information have
 * remained intact.</p>
 *
 * @author Sergio del Amo
 * @since 2.7.0
 */
@Filter(Filter.MATCH_ALL_PATTERN)
@Requires(beans = AWSXRayRecorder.class)
@Requires(beans = SegmentNamingStrategy.class)
public class XRayHttpServerFilter extends OncePerRequestHttpServerFilter {

    private static final Logger LOG = LoggerFactory.getLogger(XRayHttpServerFilter.class);
    public static final String ATTRIBUTE_X_RAY_TRACE_ENTITY = "X_RAY_TRACE_ENTITY";

    private final AntPathMatcher pathMatcher;

    private final AWSXRayRecorder recorder;

    private final HttpRequestAttributesBuilder httpRequestAttributesBuilder;

    private final HttpResponseAttributesBuilder httpResponseAttributesBuilder;

    private final SegmentNamingStrategy segmentNamingStrategy;

    @Nullable
    private final List<String> excludes;

    public XRayHttpServerFilter(@NonNull XRayConfiguration xRayConfiguration,
                                @NonNull AWSXRayRecorder awsxRayRecorder,
                                @NonNull HttpResponseAttributesBuilder httpResponseAttributesBuilder,
                                @NonNull HttpRequestAttributesBuilder httpRequestAttributesBuilder,
                                SegmentNamingStrategy segmentNamingStrategy) {
        this.excludes = xRayConfiguration.getExcludes().orElse(Collections.emptyList());
        this.recorder = awsxRayRecorder;
        this.httpResponseAttributesBuilder = httpResponseAttributesBuilder;
        this.httpRequestAttributesBuilder = httpRequestAttributesBuilder;
        this.segmentNamingStrategy = segmentNamingStrategy;
        this.pathMatcher = PathMatcher.ANT;
    }

    @Override
    public int getOrder() {
        return ServerFilterPhase.TRACING.order();
    }

    @Override
    protected Publisher<MutableHttpResponse<?>> doFilterOnce(HttpRequest<?> request, ServerFilterChain chain) {
        if (CollectionUtils.isNotEmpty(excludes)) {
            final String path = request.getUri().getPath();
            if (excludes.stream().anyMatch(exclude -> pathMatcher.matches(exclude, path))) {
                if (LOG.isTraceEnabled()) {
                    //LOG.trace(" {} {} Excluding request from AWSXRayServletFilter", request.getMethod(), request.getPath());
                }
                return chain.proceed(request);
            }
        }
        if (LOG.isTraceEnabled()) {
            LOG.trace("Tracing request {} {}", request.getMethod(), request.getPath());
        }
        String segmentName = segmentNamingStrategy.nameForRequest(request);
        SamplingRequest samplingRequest = createSamplingRequest(request, segmentName);
        SamplingResponse samplingResponse = recorder.getSamplingStrategy().shouldTrace(samplingRequest);
        SamplingStrategy samplingStrategy = recorder.getSamplingStrategy();
        TraceHeader.SampleDecision sampleDecision = sampleDecision(request, samplingResponse);
        Optional<TraceHeader> incomingTraceHeaderOptional = getTraceHeader(request);
        TraceHeader incomingTraceHeader = incomingTraceHeaderOptional.orElse(null);
        Map<String, Object>  requestAttributes = httpRequestAttributesBuilder.requestAttributes(request);

        final Segment segment = createSegment(incomingTraceHeader, sampleDecision, samplingResponse, samplingStrategy, requestAttributes, segmentName);
        final Entity context = recorder.getTraceEntity();
        request.setAttribute(ATTRIBUTE_X_RAY_TRACE_ENTITY, context);

        return Flowable.fromPublisher(chain.proceed(request))
                .doOnError(t -> {
                    try {
                        segment.addException(t);
                    } catch (SegmentNotFoundException e) {
                        if (LOG.isWarnEnabled()) {
                            LOG.warn("segment not found onError");
                        }
                    }
                }).doFinally(() -> {
                    try {
                        Entity currentContext = recorder.getTraceEntity();
                        recorder.setTraceEntity(context);
                        segment.close();
                        recorder.setTraceEntity(currentContext);
                    } catch (SegmentNotFoundException e) {
                        if (LOG.isWarnEnabled()) {
                            LOG.warn("Cloud not close segment because segment {} was not found", segment.getName());
                        }
                    }

                })
                .map(mutableHttpResponse -> {
                    TraceHeader responseTraceHeader = createResponseTraceHeader(incomingTraceHeader, segment);
                    final String traceHeader = responseTraceHeader.toString();
                    mutableHttpResponse.getHeaders().add(TraceHeader.HEADER_KEY, traceHeader);
                    httpResponseAttributesBuilder.putHttpResponseInformation(segment, mutableHttpResponse);
                    return mutableHttpResponse;
                });
    }

    private Optional<TraceHeader> getTraceHeader(HttpRequest<?> request) {
        String traceHeaderString = request.getHeaders().get(TraceHeader.HEADER_KEY);
        if (null != traceHeaderString) {
            return Optional.of(TraceHeader.fromString(traceHeaderString));
        }
        return Optional.empty();
    }

    @NonNull
    private TraceHeader.SampleDecision sampleDecision(@NonNull HttpRequest<?> request,
                                                      @NonNull SamplingResponse samplingResponse) {
        Optional<TraceHeader> incomingHeader = getTraceHeader(request);
        TraceHeader.SampleDecision sampleDecision = incomingHeader.map(TraceHeader::getSampled)
                .orElseGet(() -> getSampleDecision(samplingResponse));
        if (TraceHeader.SampleDecision.REQUESTED.equals(sampleDecision) || TraceHeader.SampleDecision.UNKNOWN.equals(sampleDecision)) {
            return getSampleDecision(samplingResponse);
        }
        return sampleDecision;
    }

    private TraceHeader.SampleDecision getSampleDecision(@NonNull SamplingResponse sample) {
        if (sample.isSampled()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Sampling strategy decided SAMPLED.");
            }
            return TraceHeader.SampleDecision.SAMPLED;
        } else {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Sampling strategy decided NOT_SAMPLED.");
            }
            return TraceHeader.SampleDecision.NOT_SAMPLED;
        }
    }

    @NonNull
    private SamplingRequest createSamplingRequest(@NonNull HttpRequest<?> httpRequest, String segmentName) {
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
    private Segment createSegment(TraceHeader incomingTraceHeader,
                               TraceHeader.SampleDecision sampleDecision,
                               SamplingResponse samplingResponse,
                               SamplingStrategy samplingStrategy,
                               Map<String, Object> requestAttributes,
                               String segmentName) {
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

    @NonNull
    private TraceHeader createResponseTraceHeader(@Nullable TraceHeader incomingHeader,
                                                  @NonNull Segment created) {
        final TraceHeader responseHeader;
        if (incomingHeader != null) {
            // create a new header, and use the incoming header so we know what to do in regards to sending back the sampling
            // decision.
            responseHeader = new TraceHeader(created.getTraceId());
            if (TraceHeader.SampleDecision.REQUESTED == incomingHeader.getSampled()) {
                responseHeader.setSampled(created.isSampled() ? TraceHeader.SampleDecision.SAMPLED : TraceHeader.SampleDecision.NOT_SAMPLED);
            }
        } else {
            // Create a new header, we're the tracing root. We wont return the sampling decision.
            responseHeader = new TraceHeader(created.getTraceId());
        }
        return responseHeader;
    }
}
