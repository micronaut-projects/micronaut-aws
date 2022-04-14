/*
 * Copyright 2017-2021 original authors
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

import com.amazonaws.services.lambda.runtime.serialization.util.SerializeUtil;
import io.micronaut.context.annotation.Requires;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.TypeHint;
import io.micronaut.core.type.Argument;
import io.micronaut.serde.Decoder;
import io.micronaut.serde.Encoder;
import io.micronaut.serde.util.NullableSerde;
import jakarta.inject.Singleton;
import org.joda.time.DateTime;

import java.io.IOException;

/**
 * {@link NullableSerde} implementation for JODA {@link DateTime}.
 *
 * @author Sergio del Amo
 * @since 1.0.0
 */
@Singleton
@Requires(classes = DateTime.class)
@TypeHint(typeNames = {"org.joda.time.format.DateTimeFormatter", "org.joda.time.format.ISODateTimeFormat", "org.joda.time.ReadableInstant"})
public class JodaDateTimeSerde implements NullableSerde<DateTime> {//TODO unit test

    @Override
    public void serialize(@NonNull Encoder encoder,
                          @NonNull EncoderContext context,
                          @NonNull Argument<? extends DateTime> type,
                          @NonNull DateTime value) throws IOException {
        encoder.encodeString(SerializeUtil.serializeDateTime(value, getClass().getClassLoader()));
    }

    @Override
    @NonNull
    public DateTime deserializeNonNull(Decoder decoder, DecoderContext decoderContext, Argument<? super DateTime> type) throws IOException {
        return SerializeUtil.deserializeDateTime(DateTime.class, decoder.decodeString());
    }
}
