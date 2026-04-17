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
package io.micronaut.aws.secretsmanager.imports;

import io.micronaut.aws.distributedconfiguration.imports.AwsConfigImportContextFactory;
import io.micronaut.aws.secretsmanager.SecretsManagerConfiguration;
import io.micronaut.aws.secretsmanager.SecretsManagerConfigurationProperties;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.env.PropertySource;
import io.micronaut.context.exceptions.ConfigurationException;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.convert.value.ConvertibleValues;
import io.micronaut.core.util.CollectionUtils;
import io.micronaut.core.util.ConnectionString;
import io.micronaut.core.util.StringUtils;
import io.micronaut.discovery.config.RetryablePropertySourceImporter;
import io.micronaut.json.JsonMapper;
import io.micronaut.retry.RetryOperationsFactory;
import io.micronaut.retry.RetryPolicy;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.ListSecretsRequest;
import software.amazon.awssdk.services.secretsmanager.model.SecretListEntry;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Property source importer for AWS Secrets Manager.
 *
 * <p>Supported credentials/configuration options may be supplied either in the connection string or the
 * structured map declaration. User-info in the connection string maps to `aws.access-key-id` and
 * `aws.secret-access-key`. Connection-string query parameters accept both prefixed and unprefixed aliases
 * for AWS credentials and region (for example `region` or `aws.region`). Supported option keys are
 * `aws.access-key-id` / `access-key-id`, `aws.secret-access-key` / `secret-access-key`,
 * `aws.secret-key` / `secret-key`, `aws.session-token` / `session-token`, and `aws.region` / `region`.
 * In the structured map form, secret mappings are supplied via `secrets` entries using `secret-name`
 * and optional `prefix` fields. Standard retry settings are also supported with `retry-attempts`,
 * `retry-count`, `retry-delay`, `retry-max-delay`, `retry-multiplier`, and `retry-jitter`.</p>
 *
 * @since 5.0.0
 */
@Internal
public final class SecretsManagerPropertySourceImporter extends RetryablePropertySourceImporter<SecretsManagerPropertySourceImporter.SecretsManagerImport> {

    private static final int IMPORT_ORDER = io.micronaut.context.env.EnvironmentPropertySource.POSITION + 300;
    private final SecretsManagerContextSupplier contextSupplier;
    private final SecretsManagerImportSupport importSupport;

    /**
     * Default constructor.
     */
    public SecretsManagerPropertySourceImporter() {
        this((environment, providerProperties) -> new AwsConfigImportContextFactory().build(environment, providerProperties),
            new SecretsManagerImportSupport());
    }

    SecretsManagerPropertySourceImporter(SecretsManagerContextSupplier contextSupplier,
                                         SecretsManagerImportSupport importSupport) {
        super();
        this.contextSupplier = contextSupplier;
        this.importSupport = importSupport;
    }

    SecretsManagerPropertySourceImporter(RetryOperationsFactory retryOperationsFactory,
                                         SecretsManagerContextSupplier contextSupplier,
                                         SecretsManagerImportSupport importSupport) {
        super(retryOperationsFactory);
        this.contextSupplier = contextSupplier;
        this.importSupport = importSupport;
    }

    @Override
    public String getProvider() {
        return "aws-secretsmanager";
    }

    @Override
    protected SecretsManagerImport newImportDeclaration(ConnectionString connectionString, RetryPolicy retryPolicy) {
        return new SecretsManagerImport(requirePath(connectionString.getPath()), connectionString.isOptional(), providerPropertiesFromConnectionString(connectionString));
    }

    @Override
    protected SecretsManagerImport newImportDeclaration(ConvertibleValues<Object> values, RetryPolicy retryPolicy) {
        return new SecretsManagerImport(requirePath(values.get("path", String.class).orElse(null)), values.get("optional", Boolean.class).orElse(false), providerPropertiesFromValues(values));
    }

    @Override
    protected Optional<PropertySource> importRetryablePropertySource(ImportContext<SecretsManagerImport> context) {
        SecretsManagerImport declaration = context.importDeclaration();
        Map<String, Object> providerProperties = new LinkedHashMap<>(declaration.providerProperties());
        providerProperties.put(SecretsManagerConfigurationProperties.PREFIX + ".enabled", true);
        try (ApplicationContext child = contextSupplier.build(context.environment(), providerProperties)) {
            SecretsManagerClient client = child.getBean(SecretsManagerClient.class);
            JsonMapper jsonMapper = child.getBean(JsonMapper.class);
            SecretsManagerConfiguration configuration = child.getBean(SecretsManagerConfiguration.class);
            Map<String, Map<String, Object>> groups = importSupport.load(client, jsonMapper, declaration.path());
            if (groups.isEmpty()) {
                return Optional.empty();
            }
            Map<String, Object> properties = new LinkedHashMap<>();
            for (Map.Entry<String, Map<String, Object>> entry : groups.entrySet()) {
                String groupName = entry.getKey();
                for (Map.Entry<String, Object> keyValue : entry.getValue().entrySet()) {
                    properties.putIfAbsent(importSupport.adaptPropertyKey(configuration, keyValue.getKey(), groupName), keyValue.getValue());
                }
            }
            if (properties.isEmpty()) {
                return Optional.empty();
            }
            return Optional.of(PropertySource.of("awssecretsmanager-" + declaration.path(), properties, IMPORT_ORDER));
        } catch (Exception e) {
            if (declaration.optional()) {
                return Optional.empty();
            }
            if (e instanceof ConfigurationException configurationException) {
                throw configurationException;
            }
            throw new ConfigurationException("Error reading distributed configuration from AWS Secrets Manager: " + e.getMessage(), e);
        }
    }

