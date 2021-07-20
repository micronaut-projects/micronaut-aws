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

import io.micronaut.core.annotation.Nullable;
import io.micronaut.context.annotation.BootstrapContextCompatible;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.env.Environment;
import io.micronaut.context.env.PropertySource;
import io.micronaut.context.exceptions.ConfigurationException;
import io.micronaut.discovery.aws.servicediscovery.AwsServiceDiscoveryClientConfiguration;
import io.micronaut.discovery.aws.servicediscovery.AwsServiceDiscoveryConfiguration;
import io.micronaut.discovery.client.ClientUtil;
import io.micronaut.discovery.config.ConfigurationClient;
import io.micronaut.runtime.ApplicationConfiguration;
import io.micronaut.scheduling.TaskExecutors;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.services.ssm.SsmAsyncClient;
import software.amazon.awssdk.services.ssm.model.*;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * A {@link ConfigurationClient} implementation for AWS ParameterStore.
 *
 * @author Rvanderwerf
 * @author graemerocher
 * @since 1.0
 */
@Singleton
@Requires(env = Environment.AMAZON_EC2)
@Requires(beans = {AWSParameterStoreConfiguration.class, SsmAsyncClient.class})
@BootstrapContextCompatible
public class AWSParameterStoreConfigClient implements ConfigurationClient {

    private static final Logger LOG = LoggerFactory.getLogger(AWSParameterStoreConfigClient.class);
    private final AWSParameterStoreConfiguration awsParameterStoreConfiguration;
    private final String serviceId;
    private SsmAsyncClient client;
    private ExecutorService executorService;
    private AWSParameterQueryProvider queryProvider;

    /**
     * Initialize @Singleton.
     *
     * @param asyncClient                         async client
     * @param awsParameterStoreConfiguration      configuration for the parameter store
     * @param applicationConfiguration            the application configuration
     * @param queryProvider                       the query provider that will help find configuration values
     * @param serviceDiscoveryConfiguration       configuration for route53 service discovery, if you are using this (not required)
     */
    AWSParameterStoreConfigClient(
            SsmAsyncClient asyncClient,
            AWSParameterStoreConfiguration awsParameterStoreConfiguration,
            ApplicationConfiguration applicationConfiguration,
            AWSParameterQueryProvider queryProvider,
            @Nullable AwsServiceDiscoveryConfiguration serviceDiscoveryConfiguration) {
        this.awsParameterStoreConfiguration = awsParameterStoreConfiguration;
        this.client = asyncClient;
        this.serviceId = serviceDiscoveryConfiguration != null ? serviceDiscoveryConfiguration.getAwsServiceId() : applicationConfiguration.getName().orElse(null);
        this.queryProvider = queryProvider;
    }


    /**
     * Get your PropertySources from AWS Parameter Store.
     * Property sources are expected to be set up in this way:
     * \ config \ micronaut \ environment name \ app name \
     * If you want to change the base \configuration\micronaut set the property aws.system-manager.parameterStore.rootHierarchyPath
     *
     * @param environment The environment
     * @return property source objects by environment.
     */
    @Override
    public Publisher<PropertySource> getPropertySources(Environment environment) {
        if (!awsParameterStoreConfiguration.isEnabled()) {
            return Flux.empty();
        }

        List<ParameterQuery> queries = queryProvider.getParameterQueries(environment, serviceId, awsParameterStoreConfiguration);
        Flux<ParameterQueryResult> queryResults =
                Flux.concat(
                        Flux.fromIterable(queries)
                                .map(this::getParameters));
        Flux<PropertySource> propertySourceFlowable =
                queryResults
                        .flatMap(this::buildLocalSource)
                        .reduce(new HashMap<>(), AWSParameterStoreConfigClient::mergeLocalSources)
                        .flatMapMany(AWSParameterStoreConfigClient::toPropertySourcePublisher);

        return propertySourceFlowable.onErrorResume(AWSParameterStoreConfigClient::onPropertySourceError);

    }

    /**
     * Description.
     *
     * @return the description
     */
    @Override
    public String getDescription() {
        return "AWS Parameter Store";
    }

    private static Publisher<? extends PropertySource> onPropertySourceError(Throwable throwable) {
        if (throwable instanceof ConfigurationException) {
            return Flux.error(throwable);
        } else {
            return Flux.error(new ConfigurationException("Error reading distributed configuration from AWS Parameter Store: " + throwable.getMessage(), throwable));
        }
    }

