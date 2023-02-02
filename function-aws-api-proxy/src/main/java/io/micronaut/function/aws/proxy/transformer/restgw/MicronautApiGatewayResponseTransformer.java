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
package io.micronaut.function.aws.proxy.transformer.restgw;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import io.micronaut.context.ApplicationContext;
import io.micronaut.function.aws.proxy.MicronautAwsResponseTransformer;
import io.micronaut.http.HttpHeaders;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.codec.CodecException;
import io.micronaut.http.codec.MediaTypeCodec;
import io.micronaut.http.codec.MediaTypeCodecRegistry;
import jakarta.inject.Singleton;

@Singleton
public class MicronautApiGatewayResponseTransformer<T> implements MicronautAwsResponseTransformer<APIGatewayProxyResponseEvent> {
    private final ApplicationContext context;
    private final MediaTypeCodecRegistry mediaTypeCodecRegistry;

    public MicronautApiGatewayResponseTransformer(final ApplicationContext context) {
        this.context = context;
        this.mediaTypeCodecRegistry = context.getBean(MediaTypeCodecRegistry.class);
    }

    @Override
    public APIGatewayProxyResponseEvent toAwsResponse(final HttpResponse<?> containerResponse) {
        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();

        HttpHeaders headers = containerResponse.getHeaders();
        Map<String, List<String>> multiValueHeaders = new HashMap<>();
        Map<String, String> simpleHeaders = new HashMap<>();

        headers.forEach((k, mv) -> {
            if (mv.size() == 1) {
                simpleHeaders.put(k, mv.get(0));
                return;
            }
            multiValueHeaders.put(k, mv);
        });

        MediaType mediaType = containerResponse.getContentType().orElse(MediaType.APPLICATION_JSON_TYPE);

        MediaTypeCodec codec = mediaTypeCodecRegistry.findCodec(mediaType)
            .orElseThrow(() -> new CodecException("Codec not found for type " + mediaType));

        Object body = containerResponse.body();
        byte[] encodedBytes = codec.encode(body);

        response.setStatusCode(containerResponse.getStatus().getCode());
        response.setHeaders(simpleHeaders);
        response.setMultiValueHeaders(multiValueHeaders);
        // TODO: Add Property for Optional Base64 Encoding
        response.setBody(new String(encodedBytes));
        response.setIsBase64Encoded(false);

        return response;
    }
}
