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

import io.micronaut.aws.sdk.v2.EnvironmentAwsCredentialsProvider;
import io.micronaut.core.annotation.Internal;
import org.jspecify.annotations.Nullable;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.AwsSessionCredentials;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.utils.StringUtils;

import static io.micronaut.aws.sdk.v2.EnvironmentAwsCredentialsProvider.ACCESS_KEY_ENV_VAR;
import static io.micronaut.aws.sdk.v2.EnvironmentAwsCredentialsProvider.ALTERNATE_ACCESS_KEY_ENV_VAR;
import static io.micronaut.aws.sdk.v2.EnvironmentAwsCredentialsProvider.ALTERNATE_SECRET_KEY_ENV_VAR;
import static io.micronaut.aws.sdk.v2.EnvironmentAwsCredentialsProvider.SECRET_KEY_ENV_VAR;

/**
 * The development mode counterpart of {@link EnvironmentAwsCredentialsProvider}: it resolves the same credentials,
 * by the same rules, from the values it copied when it was made rather than from the environment, which it does not
 * hold. The access key is {@value EnvironmentAwsCredentialsProvider#ACCESS_KEY_ENV_VAR}, or else
 * {@value EnvironmentAwsCredentialsProvider#ALTERNATE_ACCESS_KEY_ENV_VAR}, the secret key
 * {@value EnvironmentAwsCredentialsProvider#SECRET_KEY_ENV_VAR}, or else
 * {@value EnvironmentAwsCredentialsProvider#ALTERNATE_SECRET_KEY_ENV_VAR}, both trimmed, with the trimmed
 * {@value EnvironmentAwsCredentialsProvider#AWS_SESSION_TOKEN_ENV_VAR} making session credentials; when either key is
 * blank it throws, so that the chain asks the next provider.
 *
 * <p>The environment provider reads the values on every call, so that it follows a refreshed environment. This one
 * copies them once, and in development mode a change under {@code aws} releases it, with the clients holding it, and
 * the next context makes another from the changed values.</p>
 *
 * @author graemerocher
 * @since 5.2.0
 */
@Internal
final class CopiedAwsCredentialsProvider implements AwsCredentialsProvider {

    private final @Nullable AwsCredentials credentials;

    /**
     * @param environment The values copied from the environment
     */
    CopiedAwsCredentialsProvider(AwsEnvironmentSnapshot environment) {
        String accessKey = StringUtils.trim(environment.getAccessKeyId() != null ? environment.getAccessKeyId() : environment.getAccessKey());
        String secretKey = StringUtils.trim(environment.getSecretKey() != null ? environment.getSecretKey() : environment.getSecretAccessKey());
        String sessionToken = StringUtils.trim(environment.getSessionToken());
        if (StringUtils.isBlank(accessKey) || StringUtils.isBlank(secretKey)) {
            credentials = null;
        } else {
            credentials = sessionToken == null
                ? AwsBasicCredentials.create(accessKey, secretKey)
                : AwsSessionCredentials.create(accessKey, secretKey, sessionToken);
        }
    }

    @Override
    public AwsCredentials resolveCredentials() {
        if (credentials == null) {
            throw SdkClientException.create(
                "Unable to load AWS credentials from environment "
                    + "(" + ACCESS_KEY_ENV_VAR + " (or " + ALTERNATE_ACCESS_KEY_ENV_VAR + ") and "
                    + SECRET_KEY_ENV_VAR + " (or " + ALTERNATE_SECRET_KEY_ENV_VAR + "))");
        }
        return credentials;
    }

    @Override
    public String toString() {
        // as the environment provider, which never shows the credentials
        return EnvironmentAwsCredentialsProvider.class.getSimpleName();
    }
}
