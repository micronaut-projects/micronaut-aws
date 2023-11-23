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
 * This seems to be necessary because Serde was not picking the appropriate constructor {@link com.amazonaws.services.lambda.runtime.events.models.s3.S3EventNotification.S3ObjectEntity(String, Long, String, String, String)}.
 */
@Internal
@Singleton
public class S3ObjectEntitySerde implements Serde<S3EventNotification.S3ObjectEntity> {
    private final String KEY = "key";
    private final String SIZE = "size";
    private final String ETAG = "eTag";
    private final String VERSION_ID = "versionId";
    private final String SEQUENCER = "sequencer";

    @Override
    public @Nullable S3EventNotification.S3ObjectEntity deserialize(@NonNull Decoder decoder, @NonNull DecoderContext context, @NonNull Argument<? super S3EventNotification.S3ObjectEntity> type) throws IOException {
        JsonNode node = decoder.decodeNode();

        JsonNode keyNode = node.get(KEY);
        String key = keyNode != null ? keyNode.getStringValue() : null;

        JsonNode sizeNode = node.get(SIZE);
        Long size = sizeNode != null ? sizeNode.getLongValue() : null;

        JsonNode eTagNode = node.get(ETAG);
        String eTag = eTagNode != null ? eTagNode.getStringValue() : null;

        JsonNode versionIdNode = node.get(VERSION_ID);
        String versionId = versionIdNode != null ? versionIdNode.getStringValue() : null;

        JsonNode sequencerNode = node.get(SEQUENCER);
        String sequencer = sequencerNode != null ? sequencerNode.getStringValue() : null;

        return new  S3EventNotification.S3ObjectEntity(key, size, eTag, versionId, sequencer);
    }

    @Override
    public void serialize(@NonNull Encoder encoder, @NonNull EncoderContext context, @NonNull Argument<? extends S3EventNotification.S3ObjectEntity> type, S3EventNotification.@NonNull S3ObjectEntity value) throws IOException {
        encoder.encodeObject(type);
        if (value.getKey() != null) {
            encoder.encodeKey(KEY);
            encoder.encodeString(value.getKey());
        }
        if (value.getSize() != null) {
            encoder.encodeKey(SIZE);
            encoder.encodeLong(value.getSize());
        }
        if (value.geteTag() != null) {
            encoder.encodeKey(ETAG);
            encoder.encodeString(value.geteTag());
        }
        if (value.getVersionId() != null) {
            encoder.encodeKey(VERSION_ID);
            encoder.encodeString(value.getVersionId());
        }
        if (value.getSequencer() != null) {
            encoder.encodeKey(SEQUENCER);
            encoder.encodeString(value.getSequencer());
        }
        encoder.finishStructure();
    }
}
