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
package io.micronaut.aws.sdk.v2.dev;

import io.micronaut.aws.ua.UserAgentProvider;
import io.micronaut.context.BeanRegistration;
import io.micronaut.context.env.DevelopmentActive;
import io.micronaut.context.reload.BeanRetentionPolicy;
import io.micronaut.core.annotation.Internal;
import jakarta.inject.Singleton;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.awscore.AwsServiceClientConfiguration;
import software.amazon.awssdk.core.SdkClient;
import software.amazon.awssdk.core.SdkServiceClientConfiguration;
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
import software.amazon.awssdk.core.client.config.SdkAdvancedClientOption;
import software.amazon.awssdk.core.interceptor.ExecutionInterceptor;
import software.amazon.awssdk.http.SdkHttpClient;
import software.amazon.awssdk.http.async.SdkAsyncHttpClient;
import software.amazon.awssdk.identity.spi.IdentityProvider;
import software.amazon.awssdk.regions.providers.AwsRegionProvider;
import software.amazon.awssdk.utils.ScheduledExecutorUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Refuses, in development mode only, to retain an AWS SDK client, or a bean that the clients are built from, that
 * holds a class of the application. The clients and the SDK HTTP clients of this module are
 * {@link io.micronaut.context.annotation.Retain retained} across a restart, and the context refuses those built from
 * beans of the application, but it cannot see what an application listener gives a client builder: execution
 * interceptors, metric publishers, a retry strategy or policy, a scheduled executor, a signer, a credentials, endpoint
 * or auth scheme provider. A retained client would keep running those after
 * the restart replaced their classes, and keep the retired generation reachable. It refuses:
 *
 * <ul>
 *     <li>an {@link SdkClient} whose class, credentials, endpoint or auth scheme provider, or execution interceptors,
 *     metric publishers, retry strategy or policy, scheduled executor, profile file supplier or signers of its
 *     override configuration, are classes of the application or, when the SDK built them, hold one, as a retry
 *     strategy built with a predicate of the application, or an executor with its thread factory, does; or that does
 *     not tell what it holds;</li>
 *     <li>an {@link SdkHttpClient}, {@link SdkAsyncHttpClient}, credentials or region provider, {@link UserAgentProvider}
 *     or {@link ExecutionInterceptor} bean whose class is a class of the application.</li>
 * </ul>
 *
 * <p>A client refused here is made again by the next context, as is any retained bean holding a refused one. The
 * policy abstains on every other bean, and exists only in development mode, so nothing of it is on the path of a
 * request.</p>
 *
 * @author graemerocher
 * @since 5.2.0
 */
@Internal
@Singleton
@DevelopmentActive
final class DevelopmentAwsRetentionPolicy implements BeanRetentionPolicy {

    private static final Logger LOG = LoggerFactory.getLogger(DevelopmentAwsRetentionPolicy.class);
    private static final String SDK_PACKAGE = "software.amazon.awssdk.";
    private static final int MAX_DEPTH = 8;

    @Override
    public Decision decide(BeanRegistration<?> registration) {
        Object bean = registration.bean();
        String applicationClass;
        if (bean instanceof SdkClient client) {
            applicationClass = applicationClassOf(client);
        } else if (bean instanceof SdkHttpClient
            || bean instanceof SdkAsyncHttpClient
            || bean instanceof IdentityProvider<?>
            || bean instanceof AwsRegionProvider
            || bean instanceof UserAgentProvider
            || bean instanceof ExecutionInterceptor) {
            applicationClass = isApplicationClass(bean.getClass()) ? bean.getClass().getName() : null;
        } else {
            return Decision.ABSTAIN;
        }
        if (applicationClass != null) {
            LOG.debug("The AWS bean [{}] is not retained across the restart: it holds the class [{}] of the application", bean, applicationClass);
            return Decision.REFUSE;
        }
        return Decision.ABSTAIN;
    }

    /**
     * The first class of the application a client holds, among those it tells: its own, its credentials, endpoint and
     * auth scheme providers, and what its override configuration holds, or what any of those built by the SDK hold.
     */
    private static @Nullable String applicationClassOf(SdkClient client) {
        if (isApplicationClass(client.getClass())) {
            return client.getClass().getName();
        }
        List<@Nullable Object> held = new ArrayList<>();
        try {
            SdkServiceClientConfiguration configuration = client.serviceClientConfiguration();
            if (configuration instanceof AwsServiceClientConfiguration aws) {
                held.add(aws.credentialsProvider());
            }
            held.add(configuration.endpointProvider().orElse(null));
            held.add(authSchemeProviderOf(configuration));
            held.addAll(overrides(configuration.overrideConfiguration()));
            Set<Object> visited = Collections.newSetFromMap(new IdentityHashMap<>());
            for (Object object : held) {
                String applicationClass = applicationClassIn(object, 0, visited);
                if (applicationClass != null) {
                    return applicationClass;
                }
            }
            return null;
        } catch (RuntimeException e) {
            // a client, of the application, that does not tell what it holds
            return client.getClass().getName();
        }
    }

