/*
 * Copyright 2017-2026 original authors
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
package io.micronaut.aws.sdk.v2.dev;

import io.micronaut.aws.AWSConfiguration;
import io.micronaut.aws.sdk.v2.EnvironmentAwsCredentialsProvider;
import io.micronaut.aws.sdk.v2.EnvironmentAwsRegionProvider;
import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.context.env.DevelopmentActive;
import io.micronaut.core.annotation.Internal;
import org.jspecify.annotations.Nullable;

/**
 * The values under {@code aws} that {@link EnvironmentAwsCredentialsProvider} and {@link EnvironmentAwsRegionProvider}
 * read from the environment, bound in development mode only, so that the providers made from them hold a copy rather
 * than the environment: a provider holding the environment, or the context, keeps every client built on it from
 * being retained across a restart.
 *
 * <p>The values are bound when the context binds this bean. Outside development mode, the environment providers
 * read them on every call instead; in development mode a change under {@code aws} releases the retained clients and
 * the providers they hold, through {@link io.micronaut.context.annotation.Retain#invalidatedBy()}, and the next
 * context binds the values again, so the providers never serve stale ones.</p>
 *
 * @author graemerocher
 * @since 5.2.0
 */
@Internal
@DevelopmentActive
@ConfigurationProperties(AWSConfiguration.PREFIX)
final class AwsEnvironmentSnapshot {

    private @Nullable String accessKeyId;
    private @Nullable String accessKey;
    private @Nullable String secretKey;
    private @Nullable String secretAccessKey;
    private @Nullable String sessionToken;
    private @Nullable String region;

    /**
     * @return The value of {@value EnvironmentAwsCredentialsProvider#ACCESS_KEY_ENV_VAR}
     */
    public @Nullable String getAccessKeyId() {
        return accessKeyId;
    }

    /**
     * @param accessKeyId The value of {@value EnvironmentAwsCredentialsProvider#ACCESS_KEY_ENV_VAR}
     */
    public void setAccessKeyId(@Nullable String accessKeyId) {
        this.accessKeyId = accessKeyId;
    }

    /**
     * @return The value of {@value EnvironmentAwsCredentialsProvider#ALTERNATE_ACCESS_KEY_ENV_VAR}
     */
    public @Nullable String getAccessKey() {
        return accessKey;
    }

    /**
     * @param accessKey The value of {@value EnvironmentAwsCredentialsProvider#ALTERNATE_ACCESS_KEY_ENV_VAR}
     */
    public void setAccessKey(@Nullable String accessKey) {
        this.accessKey = accessKey;
    }

    /**
     * @return The value of {@value EnvironmentAwsCredentialsProvider#SECRET_KEY_ENV_VAR}
     */
    public @Nullable String getSecretKey() {
        return secretKey;
    }

    /**
     * @param secretKey The value of {@value EnvironmentAwsCredentialsProvider#SECRET_KEY_ENV_VAR}
     */
    public void setSecretKey(@Nullable String secretKey) {
        this.secretKey = secretKey;
    }

    /**
     * @return The value of {@value EnvironmentAwsCredentialsProvider#ALTERNATE_SECRET_KEY_ENV_VAR}
     */
    public @Nullable String getSecretAccessKey() {
        return secretAccessKey;
    }

    /**
     * @param secretAccessKey The value of {@value EnvironmentAwsCredentialsProvider#ALTERNATE_SECRET_KEY_ENV_VAR}
     */
    public void setSecretAccessKey(@Nullable String secretAccessKey) {
        this.secretAccessKey = secretAccessKey;
    }

    /**
     * @return The value of {@value EnvironmentAwsCredentialsProvider#AWS_SESSION_TOKEN_ENV_VAR}
     */
    public @Nullable String getSessionToken() {
        return sessionToken;
    }

    /**
     * @param sessionToken The value of {@value EnvironmentAwsCredentialsProvider#AWS_SESSION_TOKEN_ENV_VAR}
     */
    public void setSessionToken(@Nullable String sessionToken) {
        this.sessionToken = sessionToken;
    }

    /**
     * @return The value of {@value EnvironmentAwsRegionProvider#REGION_ENV_VAR}
     */
    public @Nullable String getRegion() {
        return region;
    }

    /**
     * @param region The value of {@value EnvironmentAwsRegionProvider#REGION_ENV_VAR}
     */
    public void setRegion(@Nullable String region) {
        this.region = region;
    }
}
