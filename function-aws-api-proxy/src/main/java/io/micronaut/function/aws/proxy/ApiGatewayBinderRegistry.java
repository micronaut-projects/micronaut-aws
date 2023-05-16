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

import io.micronaut.context.annotation.Replaces;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.async.publisher.Publishers;
import io.micronaut.core.convert.ArgumentConversionContext;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.type.Argument;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.MediaType;
import io.micronaut.http.bind.DefaultRequestBinderRegistry;
import io.micronaut.http.bind.binders.DefaultBodyAnnotationBinder;
import io.micronaut.http.bind.binders.RequestArgumentBinder;
import io.micronaut.http.codec.MediaTypeCodecRegistry;
import io.micronaut.json.codec.MapperMediaTypeCodec;
import io.micronaut.json.tree.JsonArray;
import io.micronaut.json.tree.JsonNode;
import io.micronaut.servlet.http.ServletBinderRegistry;
import io.micronaut.servlet.http.ServletBodyBinder;
import io.micronaut.servlet.http.StreamedServletMessage;
import jakarta.inject.Singleton;
import org.reactivestreams.Processor;
import reactor.core.publisher.Flux;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.BiConsumer;

@Singleton
@Internal
@Replaces(DefaultRequestBinderRegistry.class)
class ApiGatewayBinderRegistry<T> extends ServletBinderRegistry<T> {

    public static final Argument<byte[]> BYTE_ARRAY = Argument.of(byte[].class);

    ApiGatewayBinderRegistry(
        MediaTypeCodecRegistry mediaTypeCodecRegistry,
        ConversionService conversionService,
        List<RequestArgumentBinder> binders,
        DefaultBodyAnnotationBinder<T> defaultBodyAnnotationBinder
    ) {
        super(mediaTypeCodecRegistry, conversionService, binders, defaultBodyAnnotationBinder);
    }

    @Override
    protected ServletBodyBinder<T> newServletBodyBinder(MediaTypeCodecRegistry mediaTypeCodecRegistry, ConversionService conversionService, DefaultBodyAnnotationBinder<T> defaultBodyAnnotationBinder) {
        return new ApiGatewayBinderRegistry.DefaultServletBodyBinder(conversionService, mediaTypeCodecRegistry, defaultBodyAnnotationBinder);
    }

    /**
     * Overridden body binder.
     *
     * @param <T> The type
     */
    private static class DefaultServletBodyBinder<T> extends ServletBodyBinder<T> {
        private final MediaTypeCodecRegistry mediaTypeCodecRegistry;

        public DefaultServletBodyBinder(ConversionService conversionService,
                                        MediaTypeCodecRegistry mediaTypeCodecRegistry,
                                        DefaultBodyAnnotationBinder<T> defaultBodyAnnotationBinder) {
            super(conversionService, mediaTypeCodecRegistry, defaultBodyAnnotationBinder);
            this.mediaTypeCodecRegistry = mediaTypeCodecRegistry;
        }

        @Override
        public BindingResult bind(ArgumentConversionContext context, HttpRequest source) {
            Argument<?> argument = context.getArgument();
            Class<?> type = argument.getType();
            if (CompletionStage.class.isAssignableFrom(type)) {
                StreamedServletMessage<?, byte[]> servletHttpRequest = (StreamedServletMessage<?, byte[]>) source;
                CompletableFuture<Object> future = new CompletableFuture<>();
                Argument<?> typeArgument = argument.getFirstTypeVariable().orElse(BYTE_ARRAY);
                Class<?> javaArgument = typeArgument.getType();
                Charset characterEncoding = servletHttpRequest.getCharacterEncoding();
                if (CharSequence.class.isAssignableFrom(javaArgument)) {
                    Flux.from(servletHttpRequest).collect(StringBuilder::new, (stringBuilder, bytes) ->
                        stringBuilder.append(new String(bytes, characterEncoding))
                    ).subscribe(
                        stringBuilder -> future.complete(stringBuilder.toString()),
                        future::completeExceptionally
                    );
                } else if (BYTE_ARRAY.getType().isAssignableFrom(type)) {
                    BiConsumer<ByteArrayOutputStream, byte[]> uncheckedOutputStreamWrite = (stream, bytes) -> {
                        try {
                            stream.write(bytes);
                        } catch (IOException ex) {
                            throw new UncheckedIOException(ex);
                        }
                    };
                    Flux.from(servletHttpRequest).collect(ByteArrayOutputStream::new, uncheckedOutputStreamWrite)
                        .subscribe(
                            stream -> future.complete(stream.toByteArray()),
                            future::completeExceptionally
                        );
                } else {
                    MediaType mediaType = servletHttpRequest.getContentType().orElse(MediaType.APPLICATION_JSON_TYPE);
                    MapperMediaTypeCodec codec = (MapperMediaTypeCodec) mediaTypeCodecRegistry.findCodec(mediaType, javaArgument).orElse(null);

                    if (codec == null) {
                        return super.bind(context, source);
                    } else {
                        Processor<byte[], JsonNode> jsonProcessor = codec.getJsonMapper().createReactiveParser(servletHttpRequest::subscribe, false);
                        Flux.from(jsonProcessor)
                            .next()
                            .subscribe((jsonNode) -> {
                                try {
                                    future.complete(codec.decode(typeArgument, jsonNode));
                                } catch (Exception e) {
                                    future.completeExceptionally(e);
                                }
                            }, (future::completeExceptionally));

                    }
                }
                return () -> Optional.of(future);
            } else {
                if (Publishers.isConvertibleToPublisher(type)) {
                    Argument<?> typeArgument = argument.getFirstTypeVariable().orElse(BYTE_ARRAY);
                    Class<?> javaArgument = typeArgument.getType();
                    ApiGatewayServletRequest<?, ?, ?> request = (ApiGatewayServletRequest<?, ?, ?>) source;

                    if (CharSequence.class.isAssignableFrom(javaArgument)) {
                        Flux<String> stringFlux = Flux.just(request.getBody(String.class).get());
                        if (type.isInstance(stringFlux)) {
                            return () -> Optional.of(stringFlux);
                        } else {
                            Object converted = Publishers.convertPublisher(conversionService, stringFlux, type);
                            return () -> Optional.of(converted);
                        }
                    } else if (byte[].class.isAssignableFrom(javaArgument)) {
                        byte[] bodyBytes;
                        try {
                            bodyBytes = request.getBodyBytes();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        return () -> Optional.of(Flux.just(bodyBytes));
                    } else {
                        MediaType mediaType = request.getContentType().orElse(MediaType.APPLICATION_JSON_TYPE);
                        MapperMediaTypeCodec codec = (MapperMediaTypeCodec) mediaTypeCodecRegistry.findCodec(mediaType, javaArgument).orElse(null);
                        JsonNode body = request.getBody(JsonNode.class).get();
                        if (body instanceof JsonArray arr) {
                            Flux<JsonNode> fromIterable = Flux.fromIterable(arr.values());
                            return () -> Optional.of(fromIterable.map(o -> codec.decode(typeArgument, o)));
                        } else {
                            Object converted = codec.decode(typeArgument, body);
                            return () -> Optional.of(Flux.just(converted));
                        }
                    }
                }
            }
            return super.bind(context, source);
        }
    }
}
