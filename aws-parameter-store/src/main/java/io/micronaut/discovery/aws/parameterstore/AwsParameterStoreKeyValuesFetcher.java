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
package io.micronaut.discovery.aws.parameterstore;

import io.micronaut.aws.distributedconfiguration.KeyValueFetcher;
import io.micronaut.context.annotation.BootstrapContextCompatible;
import io.micronaut.core.annotation.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.GetParametersByPathRequest;
import software.amazon.awssdk.services.ssm.model.GetParametersByPathResponse;
import software.amazon.awssdk.services.ssm.model.Parameter;
import software.amazon.awssdk.services.ssm.model.ParameterType;

import javax.inject.Singleton;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author Sergio del Amo
 * @since 2.7.0
 */
@Singleton
@BootstrapContextCompatible
public class AwsParameterStoreKeyValuesFetcher implements KeyValueFetcher {
    private static final Logger LOG = LoggerFactory.getLogger(AwsParameterStoreKeyValuesFetcher.class);

    private final AWSParameterStoreConfigurationProperties awsParameterStoreConfiguration;
    private final SsmClient ssmClient;

    /**
     *
     * @param awsParameterStoreConfiguration AWS Parameter store configuration
     * @param ssmClient AWS Systems Manager Client
     */
    public AwsParameterStoreKeyValuesFetcher(AWSParameterStoreConfigurationProperties awsParameterStoreConfiguration,
                                             SsmClient ssmClient) {
        this.awsParameterStoreConfiguration = awsParameterStoreConfiguration;
        this.ssmClient = ssmClient;
    }

    @Override
    @NonNull
    public Optional<Map> keyValuesByPrefix(@NonNull String prefix) {
        String nextToken = null;
        Map<String, Object> result = new HashMap<>();
        do {
            GetParametersByPathRequest request = GetParametersByPathRequest.builder()
                    .withDecryption(awsParameterStoreConfiguration.getUseSecureParameters())
                    .path(prefix)
                    .recursive(true)
                    .nextToken(nextToken)
                    .build();
            Optional<GetParametersByPathResponse> responseOptional = exchange(request);
            if (responseOptional.isPresent()) {
                GetParametersByPathResponse response = responseOptional.get();
                parametersMap(prefix, response).ifPresent(result::putAll);
                nextToken = response.nextToken();
            } else {
                nextToken = null;
            }
        } while (nextToken != null);

        return Optional.of(result);
    }

    /**
     *
     * @param prefix Prefix
     * @param response Get Parameters by Path response
     * @return A a Configuration Map for the repsonse
     */
    @NonNull
    protected Optional<Map<String, Object>> parametersMap(@NonNull String prefix,
                                                          @NonNull GetParametersByPathResponse response) {
        if (response.hasParameters()) {
            List<Parameter> params = response.parameters();
            return Optional.of(parametersToMap(prefix, params));
        }
        return Optional.empty();
    }

    /**
     *
     * @param getParametersByPathRequest Get Parameters by Path request
     * @return An GetParametersBytPath response
     */
    protected Optional<GetParametersByPathResponse> exchange(GetParametersByPathRequest getParametersByPathRequest) {
        try {
            GetParametersByPathResponse getParametersByPathResponse = ssmClient.getParametersByPath(getParametersByPathRequest);
            return Optional.of(getParametersByPathResponse);
        } catch (AwsServiceException | SdkClientException e) {
            if (LOG.isWarnEnabled()) {
                LOG.warn("Exception getting parameters by path", e);
            }
            return Optional.empty();
        }
    }

    /**
     * Helper method for converting parameters from amazon format to a map.
     *
     * @param prefix Prefix
     * @param params AWS Parameter Store parameters
     * @return map of the results, converted
     */
    @NonNull
    private Map<String, Object> parametersToMap(@NonNull String prefix, List<Parameter> params) {
        Map<String, Object> output = new HashMap<>();
        for (Parameter param : params) {
            String key = parseKeyFromParameter(prefix, param);
            output.put(key, parseValueFromParameter(param));
        }
        if (LOG.isTraceEnabled()) {
            LOG.trace("Converted {}", output);
        }
        return output;
    }

    /**
     *
     * @param param AWS Parameter Store Parameter
     * @return Value
     */
    @NonNull
    protected Object parseValueFromParameter(@NonNull Parameter param) {
        if (ParameterType.STRING_LIST.equals(param.type())) {
            String[] items = param.value().split(",");
            return Arrays.asList(items);
        }
        return param.value();
    }

    /**
     *
     * @param prefix Prefix
     * @param param AWS Parameter Store Parameter
     * @return the key for a given Parameter
     */
    @NonNull
    protected String parseKeyFromParameter(@NonNull String prefix, @NonNull Parameter param) {
        return param.name().substring(prefix.length());
    }
}
