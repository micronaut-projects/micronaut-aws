/*
 * Copyright 2017-2023 original authors
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

    protected abstract MicronautAwsRequestTransformer<RequestType, ?> requestTransformer();

    protected abstract MicronautAwsResponseTransformer<ResponseType> responseTransformer();
}