    private static Mono<? extends GetParametersResponse> onGetParametersError(Throwable throwable) {
        if (throwable instanceof SdkClientException) {
            return Mono.error(throwable);
        } else {
            return Mono.error(new ConfigurationException("Error reading distributed configuration from AWS Parameter Store: " + throwable.getMessage(), throwable));
        }
    }

    private static Mono<? extends GetParametersByPathResponse> onGetParametersByPathResult(Throwable throwable) {
        if (throwable instanceof SdkClientException) {
            return Mono.error(throwable);
        } else {
            return Mono.error(new ConfigurationException("Error reading distributed configuration from AWS Parameter Store: " + throwable.getMessage(), throwable));
        }
    }

    private Publisher<ParameterQueryResult> getParameters(ParameterQuery query) {
        String path = query.getPath();
        return query.isName()
                ? Flux.from(getParameters(path)).map(r -> new ParameterQueryResult(query, r.parameters()))
                : Flux.from(getHierarchy(path, new ArrayList<>(), null)).map(r -> new ParameterQueryResult(query, r));
    }

    private Flux<LocalSource> buildLocalSource(ParameterQueryResult queryResult) {
        String key = queryResult.query.getPath();
        if (queryResult.parameters.isEmpty()) {
            LOG.trace("parameterBasePath={} no parameters found", key);
            return Flux.empty();
        }
        Map<String, Object> properties = convertParametersToMap(queryResult);
        String propertySourceName = queryResult.query.getPropertySourceName();
        if (LOG.isTraceEnabled()) {
            properties.keySet().iterator().forEachRemaining(param ->
                    LOG.trace("param found: parameterBasePath={} parameter={}", queryResult.query.getPath(), param));
        }
        LocalSource localSource = new LocalSource(queryResult.query.getPriority(), propertySourceName);
        localSource.putAll(properties);
        return Flux.just(localSource);
    }

    private Flux<List<Parameter>> getHierarchy(final String path, final List<Parameter> parameters, final String nextToken) {
        Flux<GetParametersByPathResponse> paramPage = Flux.from(getHierarchy(path, nextToken));

        return paramPage.flatMap(getParametersByPathResult -> {
            List<Parameter> params = getParametersByPathResult.parameters();

            if (getParametersByPathResult.nextToken() != null) {
                return Flux.merge(
                        Flux.just(parameters),
                        getHierarchy(path, params, getParametersByPathResult.nextToken())
                );
            } else {
                return Flux.merge(
                        Flux.just(parameters),
                        Flux.just(params)
                );
            }
        });
    }

    /**
     * Gets the Parameter hierarchy from AWS parameter store.
     * Please note this only returns something if the current node has children and will not return itself.
     *
     * @param path      path based on the parameter names PRIORITY_TOP.e. /config/application/.*
     * @param nextToken token to paginate in the resultset from AWS
     * @return Publisher for GetParametersByPathResult
     */
    private Publisher<GetParametersByPathResponse> getHierarchy(String path, String nextToken) {
        LOG.trace("Retrieving parameters by path {}, pagination requested: {}", path, nextToken != null);
        GetParametersByPathRequest getRequest = GetParametersByPathRequest.builder()
                .withDecryption(awsParameterStoreConfiguration.getUseSecureParameters())
                .path(path)
                .recursive(true)
                .nextToken(nextToken)
                .build();

        CompletableFuture<GetParametersByPathResponse> future = client.getParametersByPath(getRequest);

        Mono<GetParametersByPathResponse> invokeFlowable = Mono.fromFuture(future);
        if (executorService != null) {
            invokeFlowable = invokeFlowable.subscribeOn(Schedulers.fromExecutor(executorService));
        }

        return invokeFlowable.onErrorResume(AWSParameterStoreConfigClient::onGetParametersByPathResult);
    }

    /**
     * Gets the parameters from AWS.
     *
     * @param path this is the hierarchy path (via name field) from the property store
     * @return invokeFlowable - converted future from AWS SDK Async
     */
    private Publisher<GetParametersResponse> getParameters(String path) {

        GetParametersRequest getRequest = GetParametersRequest.builder()
                .withDecryption(awsParameterStoreConfiguration.getUseSecureParameters())
                .names(path)
                .build();

        CompletableFuture<GetParametersResponse> future = client.getParameters(getRequest);

        Mono<GetParametersResponse> invokeFlowable = Mono.fromFuture(future);
        if (executorService != null) {
            invokeFlowable = invokeFlowable.subscribeOn(Schedulers.fromExecutor(executorService));
        }

        return invokeFlowable.onErrorResume(AWSParameterStoreConfigClient::onGetParametersError);
    }

