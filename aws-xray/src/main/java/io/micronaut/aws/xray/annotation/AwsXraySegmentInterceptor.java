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
package io.micronaut.aws.xray.annotation;

import com.amazonaws.xray.AWSXRayRecorder;
import com.amazonaws.xray.entities.Entity;
import com.amazonaws.xray.entities.Segment;
import com.amazonaws.xray.entities.Subsegment;
import io.micronaut.aop.*;
import io.micronaut.context.annotation.Requires;
import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.async.publisher.Publishers;
import io.micronaut.core.type.Argument;
import io.micronaut.core.type.ReturnType;
import jakarta.inject.Singleton;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * {@link MethodInterceptor} that instruments {@link AwsXraySubsegment}.
 *
 * @author Pavol Gressa
 * @since 2.7.0
 */
@Requires(beans = AWSXRayRecorder.class)
@Singleton
@InterceptorBean(AwsXraySubsegment.class)
public class AwsXraySegmentInterceptor implements MethodInterceptor<Object, Object> {
    private static final Logger LOG = LoggerFactory.getLogger(AwsXraySegmentInterceptor.class);

    private final AWSXRayRecorder recorder;

    public AwsXraySegmentInterceptor(AWSXRayRecorder recorder) {
        this.recorder = recorder;
    }

    @Override
    public Object intercept(MethodInvocationContext<Object, Object> context) {
        Optional<AnnotationValue<AwsXraySubsegment>> opt = context.findAnnotation(AwsXraySubsegment.class);
        if (!opt.isPresent()) {
            return context.proceed();
        }

        AnnotationValue<AwsXraySubsegment> annotation = opt.get();
        String subSegmentName = annotation.stringValue("name").orElse(context.getMethodName());
        if (LOG.isTraceEnabled()) {
            LOG.trace("Annotation AwsXraySubsegment name {}. ", subSegmentName);
        }
        Entity currentContext = recorder.getTraceEntity();

        Optional<Segment> segmentOptional = recorder.getCurrentSegmentOptional();
        if (!segmentOptional.isPresent()) {
            if (LOG.isTraceEnabled()) {
                LOG.trace("Could not created subsegment {}. No segment found", subSegmentName);
            }
            return context.proceed();
        }
        return wrapContextInSubsegment(context, subSegmentName);
    }

    /**
     *
     * @param context The context
     * @param subSegmentName subsegment's name
     * @return The result
     */
    public Object wrapContextInSubsegment(MethodInvocationContext<Object, Object> context, @NonNull String subSegmentName) {
        Subsegment subsegment = recorder.beginSubsegment(subSegmentName);
        if (LOG.isTraceEnabled()) {
            LOG.trace("Created subsegment {}", subSegmentName);
        }
        InterceptedMethod interceptedMethod = InterceptedMethod.of(context);
        try {
            switch (interceptedMethod.resultType()) {
                case PUBLISHER:
                    return interceptedMethod.handleResult(Flux.from(interceptedMethod.interceptResultAsPublisher())
                        .doOnError(subsegment::addException)
                    );
                case COMPLETION_STAGE:
                    return interceptedMethod.interceptResultAsCompletionStage().whenComplete((o, t) -> {
                        if ( t!= null) {
                            subsegment.addException(t);
                        }
                    });
                case SYNCHRONOUS:
                    return context.proceed();
                default:
                    return interceptedMethod.unsupported();
            }
        } catch (Exception e) {
            subsegment.addException(e);
            return interceptedMethod.handleException(e);
        } finally {
            recorder.endSubsegment();
        }

    }

    @Override
    public int getOrder() {
        return InterceptPhase.TRACE.getPosition();
    }
}
