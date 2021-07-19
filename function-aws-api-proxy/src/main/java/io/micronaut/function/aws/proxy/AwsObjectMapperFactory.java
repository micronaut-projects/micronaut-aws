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
import io.micronaut.aws.AWSConfiguration;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Requires;

import jakarta.inject.Named;
import jakarta.inject.Singleton;

/**
 * Factory class that creates an object mapper if the property "aws.proxy.shared-object-mapper" is set to true.
 *
 * @author Álvaro Sánchez-Mariscal
 * @since 1.4.0
 */
@Factory
public class AwsObjectMapperFactory {

    /**
     * @return a new {@link ObjectMapper}
     */
    @Singleton
    @Named("aws")
    @Requires(property = AWSConfiguration.PREFIX + "." + MicronautAwsProxyConfiguration.PREFIX + ".shared-object-mapper", value = "false")
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}