    /**
     * Execution service to make call to AWS.
     *
     * @param executorService ExecutorService
     */
    @Inject
    void setExecutionService(@Named(TaskExecutors.IO) @Nullable ExecutorService executorService) {
        if (executorService != null) {
            this.executorService = executorService;
        }
    }

    /**
     * Calculates property names to look for.
     *
     * @param prefix      The prefix
     * @param activeNames active environment names
     * @return A set of calculated property names
     */
    private Set<String> calcPropertySourceNames(String prefix, List<String> activeNames) {
        return ClientUtil.calcPropertySourceNames(prefix, activeNames, "_");
    }

    /**
     * Helper method for converting parameters from amazon format to a map.
     *
     * @param queryResult parameters with the base path
     * @return map of the results, converted
     */
    private static Map<String, Object> convertParametersToMap(ParameterQueryResult queryResult) {
        Map<String, Object> output = new HashMap<>();
        for (Parameter param : queryResult.parameters) {
            String key = param.name().substring(queryResult.query.getPath().length());
            if (key.length() > 1) {
                key = key.substring(1).replace("/", ".");
            }

            if (ParameterType.STRING_LIST.equals(param.type())) {
                String[] items = param.value().split(",");
                output.put(key, Arrays.asList(items));
            } else {
                output.put(key, param.value());
            }
        }
        LOG.trace("Converted " + output);
        return output;
    }

    /**
     * Stores LocalSource objects into an accumulator map. If two LocalSource have the same property source name
     * (which happens when using both GetParameters and GetParametersByPath-style queries), the properties are
     * merged, with the new LocalSource overwriting any existing keys.
     *
     * @param accumulator a map of local sources indexed by property source name
     * @param localSource the local source to store
     * @return the accumulator
     */
    private static Map<String, LocalSource> mergeLocalSources(Map<String, LocalSource> accumulator, LocalSource localSource) {
        LocalSource previous = accumulator.get(localSource.name);
        if (previous == null) {
            accumulator.put(localSource.name, localSource);
        } else {
            LOG.trace("merging into existing source {} from {}", localSource.name, localSource.priority);
            if (previous.priority != localSource.priority) {
                LOG.warn("local source {} redeclared with priority {} instead ofg {}, ignoring", localSource.name,
                        localSource.priority, previous.priority);
            }
            previous.putAll(localSource.values);
        }
        return accumulator;
    }

    private static Flux<PropertySource> toPropertySourcePublisher(Map<String, LocalSource> localSourceMap) {
        return Flux.fromIterable(localSourceMap.values())
                .map(localSource -> {
                    LOG.trace("source={} got priority={}", localSource.name, localSource.priority);
                    return PropertySource.of(AwsServiceDiscoveryClientConfiguration.SERVICE_ID
                            + '-' + localSource.name, localSource.values, localSource.priority);
                });
    }

    /**
     * @param client SsmAsyncClient client
     */
    protected void setClient(SsmAsyncClient client) {
        this.client = client;
    }

    /**
     * @return SsmAsyncClient client
     */
    protected SsmAsyncClient getClient() {
        return client;
    }

    /**
     * @return query provider
     */
    protected AWSParameterQueryProvider getQueryProvider() {
        return queryProvider;
    }

    /**
     * @param queryProvider query provider
     */
    protected void setQueryProvider(AWSParameterQueryProvider queryProvider) {
        this.queryProvider = queryProvider;
    }

    /**
     * Simple container class to hold the list of parameters and a base path which was used to collect them.
     */
    static class ParameterQueryResult {
        private final ParameterQuery query;
        private final List<Parameter> parameters;

        public ParameterQueryResult(ParameterQuery query, List<Parameter> parameters) {
            this.query = query;
            this.parameters = parameters;
        }
    }

    /**
     * A local property source.
     */
    private static class LocalSource {

        private final int priority;
        private final String name;
        private final Map<String, Object> values = new LinkedHashMap<>();

        LocalSource(int priority,
                    String name) {
            this.priority = priority;
            this.name = name;
        }

        void putAll(Map<String, Object> values) {
            this.values.putAll(values);
        }

    }
}
