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

import com.amazonaws.xray.AWSXRay;
import com.amazonaws.xray.AWSXRayRecorder;
import com.amazonaws.xray.entities.Entity;
import com.amazonaws.xray.entities.Segment;
import com.amazonaws.xray.entities.Subsegment;
import io.micronaut.aop.InterceptPhase;
import io.micronaut.aop.InterceptorBean;
import io.micronaut.aop.MethodInterceptor;
import io.micronaut.aop.MethodInvocationContext;
import io.micronaut.aws.xray.filters.server.XRayHttpServerFilter;
import io.micronaut.context.annotation.Requires;
import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.http.context.ServerRequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.inject.Singleton;
import java.util.Optional;

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
        String name = annotation.stringValue("name").orElse(context.getMethodName());
        if (LOG.isTraceEnabled()) {
            LOG.trace("Annotation AwsXraySubsegment name {}. ", name);
        }
        Entity currentContext = recorder.getTraceEntity();
        ServerRequestContext.currentRequest()
                .flatMap(httpRequest -> httpRequest.getAttribute(XRayHttpServerFilter.ATTRIBUTE_X_RAY_TRACE_ENTITY, Entity.class))
                .ifPresent(AWSXRay::setTraceEntity);
        Optional<Segment> segmentOptional = recorder.getCurrentSegmentOptional();
        if (segmentOptional.isPresent()) {
            Object result = wrapContextInSubsegment(context, name);
            recorder.setTraceEntity(currentContext);
            return result;
        }
        if (LOG.isTraceEnabled()) {
            LOG.trace("Could not created subsegment {}. No segment found", name);
        }
        return context.proceed();
    }


    /**
     *
     * @param context The context
     * @param name subsegment's name
     * @return The result
     */
    public Object wrapContextInSubsegment(MethodInvocationContext<Object, Object> context, @NonNull String name) {
        Subsegment subsegment = recorder.beginSubsegment(name);
        if (LOG.isTraceEnabled()) {
            LOG.trace("Created subsegment {}", name);
        }
        try {
            return context.proceed();
        } catch (Exception e) {
            subsegment.addException(e);
            throw e;
        } finally {
            recorder.endSubsegment();
            if (LOG.isTraceEnabled()) {
                LOG.trace("Closed subsegment {}", name);
            }
        }
    }

    @Override
    public int getOrder() {
        return InterceptPhase.TRACE.getPosition();
    }
}
