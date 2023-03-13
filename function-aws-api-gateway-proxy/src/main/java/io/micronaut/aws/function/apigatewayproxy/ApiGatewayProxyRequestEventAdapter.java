package io.micronaut.aws.function.apigatewayproxy;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.convert.value.MutableConvertibleValues;
import io.micronaut.core.convert.value.MutableConvertibleValuesMap;
import io.micronaut.http.HttpHeaders;
import io.micronaut.http.HttpMethod;
import io.micronaut.http.HttpParameters;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.cookie.Cookies;
import java.net.URI;
import java.util.Optional;

public class ApiGatewayProxyRequestEventAdapter<T> implements HttpRequest<T> {
    private APIGatewayProxyRequestEvent event;
    private final ConversionService conversionService;
    private final MutableConvertibleValues<Object> attributes = new MutableConvertibleValuesMap<>();

    public ApiGatewayProxyRequestEventAdapter(ConversionService conversionService, APIGatewayProxyRequestEvent event) {
        this.conversionService = conversionService;
        this.event = event;
    }
    @Override
    public Cookies getCookies() {
        return null;
    }

    @Override
    public HttpParameters getParameters() {
        return null;
    }

    @Override
    public HttpMethod getMethod() {
        return HttpMethod.parse(event.getHttpMethod());
    }

    @Override
    public URI getUri() {
        return URI.create(event.getPath());
    }

    @Override
    public HttpHeaders getHeaders() {
        return new ApiGatewayProxyRequestEventAdapterHttpHeaders(conversionService, event);
    }

    @Override
    public MutableConvertibleValues<Object> getAttributes() {
        return attributes;
    }

    @Override
    public Optional<T> getBody() {
        return Optional.empty();
    }
}
