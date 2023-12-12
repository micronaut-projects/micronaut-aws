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
import com.amazonaws.lambda.thirdparty.com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.amazonaws.services.lambda.runtime.events.S3Event;
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
import java.util.List;

/**
 * {@link SerdeImport} for {@link S3Event}.
 *
 * @author Dan Hollingsworth
 * @since 4.0.0
 */
@SerdeImport(value = S3Event.class, mixin = S3EventSerde.Mixin.class, deserializable = false)
@Singleton
@Internal
public class S3EventSerde implements Deserializer<S3Event> {
    private static final String RECORDS = "Records";

    @Override
    public @NonNull Deserializer<S3Event> createSpecific(DecoderContext context, @NonNull Argument<? super S3Event> type) throws SerdeException {
        Argument<S3EventDes> arg = Argument.of(S3EventDes.class);
        Deserializer<? extends S3EventDes> specific = context.findDeserializer(S3EventDes.class).createSpecific(context, arg);
        return new Deserializer<>() {
            @Override
            public @Nullable S3Event deserialize(@NonNull Decoder decoder, DecoderContext context, @NonNull Argument<? super S3Event> type) throws IOException {
                return specific.deserialize(decoder, context, arg).actual;
            }
        };
    }

    @Override
    public @Nullable S3Event deserialize(@NonNull Decoder decoder, DecoderContext context, @NonNull Argument<? super S3Event> type) throws IOException {
        throw new UnsupportedOperationException("Use specific deserializer");
    }

    @Serdeable.Deserializable
    static final class S3EventDes {
        final S3Event actual;

        S3EventDes(@JsonProperty(RECORDS) List<S3EventNotification.S3EventNotificationRecord> records) {
            actual = new S3Event(records);
        }
    }

    interface Mixin {
        @JsonGetter(RECORDS)
        List<S3EventNotification.S3EventNotificationRecord> getRecords();
    }
}
