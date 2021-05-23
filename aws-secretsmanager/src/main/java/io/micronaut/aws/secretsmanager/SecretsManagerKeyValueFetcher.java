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
import io.micronaut.aws.distributedconfiguration.KeyValueFetcher;
import io.micronaut.context.annotation.BootstrapContextCompatible;
import io.micronaut.context.annotation.Requires;
import io.micronaut.core.annotation.Experimental;
import io.micronaut.core.annotation.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.DecryptionFailureException;
import software.amazon.awssdk.services.secretsmanager.model.Filter;
import software.amazon.awssdk.services.secretsmanager.model.FilterNameStringType;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;
import software.amazon.awssdk.services.secretsmanager.model.InternalServiceErrorException;
import software.amazon.awssdk.services.secretsmanager.model.InvalidParameterException;
import software.amazon.awssdk.services.secretsmanager.model.InvalidRequestException;
import software.amazon.awssdk.services.secretsmanager.model.ListSecretsRequest;
import software.amazon.awssdk.services.secretsmanager.model.ListSecretsResponse;
import software.amazon.awssdk.services.secretsmanager.model.ResourceNotFoundException;
import software.amazon.awssdk.services.secretsmanager.model.SecretListEntry;
import software.amazon.awssdk.services.secretsmanager.model.SecretsManagerException;
import javax.inject.Singleton;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * {@link KeyValueFetcher} implementations for AWS Secrets Manager.
 * @author Sergio del Amo
 * @since 2.7.0
 */
@Experimental
@Requires(beans = {SecretsManagerClient.class})
@BootstrapContextCompatible
@Singleton
public class SecretsManagerKeyValueFetcher implements KeyValueFetcher {
    private static final Logger LOG = LoggerFactory.getLogger(SecretsManagerKeyValueFetcher.class);

    private final SecretsManagerClient secretsClient;
    private final ObjectMapper objectMapper;

    /**
     *
     * @param secretsClient Secrets Client
     * @param objectMapper Object Mapper
     */
    public SecretsManagerKeyValueFetcher(SecretsManagerClient secretsClient,
                                         ObjectMapper objectMapper) {
        this.secretsClient = secretsClient;
        this.objectMapper = objectMapper;
    }

    @Override
    @NonNull
    public Optional<Map> keyValuesByPrefix(@NonNull String prefix) {
        Map result = new HashMap<>();
        try {
            String nextToken = null;
            do {
                ListSecretsRequest.Builder builder = ListSecretsRequest.builder()
                        .nextToken(nextToken)
                        .filters(Filter.builder()
                                .key(FilterNameStringType.NAME)
                                .values(prefix)
                                .build());
                if (nextToken != null) {
                    builder = builder.nextToken(nextToken);
                }
                ListSecretsRequest listSecretsRequest = builder.build();
                ListSecretsResponse secretsResponse = secretsClient.listSecrets(listSecretsRequest);
                List<SecretListEntry> secrets = secretsResponse.secretList();
                if (LOG.isTraceEnabled()) {
                    if (secrets.isEmpty()) {
                        LOG.trace("zero secrets for prefix: {}", prefix);
                    } else {
                        LOG.trace("# {} secrets for prefix: {}", secrets.size(), prefix);
                    }
                }
                for (SecretListEntry secret : secrets) {
                    if (LOG.isTraceEnabled()) {
                        LOG.trace("Evaluating secret {}", secret.name());
                    }
                    Optional<String> secretValueOptional = fetchSecretValue(secretsClient, secret.name());
                    if (secretValueOptional.isPresent()) {
                        try {
                            result.putAll(objectMapper.readValue(secretValueOptional.get(), Map.class));
                        } catch (JsonProcessingException e) {
                            if (LOG.isWarnEnabled()) {
                                LOG.warn("could not read secret ({}) value from JSON to Map", secret.name());
                            }
                        }
                    }
                }

                nextToken = secretsResponse.nextToken();
            } while (nextToken != null);


        } catch (SecretsManagerException e) {
            if (LOG.isWarnEnabled()) {
                LOG.warn("SecretsManagerException {}", e.awsErrorDetails().errorMessage());
            }
            return Optional.empty();
        }
        return result.isEmpty() ? Optional.empty() : Optional.of(result);
    }

    @NonNull
    private Optional<String> fetchSecretValue(@NonNull SecretsManagerClient secretsClient,
                                              @NonNull String secretName) {
        GetSecretValueRequest getSecretValueRequest = GetSecretValueRequest.builder()
                .secretId(secretName)
                .build();
        return fetchSecretValueResponse(secretsClient, getSecretValueRequest)
                .map(this::extractSecretValue);
    }

    /**
     *  Decrypts secret using the associated KMS CMK.
     * @param getSecretValueResponse Secret Value response
     * @return Secret's value depending on whether the secret is a string or binary.
     */
    @NonNull
    private String extractSecretValue(@NonNull GetSecretValueResponse getSecretValueResponse) {
        if (getSecretValueResponse.secretString() != null) {
            return getSecretValueResponse.secretString();
        }
        return new String(Base64.getDecoder().decode(getSecretValueResponse.secretBinary().asByteBuffer()).array());
    }

    @NonNull
    private Optional<GetSecretValueResponse> fetchSecretValueResponse(@NonNull SecretsManagerClient secretsClient,
                                                                      @NonNull GetSecretValueRequest getSecretValueRequest) {
        try {
            return Optional.of(secretsClient.getSecretValue(getSecretValueRequest));
        } catch (DecryptionFailureException e) {
            if (LOG.isWarnEnabled()) {
                LOG.warn("Secrets Manager can't decrypt the protected secret ({}) text using the provided KMS key.",
                        getSecretValueRequest.secretId());
            }
        } catch (InternalServiceErrorException e) {
            if (LOG.isWarnEnabled()) {
                LOG.warn("An error occurred on the server side getting secret ({}) value",
                        getSecretValueRequest.secretId());
            }
        } catch (InvalidParameterException e) {
            if (LOG.isWarnEnabled()) {
                LOG.warn("You provided an invalid value for a parameter while getting secret ({}) value",
                        getSecretValueRequest.secretId());
            }
        } catch (InvalidRequestException e) {
            if (LOG.isWarnEnabled()) {
                LOG.warn("While getting the secret value, you provided a parameter value that is not valid for the current state of the secret ({})",
                        getSecretValueRequest.secretId());
            }
        } catch (ResourceNotFoundException e) {
            if (LOG.isWarnEnabled()) {
                LOG.warn("Could not find the resource for secret ({})", getSecretValueRequest.secretId());
            }
        }
        return Optional.empty();

    }
}
