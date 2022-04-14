/*
 * Copyright 2022 original authors
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

import io.micronaut.context.annotation.Primary;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.type.Argument;
import io.micronaut.serde.Decoder;
import io.micronaut.serde.Encoder;
import io.micronaut.serde.util.NullableSerde;
import jakarta.inject.Singleton;

import java.io.IOException;
import java.util.Base64;

/**
 * @author Dan Hollingsworth
 */
@Singleton
@Primary
public class ByteArraySerde implements NullableSerde<byte[]> {

    @Override
    public void serialize(Encoder encoder, EncoderContext context, Argument<? extends byte[]> type, byte[] value) throws IOException {
        encoder.encodeString(Base64.getEncoder().encodeToString(value));
    }

    @Override
    @NonNull
    public byte[] deserializeNonNull(Decoder decoder, DecoderContext decoderContext, Argument<? super byte[]> type) throws IOException {
        return Base64.getDecoder().decode(decoder.decodeString());
    }
}
