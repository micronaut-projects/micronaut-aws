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
package io.micronaut.configuration.aws;

import com.amazonaws.ClientConfiguration;
import io.micronaut.context.annotation.BootstrapContextCompatible;
import io.micronaut.context.annotation.ConfigurationBuilder;
import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.context.annotation.Requires;

/**
 * Configuration options for AWS client.
 *
 * @author graemerocher
 * @since 1.0
 */
@ConfigurationProperties("client")
@Requires(classes = com.amazonaws.ClientConfiguration.class)
@BootstrapContextCompatible
public class AWSClientConfiguration extends AWSConfiguration {

    @ConfigurationBuilder
    protected ClientConfiguration clientConfiguration = new ClientConfiguration();

    /**
     * @return The AWS client configuration
     */
    public ClientConfiguration getClientConfiguration() {
        return clientConfiguration;
    }
}
