package io.micronaut.aws.function.apigatewayproxy;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.ApplicationContextBuilder;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.execution.ExecutionFlow;
import io.micronaut.core.execution.ImperativeExecutionFlow;
import io.micronaut.core.util.CollectionUtils;
import io.micronaut.function.aws.MicronautRequestHandler;
import io.micronaut.http.HttpHeaders;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.server.RequestLifecycle;
import io.micronaut.http.server.RouteExecutor;
import jakarta.inject.Inject;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ApiGatewayProxyRequestEventHandler extends MicronautRequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    @Inject
    RouteExecutor routeExecutor;

    @Inject
    ConversionService conversionService;

    public ApiGatewayProxyRequestEventHandler() {
    }

    public ApiGatewayProxyRequestEventHandler(ApplicationContext applicationContext) {
        super(applicationContext);
    }

    public ApiGatewayProxyRequestEventHandler(ApplicationContextBuilder applicationContextBuilder) {
        super(applicationContextBuilder);
    }

    @Override
    public APIGatewayProxyResponseEvent execute(APIGatewayProxyRequestEvent input) {
        HttpRequest<?> request = new ApiGatewayProxyRequestEventAdapter<>(conversionService, input);
        HttpResponse<?> response = handle(request);
        return adaptResponse(request, response);
    }

    private HttpResponse<?> handle(HttpRequest<?> request) {
        AmazonApiGatewayRequestLifecycle requestLifecycle = new AmazonApiGatewayRequestLifecycle(routeExecutor, request);
        ExecutionFlow<MutableHttpResponse<?>> flow = requestLifecycle.run();
        ImperativeExecutionFlow<MutableHttpResponse<?>> mutableHttpResponseImperativeExecutionFlow = flow.tryComplete();
        return mutableHttpResponseImperativeExecutionFlow.getValue();
    }

    private APIGatewayProxyResponseEvent adaptResponse(HttpRequest<?> request, HttpResponse<?> response) {
        //TODO Extract this adaptation to a  class where we can reuse it in the TCK logic
        APIGatewayProxyResponseEvent responseEvent = new APIGatewayProxyResponseEvent();
        response.getBody(String.class).ifPresent(responseEvent::setBody);
        responseEvent.setStatusCode(response.code());

        // NO idea why conten type is not in the response headers

        Map<String, List<String>> multivalueHeaders = response.getHeaders().asMap();

        if (response.getContentType().isEmpty()) {
            List<String> acceptHeaders = request.getHeaders().getAll(HttpHeaders.ACCEPT);
            if (CollectionUtils.isNotEmpty(acceptHeaders)) {
                String contentType = acceptHeaders.get(0);
                multivalueHeaders.put(HttpHeaders.CONTENT_TYPE, Collections.singletonList(contentType));
            }
        }
        responseEvent.setMultiValueHeaders(multivalueHeaders);
        Map<String, String> headers = new HashMap<>();
        for (String headerName : multivalueHeaders.keySet()) {
            headers.put(headerName, String.join(",", multivalueHeaders.get(headerName)));
        }
        responseEvent.setHeaders(headers);

        return responseEvent;
    }
}
