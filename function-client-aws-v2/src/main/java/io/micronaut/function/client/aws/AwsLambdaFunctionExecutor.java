/*
 * Copyright 2017-2020 original authors
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
package io.micronaut.function.client.aws;

import io.micronaut.context.annotation.Requires;
import io.micronaut.core.async.publisher.Publishers;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.io.buffer.ByteBufferFactory;
import io.micronaut.core.type.Argument;
import io.micronaut.function.client.FunctionDefinition;
import io.micronaut.function.client.FunctionInvoker;
import io.micronaut.function.client.FunctionInvokerChooser;
import io.micronaut.function.client.exceptions.FunctionExecutionException;
import io.micronaut.jackson.codec.JsonMediaTypeCodec;
import io.micronaut.scheduling.TaskExecutors;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.lambda.LambdaAsyncClient;
import software.amazon.awssdk.services.lambda.model.InvokeRequest;
import software.amazon.awssdk.services.lambda.model.InvokeResponse;

import java.nio.ByteBuffer;
import java.util.Optional;
import java.util.concurrent.ExecutorService;

/**
 * A {@link FunctionInvoker} for invoking functions on AWS.
 *
 * @param <I> input type
 * @param <O> output type
 * @author graemerocher
 * @since 1.0
 */
@Requires(beans = LambdaAsyncClient.class)
@Singleton
public class AwsLambdaFunctionExecutor<I, O> implements FunctionInvoker<I, O>, FunctionInvokerChooser {

    private static final int STATUS_CODE_ERROR = 300;
    private final LambdaAsyncClient asyncClient;
    private final ByteBufferFactory byteBufferFactory;
    private final JsonMediaTypeCodec jsonMediaTypeCodec;
    private final ExecutorService ioExecutor;

    /**
     * Constructor.
     *
     * @param asyncClient        asyncClient
     * @param byteBufferFactory  byteBufferFactory
     * @param jsonMediaTypeCodec jsonMediaTypeCodec
     * @param ioExecutor         ioExecutor
     */
    protected AwsLambdaFunctionExecutor(
        LambdaAsyncClient asyncClient,
        ByteBufferFactory byteBufferFactory,
        JsonMediaTypeCodec jsonMediaTypeCodec,
        @Named(TaskExecutors.IO) ExecutorService ioExecutor) {

        this.asyncClient = asyncClient;
        this.byteBufferFactory = byteBufferFactory;
        this.jsonMediaTypeCodec = jsonMediaTypeCodec;
        this.ioExecutor = ioExecutor;
    }

    @Override
    public O invoke(FunctionDefinition definition, I input, Argument<O> outputType) {
        if (!(definition instanceof AwsInvokeRequestDefinition)) {
            throw new IllegalArgumentException("Function definition must be a AWSInvokeRequestDefinition");
        }
        InvokeRequest.Builder invokeRequestBuilder = ((AwsInvokeRequestDefinition) definition).getInvokeRequestBuilder();
        boolean isReactiveType = Publishers.isConvertibleToPublisher(outputType.getType());
        if (isReactiveType) {
            final Mono<Object> invokeFlowable = Mono.<InvokeResponse>create(emitter -> {
                    InvokeRequest invokeRequest = encodeRequest(input, invokeRequestBuilder);

                    asyncClient.invoke(invokeRequest).whenComplete((invokeResult, exception) -> {
                        if (exception != null) {
                            emitter.error(exception);
                        } else {
                            emitter.success(invokeResult);
                        }
                    });
                })
                .map(invokeResponse ->
                    decodeResponse(definition, (Argument<O>) outputType.getFirstTypeVariable().orElse(Argument.OBJECT_ARGUMENT), invokeResponse))
                .onErrorResume(throwable -> Mono.error(new FunctionExecutionException("Error executing AWS Lambda [" + definition.getName() + "]: " + throwable.getMessage(), throwable)))
                .subscribeOn(Schedulers.fromExecutor(ioExecutor));

            return ConversionService.SHARED.convert(invokeFlowable, outputType).orElseThrow(() -> new IllegalArgumentException("Unsupported Reactive type: " + outputType));

        } else {
            InvokeRequest invokeRequest = encodeRequest(input, invokeRequestBuilder);

            try {
                InvokeResponse invokeResponse = asyncClient.invoke(invokeRequest).get();
                return (O) decodeResponse(definition, outputType, invokeResponse);
            } catch (Exception e) {
                throw new FunctionExecutionException("Error executing AWS Lambda [" + definition.getName() + "]: " + e.getMessage(), e);
            }
        }
    }

    private Object decodeResponse(FunctionDefinition definition, Argument<O> outputType, InvokeResponse invokeResponse) {
        Integer statusCode = invokeResponse.statusCode();
        if (statusCode >= STATUS_CODE_ERROR) {
            throw new FunctionExecutionException("Error executing AWS Lambda [" + definition.getName() + "]: " + invokeResponse.functionError());
        }

        if (outputType.equalsType(Argument.VOID)) {
            return null;
        }

        io.micronaut.core.io.buffer.ByteBuffer byteBuffer = byteBufferFactory.copiedBuffer(invokeResponse.payload().asByteBuffer());

        return jsonMediaTypeCodec.decode(outputType, byteBuffer);
    }

    private InvokeRequest encodeRequest(I input, InvokeRequest.Builder invokeRequestBuilder) {
        if (input != null) {
            ByteBuffer byteBuffer = jsonMediaTypeCodec.encode(input, byteBufferFactory).asNioBuffer();
            invokeRequestBuilder.payload(SdkBytes.fromByteBuffer(byteBuffer));
        }
        return invokeRequestBuilder.build();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <I1, O2> Optional<FunctionInvoker<I1, O2>> choose(FunctionDefinition definition) {
        if (definition instanceof AwsInvokeRequestDefinition) {
            return Optional.of((FunctionInvoker) this);
        }
        return Optional.empty();
    }
}
