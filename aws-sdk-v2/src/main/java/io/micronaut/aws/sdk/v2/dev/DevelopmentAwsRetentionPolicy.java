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
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanRegistration;
import io.micronaut.context.env.DevelopmentActive;
import io.micronaut.context.reload.BeanRetentionPolicy;
import io.micronaut.core.annotation.Internal;
import jakarta.inject.Inject;
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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.function.Predicate;

/**
 * Refuses, in development mode only, to retain an AWS SDK client, or a bean that the clients are built from, that
 * holds a class of the application. The clients and the SDK HTTP clients of this module are
 * {@link io.micronaut.context.annotation.Retain retained} across a restart, and the context refuses those built from
 * beans of the application, but it cannot see what an application listener gives a client builder: execution
 * interceptors, metric publishers, a retry strategy or policy, a scheduled executor, a signer, a credentials, endpoint
 * or auth scheme provider. A retained client would keep running those after the restart replaced their classes, and
 * keep the retired generation reachable. It refuses:
 *
 * <ul>
 *     <li>an {@link SdkClient}, {@link SdkHttpClient} or {@link SdkAsyncHttpClient} made from a bean that a
 *     {@code BeanCreatedEventListener} or {@code BeanInitializedEventListener} of the application applies to, such as
 *     its builder, whatever the listener gave it: this is decided from the definitions of the context and its
 *     dependency graph, not from what the SDK built;</li>
 *     <li>an {@link SdkClient} whose class, credentials or endpoint provider, auth schemes, or execution interceptors,
 *     metric publishers, retry strategy or its configurator, retry policy, scheduled executor, with the thread factory
 *     and rejection handler of a thread pool, profile file supplier or signers of its override configuration, as the
 *     client tells them, are classes of the application; or that does not tell what it holds;</li>
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

    private final BeanContext beanContext;
    private final Predicate<Class<?>> isApplicationClass;
    private @Nullable ApplicationListeners listeners;

    /**
     * @param beanContext The context, whose listeners and dependency graph tell what the clients were made from
     */
    @Inject
    DevelopmentAwsRetentionPolicy(BeanContext beanContext) {
        this(beanContext, DevelopmentAwsRetentionPolicy::isApplicationClass);
    }

    /**
     * @param beanContext The context
     * @param isApplicationClass Whether a class is of the application, which the restart replaces
     */
    DevelopmentAwsRetentionPolicy(BeanContext beanContext, Predicate<Class<?>> isApplicationClass) {
        this.beanContext = beanContext;
        this.isApplicationClass = isApplicationClass;
    }

    @Override
    public Decision decide(BeanRegistration<?> registration) {
        Object bean = registration.bean();
        String reason;
        if (bean instanceof SdkClient client) {
            reason = applicationClassOf(client);
        } else if (bean instanceof SdkHttpClient
            || bean instanceof SdkAsyncHttpClient
            || bean instanceof IdentityProvider<?>
            || bean instanceof AwsRegionProvider
            || bean instanceof UserAgentProvider
            || bean instanceof ExecutionInterceptor) {
            reason = isApplicationClass.test(bean.getClass()) ? "it holds the class [" + bean.getClass().getName() + "] of the application" : null;
        } else {
            return Decision.ABSTAIN;
        }
        if (reason == null && (bean instanceof SdkClient || bean instanceof SdkHttpClient || bean instanceof SdkAsyncHttpClient)) {
            reason = listeners().customizing(registration.getBeanDefinition());
        }
        if (reason != null) {
            LOG.debug("The AWS bean [{}] is not retained across the restart: {}", bean, reason);
            return Decision.REFUSE;
        }
        return Decision.ABSTAIN;
    }

    private synchronized ApplicationListeners listeners() {
        ApplicationListeners found = listeners;
        if (found == null) {
            // read as the context stops, which is when the policy is asked, so that a listener added meanwhile counts
            found = new ApplicationListeners(beanContext, isApplicationClass);
            listeners = found;
        }
        return found;
    }

    /**
     * Why a client is refused, among what it tells it holds: its own class, its credentials and endpoint providers, its
     * auth schemes, and what its override configuration holds. What the SDK built from what a builder was given, such
     * as a retry strategy built with a predicate of the application, is not read: only a listener of the application
     * gives a builder such a thing, and a client made from a builder a listener of the application applies to is
     * refused whatever it holds.
     */
    private @Nullable String applicationClassOf(SdkClient client) {
        if (isApplicationClass.test(client.getClass())) {
            return "it is of the class [" + client.getClass().getName() + "] of the application";
        }
        List<@Nullable Object> held = new ArrayList<>();
        try {
            SdkServiceClientConfiguration configuration = client.serviceClientConfiguration();
            if (configuration instanceof AwsServiceClientConfiguration aws) {
                held.add(aws.credentialsProvider());
            }
            held.add(configuration.endpointProvider().orElse(null));
            held.addAll(configuration.authSchemes().values());
            held.addAll(overrides(configuration.overrideConfiguration()));
        } catch (RuntimeException e) {
            // a client, of the application, that does not tell what it holds
            return "it does not tell what it holds";
        }
        for (Object object : held) {
            if (object != null && isApplicationClass.test(object.getClass())) {
                return "it holds the class [" + object.getClass().getName() + "] of the application";
            }
        }
        return null;
    }

    /**
     * What the override configuration of a client holds that an application may implement: its execution
     * interceptors, metric publishers, retry strategy, the configurator of its retry strategy, its retry policy, its
     * scheduled executor, with the thread factory and rejection handler of a thread pool, the supplier of its default
     * profile file and its signers.
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
            .ifPresent(executor -> {
                overrides.add(executor);
                if (executor instanceof ThreadPoolExecutor pool) {
                    overrides.add(pool.getThreadFactory());
                    overrides.add(pool.getRejectedExecutionHandler());
                }
            });
        configuration.defaultProfileFileSupplier().ifPresent(overrides::add);
        configuration.advancedOption(SdkAdvancedClientOption.SIGNER).ifPresent(overrides::add);
        configuration.advancedOption(SdkAdvancedClientOption.TOKEN_SIGNER).ifPresent(overrides::add);
        return overrides;
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
