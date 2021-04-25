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
package io.micronaut.aws.xray.client;

import com.amazonaws.xray.AWSXRayRecorder;
import com.amazonaws.xray.entities.Subsegment;
import com.amazonaws.xray.entities.TraceHeader;
import io.micronaut.core.async.publisher.Publishers;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MutableHttpRequest;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;

/**
 * AWS X-Ray {@link Publisher} that creates new subsegment for new http requests.
 *
 * @author Pavol Gressa
 * @since 2.7.0
 */
public class XRayClientTracingPublisher implements Publishers.MicronautPublisher<HttpResponse<?>> {

    public static final Logger LOG = LoggerFactory.getLogger(XRayClientTracingPublisher.class);

    private final Publisher<? extends HttpResponse<?>> publisher;
    private final MutableHttpRequest<?> request;
    private final AWSXRayRecorder recorder;
    private Subsegment subsegment;

    public XRayClientTracingPublisher(
            @NotNull MutableHttpRequest<?> request,
            @NotNull Publisher<? extends HttpResponse<?>> publisher,
            @NotNull AWSXRayRecorder recorder) {
        this.request = request;
        this.publisher = publisher;
        this.recorder = recorder;
    }

    @Override
    public void subscribe(Subscriber<? super HttpResponse<?>> actual) {
        String subsegmentName = getSubsegmentName(request);
        initSubsegment(subsegmentName);

        publisher.subscribe(new Subscriber<HttpResponse<?>>() {

            @Override
            public void onSubscribe(Subscription s) {
                actual.onSubscribe(s);
            }

            @Override
            public void onNext(HttpResponse<?> httpResponse) {
                try {
                    configureSubsegmentResponse(subsegment, httpResponse);
                } catch (Exception e) {
                    if (subsegment != null) {
                        if (LOG.isWarnEnabled()) {
                            LOG.warn(String.format("Failed to configure subsegment '%s' on success response", subsegment.getName()), e);
                        }
                        subsegment.addException(e);
                    }
                } finally {
                    endSubsegmentSafe(subsegment);
                }
                actual.onNext(httpResponse);
            }

            @Override
            public void onError(Throwable t) {
                try {
                    if (subsegment != null) {
                        subsegment.addException(t);
                    }
                } finally {
                    endSubsegmentSafe(subsegment);
                }
                actual.onError(t);
            }

            @Override
            public void onComplete() {
                actual.onComplete();
            }
        });
    }

    private void endSubsegmentSafe(Subsegment subsegment) {
        try {
            if (subsegment != null) {
                subsegment.run(recorder::endSubsegment, recorder);
            }
        } catch (Exception e) {
            if (LOG.isWarnEnabled()) {
                LOG.warn(String.format("Failed to end subsegment '%s' when handling error response", subsegment.getName()), e);
            }
        }
    }

    private void initSubsegment(String subsegmentName) {
        try {
            subsegment = recorder.beginSubsegment(subsegmentName);
            configureSubsegmentRequest(subsegment, request);
        } catch (Exception e) {
            if (LOG.isWarnEnabled()) {
                LOG.warn(String.format("Failed to configure subsegment '%s'", subsegmentName), e);
            }
            if (subsegment != null) {
                subsegment.addException(e);
            }
        }
    }

    private String getSubsegmentName(MutableHttpRequest<?> request) {
        return request.getServerAddress().toString();
    }

    private void configureSubsegmentRequest(Subsegment subsegment, MutableHttpRequest<?> request) {
        boolean isSampled = subsegment.getParentSegment().isSampled();
        TraceHeader header = new TraceHeader(
                subsegment.getParentSegment().getTraceId(),
                isSampled ? subsegment.getId() : null,
                isSampled ? TraceHeader.SampleDecision.SAMPLED : TraceHeader.SampleDecision.NOT_SAMPLED
        );

        request.header(TraceHeader.HEADER_KEY, header.toString());
        Map<String, Object> requestInformation = new HashMap<>();

        requestInformation.put("url", request.getUri().toString());
        requestInformation.put("method", request.getMethodName());
        subsegment.putHttp("request", requestInformation);
    }

    private void configureSubsegmentResponse(Subsegment subsegment, HttpResponse<?> httpResponse) {
        Map<String, Object> responseAttributes = new HashMap<>();
        int responseCode = httpResponse.getStatus().getCode();
        switch (responseCode / 100) {
            case 4:
                subsegment.setError(true);
                if (responseCode == 429) {
                    subsegment.setThrottle(true);
                }
                break;
            case 5:
                subsegment.setFault(true);
                break;
            default:
                break;
        }
        responseAttributes.put("status", responseCode);
        long contentLength = httpResponse.getContentLength();
        responseAttributes.put("content_length", contentLength);
        subsegment.putHttp("response", responseAttributes);
    }
}
