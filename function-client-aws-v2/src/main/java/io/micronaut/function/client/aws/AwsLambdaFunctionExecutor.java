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
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.async.publisher.Publishers;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.io.buffer.ByteBufferFactory;
import io.micronaut.core.type.Argument;
import io.micronaut.function.client.FunctionDefinition;
import io.micronaut.function.client.FunctionInvoker;
import io.micronaut.function.client.FunctionInvokerChooser;
import io.micronaut.function.client.exceptions.FunctionExecutionException;
import io.micronaut.json.codec.JsonMediaTypeCodec;
import io.micronaut.scheduling.TaskExecutors;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.lambda.LambdaAsyncClient;
import software.amazon.awssdk.services.lambda.LambdaClient;
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
@Internal
public class AwsLambdaFunctionExecutor<I, O> implements FunctionInvoker<I, O>, FunctionInvokerChooser {

    private static final int STATUS_CODE_ERROR = 300;
    private final LambdaClient syncClient;
    private final LambdaAsyncClient asyncClient;
    private final ByteBufferFactory byteBufferFactory;
    private final JsonMediaTypeCodec mediaTypeCodec;
    private final ExecutorService ioExecutor;

    /**
     * Constructor.
     *
     * @param asyncClient        asyncClient
     * @param byteBufferFactory  byteBufferFactory
     * @param mediaTypeCodec JsonMediaTypeCodec
     * @param ioExecutor         ioExecutor
     */
    protected AwsLambdaFunctionExecutor(
        LambdaClient syncClient,
        LambdaAsyncClient asyncClient,
        ByteBufferFactory byteBufferFactory,
        JsonMediaTypeCodec mediaTypeCodec,
        @Named(TaskExecutors.IO) ExecutorService ioExecutor) {
        this.syncClient = syncClient;
        this.asyncClient = asyncClient;
        this.byteBufferFactory = byteBufferFactory;
        this.mediaTypeCodec = mediaTypeCodec;
        this.ioExecutor = ioExecutor;
    }

    @Override
    public O invoke(FunctionDefinition definition, I input, Argument<O> outputType) {
        if (!(definition instanceof AwsInvokeRequestDefinition)) {
            throw new IllegalArgumentException("Function definition must be a AWSInvokeRequestDefinition");
        }

        boolean isReactiveType = Publishers.isConvertibleToPublisher(outputType.getType());
        SdkBytes sdkBytes = encodeInput(input);

        AwsInvokeRequestDefinition awsInvokeRequestDefinition =
            (AwsInvokeRequestDefinition) definition;

        InvokeRequest invokeRequest = InvokeRequest.builder()
            .functionName(awsInvokeRequestDefinition.getFunctionName())
            .qualifier(awsInvokeRequestDefinition.getQualifier())
            .clientContext(awsInvokeRequestDefinition.getClientContext())
            .payload(sdkBytes)
            .build();

        if (isReactiveType) {
            Mono<Object> invokeFlowable = Mono.fromFuture(asyncClient.invoke(invokeRequest))
                .map(invokeResult ->
                    decodeResult(definition, (Argument<O>) outputType.getFirstTypeVariable().orElse(Argument.OBJECT_ARGUMENT), invokeResult))
                .onErrorResume(throwable -> Mono.error(new FunctionExecutionException("Error executing AWS Lambda [" + definition.getName() + "]: " + throwable.getMessage(), throwable)))
                .subscribeOn(Schedulers.fromExecutor(ioExecutor));
            return ConversionService.SHARED.convert(invokeFlowable, outputType).orElseThrow(() -> new IllegalArgumentException("Unsupported Reactive type: " + outputType));
        } else {
            InvokeResponse invokeResult = syncClient.invoke(invokeRequest);
            try {
                return (O) decodeResult(definition, outputType, invokeResult);
            } catch (Exception e) {
                throw new FunctionExecutionException("Error executing AWS Lambda [" + definition.getName() + "]: " + e.getMessage(), e);
            }
        }
    }

    private Object decodeResult(FunctionDefinition definition, Argument<O> outputType, InvokeResponse invokeResult) {
        Integer statusCode = invokeResult.statusCode();
        if (statusCode >= STATUS_CODE_ERROR) {
            throw new FunctionExecutionException("Error executing AWS Lambda [" + definition.getName() + "]: " + invokeResult.functionError());
        }
        io.micronaut.core.io.buffer.ByteBuffer byteBuffer = byteBufferFactory.copiedBuffer(invokeResult.payload().asByteArray());

        return mediaTypeCodec.decode(outputType, byteBuffer);
    }

    private SdkBytes encodeInput(I input) {
        if (input != null) {
            ByteBuffer nioBuffer = mediaTypeCodec.encode(input, byteBufferFactory).asNioBuffer();
            return SdkBytes.fromByteBuffer(nioBuffer);
        }
        return null;
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
