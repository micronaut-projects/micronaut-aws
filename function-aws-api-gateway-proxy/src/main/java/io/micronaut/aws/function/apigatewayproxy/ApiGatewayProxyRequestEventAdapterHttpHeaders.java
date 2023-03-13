package io.micronaut.aws.function.apigatewayproxy;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.convert.ArgumentConversionContext;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.util.CollectionUtils;
import io.micronaut.http.HttpHeaders;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class ApiGatewayProxyRequestEventAdapterHttpHeaders implements HttpHeaders {
    private final Map<String, List<String>> headers;
    private final ConversionService conversionService;
    public ApiGatewayProxyRequestEventAdapterHttpHeaders(ConversionService conversionService, APIGatewayProxyRequestEvent event) {
        this.conversionService = conversionService;
        if (event.getMultiValueHeaders() == null && event.getHeaders() == null) {
            headers = Collections.emptyMap();
        } else {
            headers = new HashMap<>();
            if (event.getMultiValueHeaders() != null) {
                for (String name : event.getMultiValueHeaders().keySet()) {
                    String headerName = HttpHeaderUtils.normalizeHttpHeaderCase(name);
                    headers.computeIfAbsent(headerName, s -> new ArrayList<>());
                    headers.get(headerName).addAll(event.getMultiValueHeaders().get(headerName));
                }
            }
            if (CollectionUtils.isNotEmpty(event.getHeaders())) {
                for (String name : event.getHeaders().keySet()) {
                    String headerName = HttpHeaderUtils.normalizeHttpHeaderCase(name);
                    headers.computeIfAbsent(headerName, s -> new ArrayList<>());
                    headers.get(headerName).add(event.getHeaders().get(headerName));
                }
            }
        }
    }


    @Override
    public List<String> getAll(CharSequence name) {
        String headerName = HttpHeaderUtils.normalizeHttpHeaderCase(name.toString());
        if (!headers.containsKey(headerName)) {
            return Collections.emptyList();
        }
        List<String> values = headers.get(headerName);
        if (values == null) {
            return Collections.emptyList();
        }
        return values;
    }

    @Nullable
    @Override
    public String get(CharSequence name) {
        List<String> values = getAll(name);
        if (CollectionUtils.isEmpty(values)) {
            return null;
        }
        return values.get(0);
    }

    @Override
    public Set<String> names() {
        return headers.keySet();
    }

    @Override
    public Collection<List<String>> values() {
        return headers.values();
    }

    @Override
    public <T> Optional<T> get(CharSequence name, ArgumentConversionContext<T> conversionContext) {
        final String v = get(name);
        if (v != null) {
            return conversionService.convert(v, conversionContext);
        }
        return Optional.empty();
    }
}
