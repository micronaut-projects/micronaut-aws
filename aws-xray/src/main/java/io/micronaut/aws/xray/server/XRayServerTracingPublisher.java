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

import com.amazonaws.xray.entities.Segment;
import com.amazonaws.xray.javax.servlet.AWSXRayServletFilter;
import edu.umd.cs.findbugs.annotations.NonNull;
import io.micronaut.core.async.publisher.Publishers;
import io.micronaut.http.MutableHttpResponse;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;

/**
 * This publisher goal is to re-use existing AWS X-Ray {@link AWSXRayServletFilter} filter that has implemented logic
 * for processing http requests. However provided {@link AWSXRayServletFilter} is built on top of the {@code javax.servlet}
 * {@link javax.servlet.Filter} what complicates the integration into Micronaut which http engine is built on top of
 * abstract layer which makes the implementation of filters independent on actual type of server. For that purpose this
 * class works with {@link io.micronaut.http.HttpRequest} and {@link io.micronaut.http.HttpResponse} adapters for {@code javax.servlet}
 * while using the logic of the provided {@link AWSXRayServletFilter}.
 *
 * @author Pavol Gressa
 * @since 2.7.0
 */
@SuppressWarnings("PublisherImplementation")
public class XRayServerTracingPublisher implements Publishers.MicronautPublisher<MutableHttpResponse<?>> {
    public static final Logger LOG = LoggerFactory.getLogger(XRayServerTracingPublisher.class);

    private final Publisher<MutableHttpResponse<?>> publisher;
    private final SegmentRequestContext segmentRequestContext;
    private final AWSXRayServletFilter delegate;

    public XRayServerTracingPublisher(
            @NotNull Publisher<MutableHttpResponse<?>> publisher,
            @NotNull SegmentRequestContext segmentRequestContext,
            @NotNull AWSXRayServletFilter awsxRayServletFilter) {
        this.publisher = publisher;
        this.delegate = awsxRayServletFilter;
        this.segmentRequestContext = segmentRequestContext;
    }

    @Override
    public void subscribe(Subscriber<? super MutableHttpResponse<?>> actual) {
        //noinspection SubscriberImplementation
        publisher.subscribe(new Subscriber<MutableHttpResponse<?>>() {

            @Override
            public void onSubscribe(Subscription s) {
                XRayServerTracingPublisher.this.doOnSubscribe(segmentRequestContext);
                actual.onSubscribe(s);
            }

            @Override
            public void onNext(MutableHttpResponse<?> mutableHttpResponse) {
                Segment segment = segmentRequestContext.getSegment();
                if (segment != null) {
                    try {
                        // merge prefiltered response with resposne
                        segmentRequestContext.getMutableHttpResponse().getHeaders().forEachValue(mutableHttpResponse::header);

                        // process post filter in original filter
                        delegate.postFilter(segmentRequestContext.getHttpRequestAdapter(), new SegmentRequestContext.HttpResponseAdapter(mutableHttpResponse));
                    } catch (Exception e) {
                        LOG.warn(String.format("Failed to process segment '%s'", segment.getName()));
                    }
                }
                XRayServerTracingPublisher.this.doOnNext(mutableHttpResponse, segmentRequestContext);
                actual.onNext(mutableHttpResponse);
            }

            @Override
            public void onError(Throwable t) {
                Segment segment = segmentRequestContext.getSegment();
                if (segment != null) {
                    segment.addException(t);
                }
                XRayServerTracingPublisher.this.doOnError(t, segmentRequestContext);
                actual.onError(t);
            }

            @Override
            public void onComplete() {
                XRayServerTracingPublisher.this.doOnFinish(segmentRequestContext);
                actual.onComplete();
            }
        });
    }

    /**
     * Designed for subclasses to override and implement custom behaviour when an item is emitted.
     *
     * @param object The object
     * @param segmentRequestContext The segment context
     */
    protected void doOnNext(@NonNull MutableHttpResponse<?> object, @NonNull SegmentRequestContext segmentRequestContext) {
        // no-op
    }

    /**
     * Designed for subclasses to override and implement custom on subscribe behaviour.
     *
     * @param segmentRequestContext The segment context
     */
    protected void doOnSubscribe(@NonNull SegmentRequestContext segmentRequestContext) {
        // no-op
    }

    /**
     * Designed for subclasses to override and implement custom on finish behaviour.
     *
     * @param segmentRequestContext The segment context
     */
    @SuppressWarnings("WeakerAccess")
    protected void doOnFinish(@NonNull SegmentRequestContext segmentRequestContext) {
        // no-op
    }

    /**
     * Designed for subclasses to override and implement custom on error behaviour.
     * @param throwable The throwable
     * @param segmentRequestContext The segment context
     */
    protected void doOnError(@NonNull Throwable throwable, @NonNull SegmentRequestContext segmentRequestContext) {
        // no-op
    }
}
