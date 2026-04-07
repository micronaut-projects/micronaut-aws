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
package io.micronaut.discovery.aws.parameterstore.imports;

import io.micronaut.aws.distributedconfiguration.imports.AwsConfigImportContextFactory;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.env.PropertySource;
import io.micronaut.context.exceptions.ConfigurationException;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.convert.value.ConvertibleValues;
import io.micronaut.core.util.ConnectionString;
import io.micronaut.core.util.StringUtils;
import io.micronaut.discovery.config.RetryablePropertySourceImporter;
import io.micronaut.discovery.aws.parameterstore.AWSParameterStoreConfiguration;
import io.micronaut.discovery.aws.servicediscovery.AwsServiceDiscoveryClientConfiguration;
import io.micronaut.retry.RetryOperationsFactory;
import io.micronaut.retry.RetryPolicy;
import software.amazon.awssdk.services.ssm.SsmAsyncClient;
import software.amazon.awssdk.services.ssm.model.GetParametersByPathRequest;
import software.amazon.awssdk.services.ssm.model.GetParametersByPathResponse;
import software.amazon.awssdk.services.ssm.model.GetParametersRequest;
import software.amazon.awssdk.services.ssm.model.GetParametersResponse;
import software.amazon.awssdk.services.ssm.model.Parameter;
import software.amazon.awssdk.services.ssm.model.ParameterType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Property source importer for AWS Parameter Store.
 *
 * <p>Supported credentials/configuration options may be supplied either in the connection string or the
 * structured map declaration. User-info in the connection string maps to `aws.access-key-id` and
 * `aws.secret-access-key`. Connection-string query parameters accept both prefixed and unprefixed aliases
 * for AWS credentials and region (for example `region` or `aws.region`). Supported option keys are
 * `aws.access-key-id` / `access-key-id`, `aws.secret-access-key` / `secret-access-key`,
 * `aws.secret-key` / `secret-key`, `aws.session-token` / `session-token`, `aws.region` / `region`,
 * `root-hierarchy-path`, `use-secure-parameters`,
 * `search-active-environments`, `retry-attempts`, `retry-count`, `retry-delay`, `retry-max-delay`,
 * `retry-multiplier`, and `retry-jitter`.</p>
 *
 * @since 5.0.0
 */
@Internal
public final class ParameterStorePropertySourceImporter extends RetryablePropertySourceImporter<ParameterStorePropertySourceImporter.ParameterStoreImport> {

    private static final int IMPORT_ORDER = io.micronaut.context.env.EnvironmentPropertySource.POSITION + 300;
    private final ParameterStoreContextSupplier contextSupplier;
    private final ParameterStoreImportSupport importSupport;

    /**
     * Default constructor.
     */
    public ParameterStorePropertySourceImporter() {
        this((environment, providerProperties) -> new AwsConfigImportContextFactory().build(environment, providerProperties, io.micronaut.context.env.Environment.AMAZON_EC2),
            new ParameterStoreImportSupport());
    }

    ParameterStorePropertySourceImporter(ParameterStoreContextSupplier contextSupplier,
                                         ParameterStoreImportSupport importSupport) {
        super();
        this.contextSupplier = contextSupplier;
        this.importSupport = importSupport;
    }

    ParameterStorePropertySourceImporter(RetryOperationsFactory retryOperationsFactory,
                                         ParameterStoreContextSupplier contextSupplier,
                                         ParameterStoreImportSupport importSupport) {
        super(retryOperationsFactory);
        this.contextSupplier = contextSupplier;
        this.importSupport = importSupport;
    }

    @Override
    public String getProvider() {
        return "parameterstore";
    }

    @Override
    protected ParameterStoreImport newImportDeclaration(ConnectionString connectionString, RetryPolicy retryPolicy) {
        String path = requirePath(connectionString.getPath());
        Map<String, Object> providerProperties = providerPropertiesFromConnectionString(connectionString);
        return new ParameterStoreImport(path, connectionString.isOptional(), providerProperties);
    }

    @Override
    protected ParameterStoreImport newImportDeclaration(ConvertibleValues<Object> values, RetryPolicy retryPolicy) {
        String path = requirePath(values.get("path", String.class).orElse(null));
        return new ParameterStoreImport(path, values.get("optional", Boolean.class).orElse(false), providerPropertiesFromValues(values));
    }

