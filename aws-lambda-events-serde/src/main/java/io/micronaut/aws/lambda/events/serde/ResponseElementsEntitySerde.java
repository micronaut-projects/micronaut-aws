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
package io.micronaut.aws.lambda.events.serde;

import com.amazonaws.lambda.thirdparty.com.fasterxml.jackson.annotation.JsonCreator;
import com.amazonaws.lambda.thirdparty.com.fasterxml.jackson.annotation.JsonGetter;
import com.amazonaws.lambda.thirdparty.com.fasterxml.jackson.annotation.JsonProperty;
import com.amazonaws.services.lambda.runtime.events.models.s3.S3EventNotification;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.type.Argument;
import io.micronaut.serde.Decoder;
import io.micronaut.serde.Deserializer;
import io.micronaut.serde.annotation.SerdeImport;
import io.micronaut.serde.annotation.Serdeable;
import io.micronaut.serde.exceptions.SerdeException;
import jakarta.inject.Singleton;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * @see <a href="https://github.com/aws/aws-lambda-java-libs/blob/main/aws-lambda-java-serialization/src/main/java/com/amazonaws/services/lambda/runtime/serialization/events/serializers/S3EventSerializer.java#L526-L553">Custom Serialization for ResponseElementsEntity</a>
 */
@Internal
@Singleton
@SerdeImport(value = S3EventNotification.ResponseElementsEntity.class, mixin = ResponseElementsEntitySerde.ResponseElementsEntityMixin.class, deserializable = false)
public class ResponseElementsEntitySerde implements Deserializer<S3EventNotification.ResponseElementsEntity> {
    private static final String X_AMZ_ID_2 = "x-amz-id-2";
    private static final String X_AMZ_REQUEST_ID = "x-amz-request-id";

    @Override
    public @NonNull Deserializer<S3EventNotification.ResponseElementsEntity> createSpecific(DecoderContext context, @NonNull Argument<? super S3EventNotification.ResponseElementsEntity> type) throws SerdeException {
        Argument<ResponseElementsEntityDes> arg = Argument.of(ResponseElementsEntityDes.class);
        Deserializer<? extends ResponseElementsEntityDes> specific = context.findDeserializer(ResponseElementsEntityDes.class).createSpecific(context, arg);
        return new Deserializer<>() {
            @Override
            public @Nullable S3EventNotification.ResponseElementsEntity deserialize(@NonNull Decoder decoder, DecoderContext context, @NonNull Argument<? super S3EventNotification.ResponseElementsEntity> type) throws IOException {
                return specific.deserialize(decoder, context, arg).actual;
            }
        };
    }

    @Override
    public @Nullable S3EventNotification.ResponseElementsEntity deserialize(@NonNull Decoder decoder, @NonNull DecoderContext context, @NonNull Argument<? super S3EventNotification.ResponseElementsEntity> type) throws IOException {
        throw new UnsupportedEncodingException("Specific deserializer required");
    }

    @Serdeable.Deserializable
    static final class ResponseElementsEntityDes {
        final S3EventNotification.ResponseElementsEntity actual;

        @JsonCreator
        ResponseElementsEntityDes(@JsonProperty(X_AMZ_ID_2) String xAmzId2, @JsonProperty(X_AMZ_REQUEST_ID) String xAmzRequestId) {
            this.actual = new S3EventNotification.ResponseElementsEntity(xAmzId2, xAmzRequestId);
        }
    }

    interface ResponseElementsEntityMixin {
        @JsonGetter(X_AMZ_ID_2)
        String getxAmzId2();

        @JsonGetter(X_AMZ_REQUEST_ID)
        String getxAmzRequestId();
    }
}
