package io.micronaut.aws.xray.decorators;

import com.amazonaws.xray.AWSXRayRecorder;
import com.amazonaws.xray.entities.Entity;
import com.amazonaws.xray.entities.Segment;
import io.micronaut.aws.xray.server.XRayHttpServerFilter;
import io.micronaut.aws.xray.strategy.SegmentNamingStrategy;
import io.micronaut.context.annotation.Requires;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.annotation.Filter;
import io.micronaut.http.filter.OncePerRequestHttpServerFilter;
import io.micronaut.http.filter.ServerFilterChain;
import io.micronaut.http.filter.ServerFilterPhase;
import org.reactivestreams.Publisher;
import javax.inject.Singleton;
import java.util.List;

@Filter(Filter.MATCH_ALL_PATTERN)
@Requires(beans = AWSXRayRecorder.class)
public class SegmentDecorationFilter extends OncePerRequestHttpServerFilter {

    private final List<SegmentDecorator> segmentDecorators;
    private final AWSXRayRecorder awsxRayRecorder;

    public SegmentDecorationFilter(List<SegmentDecorator> segmentDecorators,
                                   AWSXRayRecorder awsxRayRecorder) {
        this.segmentDecorators = segmentDecorators;
        this.awsxRayRecorder = awsxRayRecorder;
    }

    @Override
    public int getOrder() {
        return ServerFilterPhase.LAST.order();
    }

    @Override
    protected Publisher<MutableHttpResponse<?>> doFilterOnce(HttpRequest<?> request, ServerFilterChain chain) {
        request.getAttribute(XRayHttpServerFilter.ATTRIBUTE_X_RAY_TRACE_ENTITY, Entity.class).ifPresent(traceEntity -> {
            awsxRayRecorder.setTraceEntity(traceEntity);
            awsxRayRecorder.getCurrentSegmentOptional().ifPresent(segment -> {
                for (SegmentDecorator decorator : segmentDecorators) {
                    decorator.decorate(segment, request);
                }
            });
        });
        return chain.proceed(request);
    }
}
