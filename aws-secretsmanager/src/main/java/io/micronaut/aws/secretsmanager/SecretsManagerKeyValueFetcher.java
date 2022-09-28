package io.micronaut.aws.secretsmanager;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micronaut.aws.distributedconfiguration.KeyValueFetcherByPath;
import io.micronaut.context.annotation.BootstrapContextCompatible;
import io.micronaut.context.annotation.Requires;
import io.micronaut.core.annotation.Experimental;
import io.micronaut.core.annotation.NonNull;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.*;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


/**
 * Class used for communicating directly with AWS sdk.
 * @author Sergio del Amo
 * @author Matej Nedic
 * @since ?
 */
@Experimental
@Requires(beans = {SecretsManagerClient.class})
@BootstrapContextCompatible
@Singleton
public class SecretsManagerKeyValueFetcher implements KeyValueFetcherByPath {
    private static final Logger LOG = LoggerFactory.getLogger(SecretsManagerKeyValueFetcher.class);

    //async?
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
    public Optional<Map<String,String>> keyValuesByPrefix(@NonNull String keyOrPath, String version) {
        Map<String,String> result = new HashMap<>();
        try {
                    Optional<String> secretValueOptional = fetchSecretValue(secretsClient, keyOrPath, version);
                    if (secretValueOptional.isPresent()) {
                        try {
                            result.putAll(objectMapper.readValue(secretValueOptional.get(), Map.class));
                        } catch (JsonProcessingException e) {
                            if (LOG.isWarnEnabled()) {
                                LOG.warn("could not read secret ({}) value from JSON to Map", keyOrPath);
                            }
                        }
                    }
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
                                              @NonNull String secretName,
                                              String versionId) {
        GetSecretValueRequest getSecretValueRequest;
        GetSecretValueRequest.Builder getSecretValueRequestBuilder = GetSecretValueRequest.builder()
                .secretId(secretName);
        if( versionId != null) {
            getSecretValueRequest = getSecretValueRequestBuilder.versionId(versionId).build();
        } else {
            getSecretValueRequest = getSecretValueRequestBuilder.build();
        }
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
