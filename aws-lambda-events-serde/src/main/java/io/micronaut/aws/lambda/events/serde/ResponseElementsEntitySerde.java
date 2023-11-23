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

import com.amazonaws.services.lambda.runtime.events.models.s3.S3EventNotification;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.type.Argument;
import io.micronaut.json.tree.JsonNode;
import io.micronaut.serde.Decoder;
import io.micronaut.serde.Encoder;
import io.micronaut.serde.Serde;
import jakarta.inject.Singleton;

import java.io.IOException;

/**
 * @see <a href="https://github.com/aws/aws-lambda-java-libs/blob/main/aws-lambda-java-serialization/src/main/java/com/amazonaws/services/lambda/runtime/serialization/events/serializers/S3EventSerializer.java#L526-L553">Custom Serialization for ResponseElementsEntity</a>
 */
@Internal
@Singleton
public class ResponseElementsEntitySerde implements Serde<S3EventNotification.ResponseElementsEntity> {
    private static final String X_AMZ_ID_2 = "x-amz-id-2";
    private static final String X_AMZ_REQUEST_ID = "x-amz-request-id";

    @Override
    public @Nullable S3EventNotification.ResponseElementsEntity deserialize(@NonNull Decoder decoder, @NonNull DecoderContext context, @NonNull Argument<? super S3EventNotification.ResponseElementsEntity> type) throws IOException {
        JsonNode node = decoder.decodeNode();
        JsonNode xAmzId2Node = node.get(X_AMZ_ID_2);
        String xAmzId2 = xAmzId2Node != null ? xAmzId2Node.getStringValue() : null;
        JsonNode xAmzRequestIdNode = node.get(X_AMZ_REQUEST_ID);
        String xAmzRequestId = xAmzRequestIdNode != null ? xAmzRequestIdNode.getStringValue() : null;
        return new S3EventNotification.ResponseElementsEntity(xAmzId2, xAmzRequestId);
    }

    @Override
    public void serialize(@NonNull Encoder encoder, @NonNull EncoderContext context, @NonNull Argument<? extends S3EventNotification.ResponseElementsEntity> type, S3EventNotification.@NonNull ResponseElementsEntity value) throws IOException {
        encoder.encodeObject(type);
        encoder.encodeKey(X_AMZ_ID_2);
        encoder.encodeString(value.getxAmzId2());
        encoder.encodeKey(X_AMZ_REQUEST_ID);
        encoder.encodeString(value.getxAmzRequestId());
        encoder.finishStructure();
    }
}
