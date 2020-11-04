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

import edu.umd.cs.findbugs.annotations.NonNull;
import io.micronaut.context.annotation.DefaultImplementation;
import io.micronaut.context.env.Environment;

import java.util.List;
import java.util.Optional;

/**
 * Definition of a service that provides a list of {@link ParameterQuery} objects
 * to be processed by the configuration client. Users who wish to search their
 * own custom paths need to implement this interface and replace the default
 * implementation:
 *
 * <pre>
 * {@code
 * @Replaces(AWSParameterQueryProvider.class)
 * public class CustomParameterQueryProvider implements AWSParameterQueryProvider {
 *
 * ...
 *
 * }}
 * </pre>
 *
 * @author ttzn
 * @since 2.1.2
 */
@DefaultImplementation(DefaultParameterQueryProvider.class)
public interface AWSParameterQueryProvider {
    /**
     * @param environment the current application environment
     * @param serviceId the service ID or application name, if applicable
     * @param configuration the parameter store configuration
     * @return a list of {@link ParameterQuery} that will be used to configure calls
     * to the Parameter Store
     */
    @NonNull List<ParameterQuery> getParameterQueries(@NonNull Environment environment,
                                                      @NonNull Optional<String> serviceId,
                                                      @NonNull AWSParameterStoreConfiguration configuration);
}
