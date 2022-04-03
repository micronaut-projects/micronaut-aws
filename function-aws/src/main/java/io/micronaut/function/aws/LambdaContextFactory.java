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
package io.micronaut.function.aws;
import com.amazonaws.services.lambda.runtime.Context;
import io.micronaut.context.annotation.DefaultImplementation;
import io.micronaut.core.annotation.NonNull;

/**
 * Register Lambda Context singletons for the current Handler execution.
 * @author Sergio del Amo
 * @since 3.2.2
 */
@DefaultImplementation(DefaultLambdaContextFactory.class)
@FunctionalInterface
public interface LambdaContextFactory {
    /**
     * Registers Lambda Context as a singleton.
     * @param lambdaContext Lambda Context
     */
    void registerSingletons(@NonNull Context lambdaContext);
}