    /**
     * The first class of the application an object is, names or holds. What the SDK builds from what it is given, such
     * as a retry strategy built with predicates of the application, or the conditions of a retry policy that a client
     * composes with its own, is of a class of the SDK that holds them; so are the lambdas the SDK makes, which capture
     * them. The fields of those are read, to a bounded depth, as are the elements of collections, maps and arrays and
     * the thread factory and rejection handler of an executor. Other objects of the JDK or the libraries are not.
     */
    private static @Nullable String applicationClassIn(@Nullable Object object, int depth, Set<Object> visited) {
        if (object == null || !visited.add(object)) {
            return null;
        }
        if (object instanceof Class<?> type) {
            return isApplicationClass(type) ? type.getName() : null;
        }
        Class<?> type = object.getClass();
        if (isApplicationClass(type)) {
            return type.getName();
        }
        if (depth >= MAX_DEPTH) {
            return null;
        }
        List<@Nullable Object> held = new ArrayList<>();
        if (object instanceof Collection<?> collection) {
            held.addAll(Arrays.asList(collection.toArray()));
        } else if (object instanceof Map<?, ?> map) {
            held.addAll(Arrays.asList(map.keySet().toArray()));
            held.addAll(Arrays.asList(map.values().toArray()));
        } else if (object instanceof Object[] array) {
            held.addAll(Arrays.asList(array));
        } else if (object instanceof ThreadPoolExecutor executor) {
            held.add(executor.getThreadFactory());
            held.add(executor.getRejectedExecutionHandler());
        } else if (type.getName().startsWith(SDK_PACKAGE) || type.isHidden()) {
            for (Class<?> declaring = type; declaring != null && declaring != Object.class; declaring = declaring.getSuperclass()) {
                for (Field field : declaring.getDeclaredFields()) {
                    if (!Modifier.isStatic(field.getModifiers()) && !field.getType().isPrimitive() && field.trySetAccessible()) {
                        try {
                            held.add(field.get(object));
                        } catch (IllegalAccessException e) {
                            // not readable, as trySetAccessible said otherwise
                        }
                    }
                }
            }
        }
        for (Object value : held) {
            String applicationClass = applicationClassIn(value, depth + 1, visited);
            if (applicationClass != null) {
                return applicationClass;
            }
        }
        return null;
    }

    /**
     * What the override configuration of a client holds that an application may implement: its execution
     * interceptors, metric publishers, retry strategy, the configurator of its retry strategy, its retry policy, its
     * scheduled executor, the supplier of its default profile file and its signers.
     */
    @SuppressWarnings("deprecation")
    private static List<@Nullable Object> overrides(@Nullable ClientOverrideConfiguration configuration) {
        if (configuration == null) {
            return List.of();
        }
        List<@Nullable Object> overrides = new ArrayList<>(configuration.executionInterceptors());
        overrides.addAll(configuration.metricPublishers());
        configuration.retryStrategy().ifPresent(overrides::add);
        configuration.retryStrategyConfigurator().ifPresent(overrides::add);
        configuration.retryPolicy().ifPresent(overrides::add);
        // the client wraps the executor it was given, so that closing the client does not shut it down
        configuration.scheduledExecutorService()
            .map(ScheduledExecutorUtils::unwrapUnmanagedScheduledExecutor)
            .ifPresent(overrides::add);
        configuration.defaultProfileFileSupplier().ifPresent(overrides::add);
        configuration.advancedOption(SdkAdvancedClientOption.SIGNER).ifPresent(overrides::add);
        configuration.advancedOption(SdkAdvancedClientOption.TOKEN_SIGNER).ifPresent(overrides::add);
        return overrides;
    }

    /**
     * The auth scheme provider of a client, which each service's configuration declares for its own provider type
     * rather than {@link SdkServiceClientConfiguration}.
     */
    private static @Nullable Object authSchemeProviderOf(SdkServiceClientConfiguration configuration) {
        try {
            return configuration.getClass().getMethod("authSchemeProvider").invoke(configuration);
        } catch (NoSuchMethodException e) {
            return null;
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Cannot read the auth scheme provider of " + configuration, e);
        }
    }

    /**
     * Whether a class was loaded by neither the classloader of the SDK nor one of its parents: the application's
     * classes are loaded by a child of the loader of the libraries, which the restart replaces.
     */
    private static boolean isApplicationClass(Class<?> type) {
        ClassLoader loader = type.getClassLoader();
        if (loader == null) {
            return false;
        }
        for (ClassLoader sdk = SdkClient.class.getClassLoader(); sdk != null; sdk = sdk.getParent()) {
            if (sdk == loader) {
                return false;
            }
        }
        return true;
    }
}
