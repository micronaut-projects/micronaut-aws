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

import com.amazonaws.lambda.thirdparty.com.fasterxml.jackson.annotation.JsonGetter;
import com.amazonaws.lambda.thirdparty.com.fasterxml.jackson.annotation.JsonProperty;
import com.amazonaws.services.lambda.runtime.events.models.s3.S3EventNotification;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.type.Argument;
import io.micronaut.json.tree.JsonNode;
import io.micronaut.serde.Decoder;
import io.micronaut.serde.Deserializer;
import io.micronaut.serde.annotation.SerdeImport;
import io.micronaut.serde.annotation.Serdeable;
import io.micronaut.serde.exceptions.SerdeException;
import jakarta.inject.Singleton;

import java.io.IOException;

/**
 * This seems to be necessary because Serde was not picking the appropriate constructor {@link com.amazonaws.services.lambda.runtime.events.models.s3.S3EventNotification.S3ObjectEntity(String, Long, String, String, String)}.
 */
@Internal
@Singleton
@SerdeImport(value = S3EventNotification.S3ObjectEntity.class, mixin = S3ObjectEntitySerde.S3ObjectEntityMixin.class, deserializable = false)
public class S3ObjectEntitySerde implements Deserializer<S3EventNotification.S3ObjectEntity> {
    private static final String KEY = "key";
    private static final String SIZE = "size";
    private static final String ETAG = "eTag";
    private static final String VERSION_ID = "versionId";
    private static final String SEQUENCER = "sequencer";

    @Override
    public @NonNull Deserializer<S3EventNotification.S3ObjectEntity> createSpecific(DecoderContext context, @NonNull Argument<? super S3EventNotification.S3ObjectEntity> type) throws SerdeException {
        Argument<S3ObjectEntityDes> arg = Argument.of(S3ObjectEntityDes.class);
        Deserializer<? extends S3ObjectEntityDes> specific = context.findDeserializer(S3ObjectEntityDes.class).createSpecific(context, arg);
        return new Deserializer<>() {
            @Override
            public @Nullable S3EventNotification.S3ObjectEntity deserialize(@NonNull Decoder decoder, DecoderContext context, @NonNull Argument<? super S3EventNotification.S3ObjectEntity> type) throws IOException {
                return specific.deserialize(decoder, context, arg).actual;
            }
        };
    }

    @Override
    public @Nullable S3EventNotification.S3ObjectEntity deserialize(@NonNull Decoder decoder, @NonNull DecoderContext context, @NonNull Argument<? super S3EventNotification.S3ObjectEntity> type) throws IOException {
        throw new UnsupportedOperationException("Use specific deserializer");
    }

    @Serdeable.Deserializable
    static final class S3ObjectEntityDes {
        final S3EventNotification.S3ObjectEntity actual;

        S3ObjectEntityDes(
            @JsonProperty(KEY) String key,
            @JsonProperty(SIZE) Long size,
            @JsonProperty(ETAG) String eTag,
            @JsonProperty(VERSION_ID) String versionId,
            @JsonProperty(SEQUENCER) String sequencer
        ) {
            actual = new S3EventNotification.S3ObjectEntity(key, size, eTag, versionId, sequencer);
        }
    }

    interface S3ObjectEntityMixin {
        @JsonGetter(KEY)
        String getKey();

        @JsonGetter(SIZE)
        Long getSizeAsLong();

        @JsonGetter(ETAG)
        String geteTag();

        @JsonGetter(VERSION_ID)
        String getVersionId();

        @JsonGetter(SEQUENCER)
        String getSequencer();
    }
}
