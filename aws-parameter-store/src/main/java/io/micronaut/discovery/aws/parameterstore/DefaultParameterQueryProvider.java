/*
 * Copyright 2017-2020 original authors
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

import io.micronaut.context.annotation.BootstrapContextCompatible;
import io.micronaut.context.env.Environment;
import io.micronaut.context.env.EnvironmentPropertySource;
import io.micronaut.core.annotation.Internal;

import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 *
 *
 * @author ttzn
 * @since 2.3.0
 */
@Internal
@Singleton
@BootstrapContextCompatible
final class DefaultParameterQueryProvider implements AWSParameterQueryProvider {
    private static int basePriority = EnvironmentPropertySource.POSITION + 100;
    private static int envBasePriority = basePriority + 50;

    @Override
    public List<ParameterQuery> getParameterQueries(Environment environment, Optional<String> serviceId, AWSParameterStoreConfiguration configuration) {
        List<String> activeNames = configuration.isSearchActiveEnvironments() ?
                new ArrayList<>(environment.getActiveNames()) : Collections.emptyList();
        String path = configuration.getRootHierarchyPath();
        String normalizedPath = !path.endsWith("/") ? path + "/" : path;
        String commonConfigPath = normalizedPath + Environment.DEFAULT_NAME;
        final boolean hasApplicationSpecificConfig = serviceId.isPresent();
        String applicationSpecificPath = hasApplicationSpecificConfig ? normalizedPath + serviceId.get() : null;

        List<ParameterQuery> queries = new ArrayList<>();
        addNameAndPathQueries(queries, commonConfigPath, Environment.DEFAULT_NAME, basePriority + 1);
        if (hasApplicationSpecificConfig) {
            addNameAndPathQueries(queries, applicationSpecificPath, serviceId.get(), basePriority + 2);
        }

        for (int i = 0; i < activeNames.size(); i++) {
            String activeName = activeNames.get(i);
            String environmentSpecificPath = commonConfigPath + "_" + activeName;
            String propertySourceName = Environment.DEFAULT_NAME + "[" + activeName + "]";
            int priority = envBasePriority + i * 2;

            addNameAndPathQueries(queries, environmentSpecificPath, propertySourceName, priority);
            if (hasApplicationSpecificConfig) {
                String appEnvironmentSpecificPath = applicationSpecificPath + "_" + activeName;
                propertySourceName = serviceId.get() + "[" + activeName + "]";
                addNameAndPathQueries(queries, appEnvironmentSpecificPath, propertySourceName, priority + 1);
            }
        }

        return queries;
    }

    private void addNameAndPathQueries(List<ParameterQuery> queries, String value, String propertySourceName, int priority) {
        queries.add(new ParameterQuery(value, propertySourceName, priority, true));
        queries.add(new ParameterQuery(value, propertySourceName, priority, false));
    }
}
