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
package io.micronaut.function.aws.proxy;

import java.util.Optional;

import io.micronaut.core.reflect.GenericTypeUtils;
import io.micronaut.core.type.Argument;
import io.micronaut.core.util.ArrayUtils;
import io.micronaut.http.codec.MediaTypeCodec;

public class DefaultMicronautAwsRequestBodySupplier<T> implements MicronautAwsRequestBodySupplier<T> {
  private final MediaTypeCodec mediaTypeCodec;
  private final String rawBody;
  private Class<T> type = initTypeArgument(0);

  public DefaultMicronautAwsRequestBodySupplier(MediaTypeCodec mediaTypeCodec, String rawBody) {
    this.mediaTypeCodec = mediaTypeCodec;
    this.rawBody = rawBody;
  }

  @Override
  public Optional<T> getBody() {
    return Optional.ofNullable(rawBody)
        .map(b -> mediaTypeCodec.decode(type, rawBody));
  }

  @Override
  public <T1> Optional<T1> getBody(final Argument<T1> argument) {
    return Optional.ofNullable(rawBody)
        .map(b -> mediaTypeCodec.decode(argument, rawBody));
  }

  private Class initTypeArgument(int index) {
    Class[] args = GenericTypeUtils.resolveSuperTypeGenericArguments(this.getClass(), DefaultMicronautAwsRequestBodySupplier.class);
    return ArrayUtils.isNotEmpty(args) && args.length > index ? args[index] : Object.class;
  }
}
