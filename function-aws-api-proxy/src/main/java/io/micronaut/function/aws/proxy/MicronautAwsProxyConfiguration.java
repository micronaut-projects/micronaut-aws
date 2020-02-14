/*
 * Copyright 2017-2019 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micronaut.function.aws.proxy;

import io.micronaut.configuration.aws.AWSConfiguration;
import io.micronaut.context.annotation.ConfigurationProperties;

/**
 * Configuration properties for the AWS proxy module.
 *
 * @author Álvaro Sánchez-Mariscal
 * @since 1.4.0
 */
@ConfigurationProperties(MicronautAwsProxyConfiguration.PREFIX)
public class MicronautAwsProxyConfiguration extends AWSConfiguration {
    public static final String PREFIX = "proxy";
    private static final boolean DEFAULT_SHARED_OBJECT_MAPPER = true;

    private boolean sharedObjectMapper = DEFAULT_SHARED_OBJECT_MAPPER;

    /**
     * @return whether to share the default ObjectMapper
     */
    public boolean isSharedObjectMapper() {
        return sharedObjectMapper;
    }

    /**
     * @param sharedObjectMapper whether to share the default ObjectMapper
     */
    public void setSharedObjectMapper(boolean sharedObjectMapper) {
        this.sharedObjectMapper = sharedObjectMapper;
    }
}
