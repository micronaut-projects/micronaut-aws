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
package io.micronaut.aws.sdk.v2.service.s3;

import io.micronaut.aws.sdk.v2.service.ClientConfigurationProperties;
import io.micronaut.context.annotation.ConfigurationBuilder;
import io.micronaut.context.annotation.ConfigurationProperties;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.S3Configuration.Builder;

/**
 * Configuration properties for S3.
 *
 * @author Álvaro Sánchez-Mariscal
 * @since 2.0.0
 */
@ConfigurationProperties(S3Client.SERVICE_NAME)
public class S3ConfigurationProperties extends ClientConfigurationProperties {

    @ConfigurationBuilder(prefixes = {""}, excludes = {"profileFile", "applyMutation"})
    private Builder builder = S3Configuration.builder();

    /**
     * @return The builder
     */
    public Builder getBuilder() {
        return builder;
    }
}
