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

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.ApplicationContextBuilder;
import io.micronaut.function.aws.MicronautRequestHandler;
import io.micronaut.function.aws.proxy.transformer.restgw.MicronautApiGatewayRequestTransformer;
import io.micronaut.function.aws.proxy.transformer.restgw.MicronautApiGatewayResponseTransformer;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.server.RouteExecutor;
import io.micronaut.serde.annotation.SerdeImport;
import io.micronaut.web.router.Router;
import io.micronaut.web.router.UriRouteMatch;
import jakarta.inject.Inject;
import reactor.core.publisher.Flux;

@SerdeImport(APIGatewayProxyRequestEvent.class)
@SerdeImport(APIGatewayProxyRequestEvent.ProxyRequestContext.class)
@SerdeImport(APIGatewayProxyRequestEvent.RequestIdentity.class)
@SerdeImport(APIGatewayProxyResponseEvent.class)
public class MicronautApiGatewayV1ContainerHandler extends MicronautRequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

  @Inject
  private MicronautApiGatewayRequestTransformer requestTransformer;

  @Inject
  private MicronautApiGatewayResponseTransformer<?> responseTransformer;

  @Inject
  private RouteExecutor routeExecutor;

  @Inject
  private Router router;

  public MicronautApiGatewayV1ContainerHandler() {
    this(ApplicationContext.builder());
  }

  public MicronautApiGatewayV1ContainerHandler(ApplicationContextBuilder contextBuilder) {
    super(contextBuilder);
  }

  public APIGatewayProxyResponseEvent handleRequest(final APIGatewayProxyRequestEvent request) {
    MicronautAwsRequest<?> containerRequest = requestTransformer.toMicronautRequest(request);
    Stream<UriRouteMatch<Object, Object>> uriRouteMatchStream = router.find(containerRequest);
    Flux<MutableHttpResponse<?>> mutableHttpResponseFlux =
        routeExecutor.executeRoute(containerRequest, true, Flux.fromStream(uriRouteMatchStream));
    HttpResponse<?> response = mutableHttpResponseFlux
        .single()
        .block();

    return responseTransformer.toAwsResponse(response);
  }

  @Override
  public APIGatewayProxyResponseEvent execute(final APIGatewayProxyRequestEvent input) {
    return handleRequest(input);
  }
}
