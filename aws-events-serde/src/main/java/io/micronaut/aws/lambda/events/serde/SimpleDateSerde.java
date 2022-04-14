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
import io.micronaut.serde.Serde;
import io.micronaut.serde.util.NullableSerde;
import jakarta.inject.Singleton;

import java.io.IOException;
import java.util.Date;

/**
 * {@link NullableSerde} implementation for Java's {@link java.util.Date}.
 *
 * @author Dan Hollingsworth
 */
//@Singleton //TODO for debugging temporarily only... conflicts with classes in serde project
//@Primary
public class SimpleDateSerde implements Serde<Date> {//TODO unit test

    @Override
    public void serialize(@NonNull Encoder encoder,
                          @NonNull EncoderContext context,
                          @NonNull Argument<? extends Date> type,
                          @NonNull Date value) throws IOException {
        System.out.println("> " + value.getTime()); //TODO the long is already truncated here
        encoder.encodeLong(value.getTime());
    }

    @Override
    @NonNull
    public Date deserialize(Decoder decoder, DecoderContext decoderContext, Argument<? super Date> type) throws IOException {
        return new Date(decoder.decodeLong());
    }
}