    @Override
    protected Optional<PropertySource> importRetryablePropertySource(ImportContext<ParameterStoreImport> context) {
        ParameterStoreImport declaration = context.importDeclaration();
        Map<String, Object> providerProperties = new LinkedHashMap<>(declaration.providerProperties());
        providerProperties.put(AWSParameterStoreConfiguration.ENABLED, true);
        try (ApplicationContext child = contextSupplier.build(context.environment(), providerProperties)) {
            SsmAsyncClient client = child.getBean(SsmAsyncClient.class);
            AWSParameterStoreConfiguration configuration = child.getBean(AWSParameterStoreConfiguration.class);
            Map<String, Object> properties = importSupport.load(client, configuration, declaration.path());
            if (properties.isEmpty()) {
                return Optional.empty();
            }
            return Optional.of(PropertySource.of(
                AwsServiceDiscoveryClientConfiguration.SERVICE_ID + '-' + declaration.path(),
                properties,
                IMPORT_ORDER
            ));
        } catch (Exception e) {
            if (declaration.optional()) {
                return Optional.empty();
            }
            if (e instanceof ConfigurationException configurationException) {
                throw configurationException;
            }
            throw new ConfigurationException("Error reading distributed configuration from AWS Parameter Store: " + e.getMessage(), e);
        }
    }

