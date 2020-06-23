/*
 * Copyright 2017-2020 original authors
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

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micronaut.context.ApplicationContextProvider;
import io.micronaut.function.aws.MicronautLambdaContext;
import io.micronaut.jackson.codec.JsonMediaTypeCodec;
import io.micronaut.web.router.Router;

/**
 * A context object to share state.
 *
 * @author graemerocher
 * @since 1.1
 */
public interface MicronautLambdaContainerContext extends ApplicationContextProvider, MicronautLambdaContext {

    /**
     * @return The {@link Router} instance.
     */
    Router getRouter();

    /**
     * @return The JSON codec.
     */
    JsonMediaTypeCodec getJsonCodec();

    /**
     * @return The Jackson's {@link ObjectMapper}
     */
    default ObjectMapper getObjectMapper() {
        return getJsonCodec().getObjectMapper();
    }
}
