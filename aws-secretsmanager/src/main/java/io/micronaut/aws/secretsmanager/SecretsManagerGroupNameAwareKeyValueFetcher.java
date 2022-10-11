/*
 * Copyright 2017-2021 original authors
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
package io.micronaut.aws.secretsmanager;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micronaut.context.annotation.BootstrapContextCompatible;
import io.micronaut.context.annotation.Requires;
import io.micronaut.core.annotation.Experimental;
import io.micronaut.core.annotation.NonNull;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.SecretListEntry;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 *  Key Value fetcher for AWS Secrets Manager.
 *
 * @author sbodvanski
 * @since 3.8.0
 */
@Experimental
@Requires(beans = {SecretsManagerClient.class})
@BootstrapContextCompatible
@Singleton
public class SecretsManagerGroupNameAwareKeyValueFetcher extends SecretsManagerKeyValueFetcher {
    private static final Logger LOG = LoggerFactory.getLogger(SecretsManagerGroupNameAwareKeyValueFetcher.class);

    /**
     * @param secretsClient Secrets Client
     * @param objectMapper Object Mapper
     */
    public SecretsManagerGroupNameAwareKeyValueFetcher(SecretsManagerClient secretsClient,
                                                       ObjectMapper objectMapper) {
        super(secretsClient, objectMapper);
    }

    @Override
    @NonNull
    protected void addSecretDetailsToResults(SecretListEntry secret, Map result) {
        Map<String, Object> keyValues = new HashMap<>();
        Optional<String> secretValueOptional = fetchSecretValue(secretsClient, secret.name());
        if (secretValueOptional.isPresent()) {
            try {
                keyValues.putAll(objectMapper.readValue(secretValueOptional.get(), Map.class));
                result.put(secret.name(), keyValues);
            } catch (JsonProcessingException e) {
                if (LOG.isWarnEnabled()) {
                    LOG.warn("could not read secret ({}) value from JSON to Map", secret.name());
                }
            }
        }
    }
}
