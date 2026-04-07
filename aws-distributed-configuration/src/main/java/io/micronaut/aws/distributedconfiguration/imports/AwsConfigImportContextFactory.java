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
package io.micronaut.aws.distributedconfiguration.imports;

import io.micronaut.context.ApplicationContext;
import io.micronaut.context.ApplicationContextBuilder;
import io.micronaut.context.env.Environment;
import io.micronaut.context.env.PropertySource;
import io.micronaut.core.annotation.Internal;
import io.micronaut.discovery.config.ConfigurationClient;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Builds importer-owned child contexts for AWS config import support.
 *
 * @since 5.0.0
 */
@Internal
public final class AwsConfigImportContextFactory {

    private static final Set<String> INCLUDED_PACKAGES = Set.of(
        "io.micronaut.context",
        "io.micronaut.core",
        "io.micronaut.aws.sdk.v2",
        "io.micronaut.aws.ua",
        "io.micronaut.discovery.aws.parameterstore",
        "io.micronaut.aws.distributedconfiguration",
        "io.micronaut.aws.secretsmanager",
        "io.micronaut.http",
        "io.micronaut.http.client",
        "io.micronaut.jackson",
        "io.micronaut.json",
        "io.micronaut.runtime",
        "io.micronaut.retry"
    );

    private static final Set<String> BASE_DISABLED_PROPERTIES = Set.of(
        ConfigurationClient.ENABLED,
        Environment.BOOTSTRAP_CONTEXT_PROPERTY,
        "aws.client.system-manager.parameterstore.enabled",
        "aws.secretsmanager.enabled"
    );

    /**
     * Default constructor.
     */
    public AwsConfigImportContextFactory() {
    }

    /**
     * Build a child context with provider-specific properties.
     *
     * @param environment The parent environment
     * @param providerProperties Provider-specific properties to enable
     * @param extraEnvironments Additional environments to activate
     * @return Started child context
     */
    public ApplicationContext build(Environment environment,
                                    Map<String, Object> providerProperties,
                                    String... extraEnvironments) {
        Map<String, Object> properties = new LinkedHashMap<>();
        for (String property : BASE_DISABLED_PROPERTIES) {
            properties.put(property, false);
        }
        environment.getProperty("micronaut.application.name", String.class)
            .ifPresent(name -> properties.put("micronaut.application.name", name));
        properties.putAll(providerProperties);

        ApplicationContextBuilder builder = ApplicationContext.builder()
            .beanConfigurationsPredicate(beanConfiguration -> INCLUDED_PACKAGES.stream().anyMatch(beanConfiguration.getPackage().getName()::startsWith))
            .eventsEnabled(false)
            .eagerBeansEnabled(false)
            .deducePackage(false)
            .bootstrapEnvironment(false)
            .enableDefaultPropertySources(false)
            .deduceCloudEnvironment(false)
            .propertySources(PropertySource.of("config", properties, PropertySource.PropertyConvention.JAVA_PROPERTIES, PropertySource.Origin.of("config")))
            .environments(environment.getActiveNames().toArray(String[]::new))
            .environments(extraEnvironments);

        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        if (contextClassLoader != null) {
            builder.classLoader(contextClassLoader);
        }
        return builder.start();
    }
}
