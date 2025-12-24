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
package io.micronaut.function.aws.proxy.payload2;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.util.CollectionUtils;
import io.micronaut.core.type.Argument;
import io.micronaut.core.util.StringUtils;
import io.micronaut.function.aws.proxy.ApiGatewayServletRequest;
import io.micronaut.function.aws.proxy.MapListOfStringAndMapStringMutableHttpParameters;
import io.micronaut.http.HttpHeaders;
import io.micronaut.http.MediaType;
import io.micronaut.http.MutableHttpHeaders;
import io.micronaut.http.MutableHttpParameters;
import io.micronaut.http.simple.SimpleHttpParameters;
import io.micronaut.http.uri.QueryStringDecoder;
import io.micronaut.servlet.http.BodyBuilder;
import io.micronaut.servlet.http.ServletHttpRequest;
import io.micronaut.servlet.http.ServletHttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.List;
import java.util.Map;

/**
 * Implementation of {@link ServletHttpRequest} for AWS API Gateway Proxy.
 * Uses comma separated header values instead of "multiValueHeaders". And puts cookies in "cookies"
 * instead of as "Set-Cookie" headers.
 * @see <a href="https://docs.aws.amazon.com/apigateway/latest/developerguide/http-api-develop-integrations-lambda.html#http-api-develop-integrations-lambda.proxy-format">
 *     Create AWS Lambda proxy integrations for HTTP APIs in API Gateway
 *     </a>
 *
 * @param <B> The body type
 * @author Tim Yates
 * @since 4.0.0
 */
@Internal
public final class APIGatewayV2HTTPEventServletRequest<B> extends ApiGatewayServletRequest<B, APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {

    private static final Logger LOG = LoggerFactory.getLogger(APIGatewayV2HTTPEventServletRequest.class);

    private final APIGatewayV2HTTPResponseServletResponse<Object> response;

    public APIGatewayV2HTTPEventServletRequest(
        APIGatewayV2HTTPEvent requestEvent,
        APIGatewayV2HTTPResponseServletResponse<Object> response,
        ConversionService conversionService,
        BodyBuilder bodyBuilder
    ) {
        super(
            conversionService,
            requestEvent,
            // We'll set the URI in a moment once we have the character encoding available
            null,
            parseMethod(() -> requestEvent.getRequestContext().getHttp().getMethod()),
            LOG,
            bodyBuilder
        );
        this.uri(ApiGatewayServletRequest.buildUri(
            requestEvent.getRequestContext().getHttp().getPath(),
            Collections.emptyMap(),
            buildMultiQueryParameters(requestEvent, getCharacterEncoding())
        ));
        this.response = response;
    }

    private static Map<String, List<String>> buildMultiQueryParameters(
        APIGatewayV2HTTPEvent requestEvent, Charset charset
    ) {
        // We're NOT going to use requestEvent.getQueryStringParameters() because AWS API Gateway V2
        // provides a parameter value of e.g. "value1,value2,value3" when the "rawQueryString" is
        // "parameter1=value1%2Cvalue2&parameter1=value3". In that case, we expect to have two
        // values, not three, where the first value is "value1,value2" and the second value is
        // "value3". However, if the "rawQueryString" is "parameter1=value1%2Cvalue2", the default
        // Micronaut behavior is to treat that as two strings, using the comma as a delimiter.
        String rawQueryString = requestEvent.getRawQueryString();
        if (StringUtils.isEmpty(rawQueryString)) {
            return Collections.emptyMap();
        } else {
            QueryStringDecoder decoder = new QueryStringDecoder(rawQueryString, charset, false);
            return decoder.parameters();
        }
    }

    @Override
    public byte[] getBodyBytes() throws EmptyBodyException {
        return getBodyBytes(requestEvent::getBody, requestEvent::getIsBase64Encoded);
    }

    @Override
    public MutableHttpHeaders getHeaders() {
        return getHeaders(requestEvent::getHeaders, this::getCookieHeaders);
    }

    @Override
    public MutableHttpParameters getParameters() {
        return getParameters(
            Collections::emptyMap,
            () -> buildMultiQueryParameters(getNativeRequest(), getCharacterEncoding())
        );
    }

    /**
     * @param queryStringParametersSupplier Query String parameters as a map with key string and value string
     * @param multiQueryStringParametersSupplier Query String parameters as a map with key string and value list of strings
     * @return Mutable HTTP parameters
     */
    @Override
    @NonNull
    protected MutableHttpParameters getParameters(
        @NonNull Supplier<Map<String, String>> queryStringParametersSupplier,
        @NonNull Supplier<Map<String, List<String>>> multiQueryStringParametersSupplier
    ) {
        MediaType mediaType = getContentType().orElse(MediaType.APPLICATION_JSON_TYPE);

        final Map<String, List<String>> parameters;
        if (isFormSubmission(mediaType)) {
            MapListOfStringAndMapStringMutableHttpParameters result = getParametersFromBody(null);

            parameters = new LinkedHashMap<>();
            for (String name : result.names()) {
                List<String> values = result.getAll(name);

                if (!values.isEmpty()) {
                    // Unlike query parameters, Micronaut only takes one value per key in a body request
                    String first = values.getFirst();
                    // This mirrors Micronaut's default binding behavior:
                    // If there is only one value, try to convert it to a List<String>.
                    // This triggers the StringToIterableConverter, unless overridden, which splits on commas.
                    List<String> converted = conversionService.convert(
                        first,
                        Argument.listOf(String.class)
                    ).orElse(values);
                    parameters.put(name, converted);
                }
            }
        } else {
            parameters = multiQueryStringParametersSupplier.get();
        }

        Map<CharSequence, List<String>> charSeqMap = parameters == null ?
            Collections.emptyMap() :
            parameters.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        return new SimpleHttpParameters(charSeqMap, conversionService);
    }

    @Override
    public ServletHttpResponse<APIGatewayV2HTTPResponse, ?> getResponse() {
        return response;
    }

    private Map<String, List<String>> getCookieHeaders() {
        List<String> cookies = requestEvent.getCookies();
        return CollectionUtils.isEmpty(cookies)
            ? Collections.emptyMap()
            : Map.of(HttpHeaders.COOKIE, cookies);
    }
}
