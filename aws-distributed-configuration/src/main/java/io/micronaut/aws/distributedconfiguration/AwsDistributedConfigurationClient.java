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
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Base implementation for AWS services contributing distributed configuration.
 *
 * @author Sergio del Amo
 * @since 2.7.0
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
     * @param keyValueFetcher a Key Value Fetcher
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

        Map<String, Map> configurationResolutionPrefixesValues = new HashMap<>();

        for (String prefix : configurationResolutionPrefixes) {
            Optional<Map> keyValuesOptional = keyValueFetcher.keyValuesByPrefix(prefix);
            if (keyValuesOptional.isPresent()) {
                Map keyValues = keyValuesOptional.get();
                configurationResolutionPrefixesValues.put(prefix, keyValues);
            }
        }
        Set<String> allKeys = new HashSet<>();
        for (Map m : configurationResolutionPrefixesValues.values()) {
            allKeys.addAll(m.keySet());
        }
        Map<String, Object> result = new HashMap<>();
        if (LOG.isTraceEnabled()) {
            LOG.trace("evaluating {} keys", allKeys.size());
        }
        for (String k : allKeys) {
            if (!result.containsKey(k)) {
                for (String prefix : configurationResolutionPrefixes) {
                    if (configurationResolutionPrefixesValues.containsKey(prefix)) {
                        Map<String, ?> values = configurationResolutionPrefixesValues.get(prefix);
                        if (values.containsKey(k)) {
                            if (LOG.isTraceEnabled()) {
                                LOG.trace("adding property {} from prefix {}", k, prefix);
                            }
                            result.put(k, values.get(k));
                            break;
                        }
                    }
                }
            }
        }
        String propertySourceName = getPropertySourceName();
        if (LOG.isDebugEnabled()) {
            LOG.debug("Property source {} with #{} items", propertySourceName, result.size());
        }
        return Publishers.just(new MapPropertySource(propertySourceName, result));
    }

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
        if (applicationName != null && awsDistributedConfiguration.isSearchActiveEnvironments()) {
            for (String name : environment.getActiveNames()) {
                configurationResolutionPrefixes.add(awsDistributedConfiguration.getLeadingDelimiter() + String.join(awsDistributedConfiguration.getDelimiter(), Arrays.asList(awsDistributedConfiguration.getPrefix(), applicationName + UNDERSCORE + name)) + awsDistributedConfiguration.getTrailingDelimiter());
            }
        }
        if (applicationName != null) {
            configurationResolutionPrefixes.add(awsDistributedConfiguration.getLeadingDelimiter() + String.join(awsDistributedConfiguration.getDelimiter(), Arrays.asList(awsDistributedConfiguration.getPrefix(), applicationName)) + awsDistributedConfiguration.getTrailingDelimiter());
        }
        if (awsDistributedConfiguration.isSearchActiveEnvironments()) {
            for (String name : environment.getActiveNames()) {
                configurationResolutionPrefixes.add(awsDistributedConfiguration.getLeadingDelimiter() + String.join(awsDistributedConfiguration.getDelimiter(), Arrays.asList(awsDistributedConfiguration.getPrefix(), awsDistributedConfiguration.getSharedConfigurationName() + UNDERSCORE + name)) + awsDistributedConfiguration.getTrailingDelimiter());
            }
        }
        configurationResolutionPrefixes.add(awsDistributedConfiguration.getLeadingDelimiter() + String.join(awsDistributedConfiguration.getDelimiter(), Arrays.asList(awsDistributedConfiguration.getPrefix(), awsDistributedConfiguration.getSharedConfigurationName())) + awsDistributedConfiguration.getTrailingDelimiter());

        return configurationResolutionPrefixes;
    }
}
