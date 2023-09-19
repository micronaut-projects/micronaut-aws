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
package io.micronaut.aws.distributedconfiguration;

import io.micronaut.context.env.Environment;
import io.micronaut.context.env.MapPropertySource;
import io.micronaut.context.env.PropertySource;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.async.publisher.Publishers;
import io.micronaut.discovery.config.ConfigurationClient;
import io.micronaut.runtime.ApplicationConfiguration;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;

/**
 * Base implementation for AWS services contributing distributed configuration.
 *
 * @author Sergio del Amo
 * @since 2.8.0
 */
public abstract class AwsDistributedConfigurationClient implements ConfigurationClient {
    private static final Logger LOG = LoggerFactory.getLogger(AwsDistributedConfigurationClient.class);
    private static final String UNDERSCORE = "_";
    private final AwsDistributedConfiguration awsDistributedConfiguration;
    private final KeyValueFetcher keyValueFetcher;

    @Nullable
    private final String applicationName;

    /**
     *
     * @param awsDistributedConfiguration AWS Distributed Configuration
     * @param keyValueFetcher Key Value Fetcher
     * @param applicationConfiguration Application Configuration
     */
    public AwsDistributedConfigurationClient(AwsDistributedConfiguration awsDistributedConfiguration,
                                             KeyValueFetcher keyValueFetcher,
                                             @Nullable ApplicationConfiguration applicationConfiguration) {
        this.awsDistributedConfiguration = awsDistributedConfiguration;
        this.keyValueFetcher = keyValueFetcher;
        this.applicationName = applicationConfiguration == null ? null : (applicationConfiguration.getName().orElse(null));
        if (LOG.isTraceEnabled()) {
            if (this.applicationName != null) {
                LOG.trace("application name: {}", applicationName);
            } else {
                LOG.trace("application name not set");
            }
        }
    }

    @Override
    public Publisher<PropertySource> getPropertySources(Environment environment) {
        List<String> configurationResolutionPrefixes = generateConfigurationResolutionPrefixes(environment);
        Map<String, Map<String, Map<String, Object>>> configurationResolutionPrefixKeyValueGroups = new LinkedHashMap<>();
        int allKeysCount = 0;

        for (String prefix : configurationResolutionPrefixes) {
            Optional<Map> keyValueGroupsOptional = keyValueFetcher.keyValuesByPrefix(prefix);
            if (keyValueGroupsOptional.isPresent()) {
                Map<String, Map<String, Object>> keyValueGroups = keyValueGroupsOptional.get();
                configurationResolutionPrefixKeyValueGroups.put(prefix, keyValueGroups);
                for (Map<String, ?> keyValues: keyValueGroups.values()) {
                    allKeysCount += keyValues.size();
                }
            }
        }
        if (LOG.isTraceEnabled()) {
            LOG.trace("evaluating {} keys", allKeysCount);
        }
        Map<String, Object> result = new HashMap<>();
        for (Map.Entry<String, Map<String, Map<String, Object>>> configurationMapEntry : configurationResolutionPrefixKeyValueGroups.entrySet()) {
            String prefix = configurationMapEntry.getKey();
            Map<String, Map<String, Object>> keyValueGroups = configurationMapEntry.getValue();
            for (Map.Entry<String, Map<String, Object>> keyValueGroupEntry : keyValueGroups.entrySet()) {
                String groupName = keyValueGroupEntry.getKey();
                Map<String, ?> keyValues = keyValueGroupEntry.getValue();

                for (Map.Entry<String, ?> keyValuesEntry: keyValues.entrySet()) {
                    String key = keyValuesEntry.getKey();
                    String adaptedPropertyKey = adaptPropertyKey(key, groupName);
                    if (!result.containsKey(adaptedPropertyKey)) {
                        if (LOG.isTraceEnabled()) {
                            LOG.trace("adding property {} from prefix {}", adaptedPropertyKey, prefix);
                        }
                        result.put(adaptedPropertyKey, keyValuesEntry.getValue());
                    }
                }

            }
        }
        String propertySourceName = getPropertySourceName();
        if (LOG.isDebugEnabled()) {
            LOG.debug("Property source {} with #{} items", propertySourceName, result.size());
        }
        if (LOG.isTraceEnabled()) {
            for (String k : result.keySet()) {
                LOG.trace("property {} resolved", k);
            }
        }
        return Publishers.just(new MapPropertySource(propertySourceName, result));
    }

    /**
     * Adapts an original key. For example, key could be appended to a prefix in order to avoid naming ambiguity.
     * *
     * @since 3.8.0
     * @param originalKey an original property key
     * @param groupName a property group name
     * @return An adapted property key (e.g. key that has been appended to a prefix)
     */
    @NonNull
    protected abstract String adaptPropertyKey(String originalKey, String groupName);

    /**
     *
     * @return The name of the property source
     */
    @NonNull
    protected abstract String getPropertySourceName();

    /**
     *
     * @param environment Micronaut's Environment
     * @return A List of prefixes ordered by precedence
     */
    @NonNull
    private List<String> generateConfigurationResolutionPrefixes(@NonNull Environment environment) {
        List<String> configurationResolutionPrefixes = new ArrayList<>();
        if (awsDistributedConfiguration.isDefaultConfigEnabled()) {
            if (applicationName != null) {
                if (awsDistributedConfiguration.isSearchActiveEnvironments()) {
                    for (String name : environment.getActiveNames()) {
                        configurationResolutionPrefixes.addAll(prefix(applicationName, name));
                    }
                }
                configurationResolutionPrefixes.addAll(prefix(applicationName));
            }
            if (awsDistributedConfiguration.isSearchCommonApplication()) {
                if (awsDistributedConfiguration.isSearchActiveEnvironments()) {
                    for (String name : environment.getActiveNames()) {
                        configurationResolutionPrefixes.addAll(prefix(awsDistributedConfiguration.getCommonApplicationName(), name));
                    }
                }
                configurationResolutionPrefixes.addAll(prefix(awsDistributedConfiguration.getCommonApplicationName()));
            }
        }
        return configurationResolutionPrefixes;
    }

    @NonNull
    private List<String> prefix(@NonNull String appName) {
        return prefix(appName, null);
    }

    @NonNull
    private List<String> prefix(@NonNull String appName, @Nullable String envName) {
        List<String> prefixes = awsDistributedConfiguration.getPrefixes().isEmpty() ?
            singletonList(awsDistributedConfiguration.getPrefix()) :
            awsDistributedConfiguration.getPrefixes();

        return prefixes.stream()
            .map(p -> buildPrefix(p, appName, envName))
            .collect(toList());
    }

    private String buildPrefix(@NonNull String inputPrefix, @NonNull String appName, @Nullable String envName) {
        String delimiter = awsDistributedConfiguration.getDelimiter();
        if (envName != null) {
            return inputPrefix + appName + UNDERSCORE + envName + delimiter;
        }
        return inputPrefix + appName + delimiter;
    }
}
