package io.micronaut.aws.xray.recorder;

import com.amazonaws.xray.contexts.SegmentContext;
import com.amazonaws.xray.contexts.SegmentContextResolverChain;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.context.ServerRequestContext;
import jakarta.inject.Singleton;

import java.util.Optional;

@Singleton
public class ReactorSegmentContextResolverChain extends SegmentContextResolverChain {

    public static final String XRAY_SEGMENT_RESOLVER = "XRAY-SEGMENT-RESOLVER";

    @Override
    public SegmentContext resolve() {
        Optional<HttpRequest<Object>> requestOptional = ServerRequestContext.currentRequest();
        if (!requestOptional.isPresent()) {
            return super.resolve();
        }
        HttpRequest<Object> request = requestOptional.get();
        Optional<SegmentContext> segmentContextOptional = request.getAttribute(XRAY_SEGMENT_RESOLVER, SegmentContext.class);
        if (segmentContextOptional.isPresent()) {
            return super.resolve();
        }
        return segmentContextOptional.get();
    }

}
