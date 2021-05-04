package io.micronaut.aws.xray

import io.micronaut.core.annotation.Nullable
import io.micronaut.core.convert.ArgumentConversionContext
import io.micronaut.core.convert.ConversionService
import io.micronaut.http.MutableHttpHeaders

import java.util.stream.Collectors

class MockHttpHeaders implements MutableHttpHeaders {

    private final Map<CharSequence, List<String>> headers;

    MockHttpHeaders(Map<CharSequence, List<String>> headers) {
        this.headers = headers;
    }

    @Override
    MutableHttpHeaders add(CharSequence header, CharSequence value) {
        headers.compute(header, (key, val) -> {
            if (val == null) {
                val = new ArrayList<>();
            }
            val.add(value.toString());
            return val;
        });
        return this;
    }

    @Override
    MutableHttpHeaders remove(CharSequence header) {
        headers.remove(header);
        return this;
    }

    @Override
    List<String> getAll(CharSequence name) {
        List<String> values = headers.get(name);
        if (values == null) {
            return Collections.emptyList();
        } else {
            return values;
        }
    }

    @Nullable
    @Override
    String get(CharSequence name) {
        List<String> values = headers.get(name);
        if (values == null || values.isEmpty()) {
            return null;
        } else {
            return values.get(0);
        }
    }

    @Override
    Set<String> names() {
        return headers.keySet().stream().map(CharSequence::toString).collect(Collectors.toSet());
    }

    @Override
    Collection<List<String>> values() {
        return headers.values();
    }

    @Override
    <T> Optional<T> get(CharSequence name, ArgumentConversionContext<T> conversionContext) {
        return ConversionService.SHARED.convert(get(name), conversionContext);
    }
}
