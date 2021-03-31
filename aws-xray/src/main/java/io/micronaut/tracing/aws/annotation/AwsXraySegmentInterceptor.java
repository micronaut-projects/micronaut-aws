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
package io.micronaut.tracing.aws.annotation;

import com.amazonaws.xray.AWSXRayRecorder;
import com.amazonaws.xray.entities.Segment;
import com.amazonaws.xray.entities.Subsegment;
import io.micronaut.aop.InterceptPhase;
import io.micronaut.aop.MethodInterceptor;
import io.micronaut.aop.MethodInvocationContext;
import io.micronaut.context.annotation.BootstrapContextCompatible;
import io.micronaut.context.annotation.Requires;
import io.micronaut.core.annotation.AnnotationValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.util.Optional;

/**
 * {@link MethodInterceptor} that instruments {@link AwsXraySegment} and {@link AwsXraySubsegment}.
 *
 * @author Pavol Gressa
 * @since 2.5
 */
@Requires(beans = AWSXRayRecorder.class)
@BootstrapContextCompatible
@Singleton
public class AwsXraySegmentInterceptor implements MethodInterceptor<Object, Object> {
    private static final Logger LOG = LoggerFactory.getLogger(AwsXraySegmentInterceptor.class);

    private final AWSXRayRecorder awsxRayRecorder;

    public AwsXraySegmentInterceptor(AWSXRayRecorder awsxRayRecorder) {
        this.awsxRayRecorder = awsxRayRecorder;
    }

    private Object handleNewSegment(MethodInvocationContext<Object, Object> context) {
        AnnotationValue<AwsXraySegment> annotation = context.getAnnotation(AwsXraySegment.class);
        String name = annotation.stringValue("name").orElse(context.getMethodName());

        Segment segment;

        Optional<Segment> parentSegmentOptional = awsxRayRecorder.getCurrentSegmentOptional();
        if (parentSegmentOptional.isPresent()) {
            Segment parentSegment = parentSegmentOptional.get();
            segment = awsxRayRecorder.beginSegment(name, parentSegment.getTraceId(), parentSegment.getParentId());
        } else {
            segment = awsxRayRecorder.beginSegment(name);
        }
        LOG.trace(String.format("Created segment '%s'", name));

        Optional<String> namespace = annotation.stringValue("namespace");
        namespace.ifPresent(segment::setNamespace);
        try {
            return context.proceed();
        } catch (Exception e) {
            segment.addException(e);
            throw e;
        } finally {
            awsxRayRecorder.endSegment();
            LOG.trace(String.format("Closed segment '%s'", name));
        }
    }

    private Object handleNewSubsegment(MethodInvocationContext<Object, Object> context) {
        AnnotationValue<AwsXraySubsegment> annotation = context.getAnnotation(AwsXraySubsegment.class);
        String name = annotation.stringValue("name").orElse(context.getMethodName());
        Subsegment subsegment = awsxRayRecorder.beginSubsegment(name);
        LOG.trace(String.format("Created subsegment '%s'", name));
        try {
            return context.proceed();
        } catch (Exception e) {
            subsegment.addException(e);
            throw e;
        } finally {
            awsxRayRecorder.endSubsegment();
            LOG.trace(String.format("Closed subsegment '%s'", name));
        }
    }

    @Override
    public Object intercept(MethodInvocationContext<Object, Object> context) {
        if (context.hasAnnotation(AwsXraySegment.class)) {
            return handleNewSegment(context);
        } else if (context.hasAnnotation(AwsXraySubsegment.class)) {
            return handleNewSubsegment(context);
        } else {
            return context.proceed();
        }
    }

    @Override
    public int getOrder() {
        return InterceptPhase.TRACE.getPosition();
    }
}
