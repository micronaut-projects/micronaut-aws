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

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micronaut.context.ApplicationContext;
import io.micronaut.jackson.codec.JsonMediaTypeCodec;
import io.micronaut.web.router.Router;

/**
 * Holds state for the running container.
 */
class MicronautLambdaContainerState implements MicronautLambdaContainerContext {
    private Router router;
    private ApplicationContext applicationContext;
    private JsonMediaTypeCodec jsonCodec;
    private ObjectMapper objectMapper;

    @Override
    public Router getRouter() {
        return router;
    }

    @Override
    public JsonMediaTypeCodec getJsonCodec() {
        return jsonCodec;
    }

    @Override
    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    @Override
    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    void setJsonCodec(JsonMediaTypeCodec jsonCodec) {
        this.jsonCodec = jsonCodec;
    }

    void setRouter(Router router) {
        this.router = router;
    }

    void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }
}