    private static String requirePath(String path) {
        if (path == null || path.isBlank()) {
            throw new ConfigurationException("AWS Secrets Manager imports require a non-blank path");
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
        applySecretManagerOptions(connectionString.getOptions(), properties);
        return properties;
    }

    private static Map<String, Object> providerPropertiesFromValues(ConvertibleValues<Object> values) {
        Map<String, Object> properties = new LinkedHashMap<>();
        copyAliasedValue(values, properties, "aws.access-key-id", "access-key-id");
        copyAliasedValue(values, properties, "aws.secret-access-key", "secret-access-key");
        copyAliasedValue(values, properties, "aws.secret-key", "secret-key");
        copyAliasedValue(values, properties, "aws.session-token", "session-token");
        copyAliasedValue(values, properties, "aws.region", "region");
        values.get("secrets", List.class).ifPresent(secrets -> properties.put(SecretsManagerConfigurationProperties.PREFIX + ".secrets", secrets));
        return properties;
    }

    private static void applySecretManagerOptions(Map<String, String> options, Map<String, Object> properties) {
        for (Map.Entry<String, String> entry : options.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            switch (key) {
                case "aws.access-key-id", "access-key-id" -> properties.put("aws.access-key-id", value);
                case "aws.secret-access-key", "secret-access-key" -> properties.put("aws.secret-access-key", value);
                case "aws.secret-key", "secret-key" -> properties.put("aws.secret-key", value);
                case "aws.session-token", "session-token" -> properties.put("aws.session-token", value);
                case "aws.region", "region" -> properties.put("aws.region", value);
                case RETRY_ATTEMPTS, RETRY_COUNT, RETRY_DELAY, RETRY_MAX_DELAY, RETRY_MULTIPLIER, RETRY_JITTER -> {
                    // Retry settings are consumed by the retryable base class.
                }
                default -> throw new ConfigurationException("AWS Secrets Manager imports do not support query option: " + key);
            }
        }
    }

    private static void mapCredentials(String username, String password, Map<String, Object> properties) {
        if (StringUtils.isEmpty(username) && StringUtils.isEmpty(password)) {
            return;
        }
        if (StringUtils.isEmpty(username) || StringUtils.isEmpty(password)) {
            throw new ConfigurationException("AWS Secrets Manager connection string credentials must include both accessKeyId and secretAccessKey");
        }
        properties.put("aws.access-key-id", username);
        properties.put("aws.secret-access-key", password);
    }

    private static void copyAliasedValue(ConvertibleValues<Object> values, Map<String, Object> properties, String canonicalKey, String alias) {
        if (copyValue(values, properties, canonicalKey, canonicalKey)) {
            return;
        }
        copyValue(values, properties, alias, canonicalKey);
    }

    private static boolean copyValue(ConvertibleValues<Object> values, Map<String, Object> properties, String sourceKey, String targetKey) {
        Optional<String> value = values.get(sourceKey, String.class);
        if (value.isPresent()) {
            properties.put(targetKey, value.get());
            return true;
        }
        return false;
    }

    @FunctionalInterface
    interface SecretsManagerContextSupplier {
        ApplicationContext build(io.micronaut.context.env.Environment environment, Map<String, Object> providerProperties);
    }

    @Internal
    static final class SecretsManagerImportSupport {

        Map<String, Map<String, Object>> load(SecretsManagerClient client,
                                              JsonMapper jsonMapper,
                                              String path) {
            try {
                Map<String, Map<String, Object>> result = new LinkedHashMap<>();
                String nextToken = null;
                do {
                    var builder = ListSecretsRequest.builder().nextToken(nextToken);
                    builder.filters(software.amazon.awssdk.services.secretsmanager.model.Filter.builder()
                        .key(software.amazon.awssdk.services.secretsmanager.model.FilterNameStringType.NAME)
                        .values(path)
                        .build());
                    var response = client.listSecrets(builder.build());
                    List<SecretListEntry> secrets = response.secretList();
                    for (SecretListEntry secret : secrets) {
                        String secretValue = client.getSecretValue(GetSecretValueRequest.builder().secretId(secret.name()).build()).secretString();
                        result.put(secret.name(), jsonMapper.readValue(secretValue, Map.class));
                    }
                    nextToken = response.nextToken();
                } while (nextToken != null);
                return result;
            } catch (IOException e) {
                throw new ConfigurationException("Error reading distributed configuration from AWS Secrets Manager: " + e.getMessage(), e);
            }
        }

        String adaptPropertyKey(SecretsManagerConfiguration configuration,
                                String originalKey,
                                String groupName) {
            if (CollectionUtils.isNotEmpty(configuration.getSecrets())) {
                for (SecretsManagerConfigurationProperties.SecretConfiguration secret : configuration.getSecrets()) {
                    if (groupName.endsWith(secret.getSecretName())) {
                        return secret.getPrefix() + "." + originalKey;
                    }
                }
            }
            return originalKey;
        }
    }

    @Internal
    record SecretsManagerImport(String path, boolean optional, Map<String, Object> providerProperties) {
    }
}