    private static String requirePath(String path) {
        if (path == null || path.isBlank()) {
            throw new ConfigurationException("AWS Parameter Store imports require a non-blank path");
        }
        String normalized = path.trim();
        if (!normalized.startsWith("/")) {
            normalized = "/" + normalized;
        }
        if (normalized.length() > 1 && normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }

    private static Map<String, Object> providerPropertiesFromConnectionString(ConnectionString connectionString) {
        Map<String, Object> properties = new LinkedHashMap<>();
        mapCredentials(connectionString.getUsername().orElse(null), connectionString.getPassword().orElse(null), properties);
        applyParameterStoreOptions(connectionString.getOptions(), properties);
        return properties;
    }

    private static Map<String, Object> providerPropertiesFromValues(ConvertibleValues<Object> values) {
        Map<String, Object> properties = new LinkedHashMap<>();
        copyValue(values, properties, "aws.access-key-id");
        copyValue(values, properties, "aws.secret-access-key");
        copyValue(values, properties, "aws.secret-key");
        copyValue(values, properties, "aws.session-token");
        copyValue(values, properties, "aws.region");
        mapIfPresent(values, properties, "root-hierarchy-path", AWSParameterStoreConfiguration.CONFIGURATION_PREFIX + ".root-hierarchy-path");
        mapIfPresent(values, properties, "use-secure-parameters", AWSParameterStoreConfiguration.CONFIGURATION_PREFIX + ".use-secure-parameters");
        mapIfPresent(values, properties, "search-active-environments", AWSParameterStoreConfiguration.CONFIGURATION_PREFIX + ".search-active-environments");
        return properties;
    }

    private static void applyParameterStoreOptions(Map<String, String> options, Map<String, Object> properties) {
        for (Map.Entry<String, String> entry : options.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            switch (key) {
                case "aws.access-key-id", "access-key-id" -> properties.put("aws.access-key-id", value);
                case "aws.secret-access-key", "secret-access-key" -> properties.put("aws.secret-access-key", value);
                case "aws.secret-key", "secret-key" -> properties.put("aws.secret-key", value);
                case "aws.session-token", "session-token" -> properties.put("aws.session-token", value);
                case "aws.region", "region" -> properties.put("aws.region", value);
                case "root-hierarchy-path" -> properties.put(AWSParameterStoreConfiguration.CONFIGURATION_PREFIX + ".root-hierarchy-path", value);
                case "use-secure-parameters" -> properties.put(AWSParameterStoreConfiguration.CONFIGURATION_PREFIX + ".use-secure-parameters", value);
                case "search-active-environments" -> properties.put(AWSParameterStoreConfiguration.CONFIGURATION_PREFIX + ".search-active-environments", value);
                case RETRY_ATTEMPTS, RETRY_COUNT, RETRY_DELAY, RETRY_MAX_DELAY, RETRY_MULTIPLIER, RETRY_JITTER -> {
                    // Retry settings are consumed by the retryable base class.
                }
                default -> throw new ConfigurationException("AWS Parameter Store imports do not support query option: " + key);
            }
        }
    }

    private static void mapCredentials(String username, String password, Map<String, Object> properties) {
        if (StringUtils.isEmpty(username) && StringUtils.isEmpty(password)) {
            return;
        }
        if (StringUtils.isEmpty(username) || StringUtils.isEmpty(password)) {
            throw new ConfigurationException("AWS Parameter Store connection string credentials must include both accessKeyId and secretAccessKey");
        }
        properties.put("aws.access-key-id", username);
        properties.put("aws.secret-access-key", password);
    }

    private static void copyValue(ConvertibleValues<Object> values, Map<String, Object> properties, String key) {
        values.get(key, String.class).ifPresent(value -> properties.put(key, value));
    }

    private static void mapIfPresent(ConvertibleValues<Object> values, Map<String, Object> properties, String sourceKey, String targetKey) {
        values.get(sourceKey, Object.class).ifPresent(value -> properties.put(targetKey, value));
    }

    @FunctionalInterface
    interface ParameterStoreContextSupplier {
        ApplicationContext build(io.micronaut.context.env.Environment environment, Map<String, Object> providerProperties);
    }

    @Internal
    static final class ParameterStoreImportSupport {

        Map<String, Object> load(SsmAsyncClient client,
                                 AWSParameterStoreConfiguration configuration,
                                 String path) {
            try {
                Map<String, Object> properties = new LinkedHashMap<>();
                List<Parameter> named = getNamed(client, configuration, path).get().parameters();
                if (!named.isEmpty()) {
                    properties.putAll(convert(path, named));
                }
                List<Parameter> hierarchy = getHierarchy(client, configuration, path, new ArrayList<>(), null);
                if (!hierarchy.isEmpty()) {
                    properties.putAll(convert(path, hierarchy));
                }
                return properties;
            } catch (Exception e) {
                throw new ConfigurationException("Error reading distributed configuration from AWS Parameter Store: " + e.getMessage(), e);
            }
        }

        private CompletableFuture<GetParametersResponse> getNamed(SsmAsyncClient client,
                                                                  AWSParameterStoreConfiguration configuration,
                                                                  String path) {
            return client.getParameters(GetParametersRequest.builder()
                .withDecryption(configuration.getUseSecureParameters())
                .names(path)
                .build());
        }

        private List<Parameter> getHierarchy(SsmAsyncClient client,
                                             AWSParameterStoreConfiguration configuration,
                                             String path,
                                             List<Parameter> parameters,
                                             String nextToken) throws Exception {
            GetParametersByPathResponse response = getHierarchyPage(client, configuration, path, nextToken).get();
            parameters.addAll(response.parameters());
            if (response.nextToken() != null) {
                return getHierarchy(client, configuration, path, parameters, response.nextToken());
            }
            return parameters;
        }

        private CompletableFuture<GetParametersByPathResponse> getHierarchyPage(SsmAsyncClient client,
                                                                                AWSParameterStoreConfiguration configuration,
                                                                                String path,
                                                                                String nextToken) {
            return client.getParametersByPath(GetParametersByPathRequest.builder()
                .withDecryption(configuration.getUseSecureParameters())
                .path(path)
                .recursive(true)
                .nextToken(nextToken)
                .build());
        }

        private Map<String, Object> convert(String path, List<Parameter> parameters) {
            Map<String, Object> output = new LinkedHashMap<>();
            for (Parameter param : parameters) {
                String name = param.name();
                if (!name.startsWith(path)) {
                    continue;
                }
                String key = name.substring(path.length());
                if (key.length() > 1) {
                    key = key.substring(1).replace("/", ".");
                }
                if (key.isEmpty()) {
                    continue;
                }
                if (ParameterType.STRING_LIST.equals(param.type())) {
                    output.put(key, Arrays.asList(param.value().split(",")));
                } else {
                    output.put(key, param.value());
                }
            }
            return output;
        }
    }

    @Internal
    record ParameterStoreImport(String path, boolean optional, Map<String, Object> providerProperties) {
    }
}
