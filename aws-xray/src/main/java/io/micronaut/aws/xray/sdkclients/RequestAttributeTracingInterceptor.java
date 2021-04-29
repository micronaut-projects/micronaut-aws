package io.micronaut.aws.xray.sdkclients;

import com.amazonaws.xray.AWSXRay;
import com.amazonaws.xray.entities.Entity;
import com.amazonaws.xray.interceptors.TracingInterceptor;
import io.micronaut.aws.xray.server.XRayHttpServerFilter;
import io.micronaut.http.context.ServerRequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.core.interceptor.Context;
import software.amazon.awssdk.core.interceptor.ExecutionAttributes;

public class RequestAttributeTracingInterceptor extends TracingInterceptor {
    private static final Logger LOG = LoggerFactory.getLogger(RequestAttributeTracingInterceptor.class);

    @Override
    public void beforeExecution(Context.BeforeExecution context, ExecutionAttributes executionAttributes) {
        if (LOG.isTraceEnabled()) {
            LOG.trace("Override beforeExecution");
        }
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

}
