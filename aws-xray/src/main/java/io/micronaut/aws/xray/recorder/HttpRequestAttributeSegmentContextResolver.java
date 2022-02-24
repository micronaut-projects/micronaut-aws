package io.micronaut.aws.xray.recorder;

import com.amazonaws.xray.contexts.SegmentContext;
import com.amazonaws.xray.contexts.SegmentContextResolver;
import io.micronaut.http.context.ServerRequestContext;

public class HttpRequestAttributeSegmentContextResolver implements SegmentContextResolver {

    @Override
    public SegmentContext resolve() {
        return ServerRequestContext.currentRequest().isPresent() ?
                new HttpRequestAttributeSegmentContext() : null;
    }
}
