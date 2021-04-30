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
package io.micronaut.aws.xray.sdkclients;

import com.amazonaws.xray.AWSXRay;
import com.amazonaws.xray.entities.Entity;
import com.amazonaws.xray.interceptors.TracingInterceptor;
import io.micronaut.aws.xray.filters.server.XRayHttpServerFilter;
import io.micronaut.http.context.ServerRequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.core.interceptor.Context;
import software.amazon.awssdk.core.interceptor.ExecutionAttributes;

/**
 * Sets the Trace Entity context before executing the interceptor.
 *
 * @author Sergio del Amo
 * @since 2.7.0
 */
public class RequestAttributeTracingInterceptor extends TracingInterceptor {
    private static final Logger LOG = LoggerFactory.getLogger(RequestAttributeTracingInterceptor.class);

    private Entity currentContext;

    @Override
    public void beforeExecution(Context.BeforeExecution context, ExecutionAttributes executionAttributes) {
        if (LOG.isTraceEnabled()) {
            LOG.trace("Override beforeExecution");
        }
        currentContext = AWSXRay.getGlobalRecorder().getTraceEntity();
        ServerRequestContext.currentRequest()
                .flatMap(httpRequest -> httpRequest.getAttribute(XRayHttpServerFilter.ATTRIBUTE_X_RAY_TRACE_ENTITY, Entity.class))
                .ifPresent(traceEntity -> {
                    if (LOG.isTraceEnabled()) {
                        LOG.trace("setting trace entity");
                    }
                    AWSXRay.getGlobalRecorder().setTraceEntity(traceEntity);
                });
        super.beforeExecution(context, executionAttributes);
    }

    @Override
    public void afterExecution(Context.AfterExecution context, ExecutionAttributes executionAttributes) {
        super.afterExecution(context, executionAttributes);
        AWSXRay.getGlobalRecorder().setTraceEntity(currentContext);
    }

}
