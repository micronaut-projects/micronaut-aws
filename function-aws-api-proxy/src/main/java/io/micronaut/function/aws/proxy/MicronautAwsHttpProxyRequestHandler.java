package io.micronaut.function.aws.proxy;

import java.util.stream.Stream;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.core.annotation.TypeHint;
import io.micronaut.function.aws.MicronautRequestHandler;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.server.RouteExecutor;
import io.micronaut.web.router.Router;
import io.micronaut.web.router.UriRouteMatch;
import jakarta.inject.Inject;
import reactor.core.publisher.Flux;

@TypeHint(
    accessType = {TypeHint.AccessType.ALL_DECLARED_CONSTRUCTORS, TypeHint.AccessType.ALL_PUBLIC},
    value = MicronautAwsHttpProxyRequestHandler.class
)
@Introspected(
    visibility = Introspected.Visibility.PUBLIC
)
public abstract class MicronautAwsHttpProxyRequestHandler<RequestType, ResponseType>
    extends MicronautRequestHandler<RequestType, ResponseType> {

    @Inject
    private RouteExecutor routeExecutor;

    @Inject
    private Router router;

    protected MicronautAwsHttpProxyRequestHandler() {
    }

    @Override
    public ResponseType execute(final RequestType input) {
        MicronautAwsRequest<?> containerRequest = requestTransformer().toMicronautRequest(input);

        Stream<UriRouteMatch<Object, Object>> uriRouteMatchStream = router.find(containerRequest);

        Flux<MutableHttpResponse<?>> mutableHttpResponseFlux =
            routeExecutor.executeRoute(containerRequest, true, Flux.fromStream(uriRouteMatchStream));

        HttpResponse<?> response = mutableHttpResponseFlux
            .single()
            .block();

        return responseTransformer().toAwsResponse(response);
    }

    protected abstract MicronautAwsRequestTransformer<RequestType, ? extends HttpRequest<?>> requestTransformer();

    protected abstract MicronautAwsResponseTransformer<ResponseType> responseTransformer();
}
