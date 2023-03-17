package io.micronaut.aws.function.apigatewayproxy;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.convert.ArgumentConversionContext;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.util.CollectionUtils;
import io.micronaut.http.HttpHeaders;
import io.micronaut.http.MutableHttpHeaders;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class ApiGatewayProxyHeaderAdapter implements MutableHttpHeaders, HttpHeaders {

    private final Map<String, List<String>> headers;
    private ConversionService conversionService;

    /**
     *
     * @param conversionService Conversion Service
     * @param event API Gateway Proxy Request event.
     */
    public ApiGatewayProxyHeaderAdapter(APIGatewayProxyRequestEvent event, ConversionService conversionService) {
        this.conversionService = conversionService;
        headers = new HashMap<>();
        adaptHeaders(event.getMultiValueHeaders(), event.getHeaders());
    }

    public ApiGatewayProxyHeaderAdapter(APIGatewayProxyResponseEvent event, ConversionService conversionService) {
        this.conversionService = conversionService;
        headers = new HashMap<>();
        adaptHeaders(event.getMultiValueHeaders(), event.getHeaders());
    }

    private void adaptHeaders(
        @Nullable Map<String, List<String>> multiValueHeaders,
        @Nullable Map<String, String> singleHeaders
    ) {
        if (multiValueHeaders != null) {
            for (String name : multiValueHeaders.keySet()) {
                String headerName = HttpHeaderUtils.normalizeHttpHeaderCase(name);
                headers.computeIfAbsent(headerName, s -> new ArrayList<>());
                headers.get(headerName).addAll(multiValueHeaders.get(name));
            }
        }
        if (CollectionUtils.isNotEmpty(singleHeaders)) {
            for (String name : singleHeaders.keySet()) {
                String headerName = HttpHeaderUtils.normalizeHttpHeaderCase(name);
                headers.computeIfAbsent(headerName, s -> new ArrayList<>());
                headers.get(headerName).add(singleHeaders.get(name));
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

    @Override
    public MutableHttpHeaders add(CharSequence name, CharSequence value) {
        String headerName = HttpHeaderUtils.normalizeHttpHeaderCase(name.toString());
        headers.computeIfAbsent(headerName, s -> new ArrayList<>());
        headers.get(headerName).add(value.toString());
        return this;
    }

    @Override
    public MutableHttpHeaders remove(CharSequence header) {
        headers.remove(header.toString());
        return this;
    }

    @Override
    public void setConversionService(ConversionService conversionService) {
        this.conversionService = conversionService;
    }
}
