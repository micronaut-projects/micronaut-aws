/*
 * Copyright 2017-2022 original authors
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
package io.micronaut.aws.apigateway;

import io.micronaut.core.annotation.NonNull;

import java.util.Optional;

/**
 * Resolves Amazon API Gateway Stage from input event.
 * An Amazon ApiGateway stage is a logical reference to a lifecycle state of your API (for example, dev, prod, beta, or v2). API stages are identified by their API ID and stage name, and they're included in the URL you use to invoke the API.
 * @author Sergio del Amo
 * @since 3.10.0
 * @param <T> input event
 */
@FunctionalInterface
public interface StageResolver<T> {
    @NonNull Optional<String> resolve(@NonNull T input);
}
