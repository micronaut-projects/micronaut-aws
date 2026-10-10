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
import software.amazon.awssdk.core.interceptor.ExecutionInterceptor;
import software.amazon.awssdk.http.SdkHttpClient;
import software.amazon.awssdk.http.async.SdkAsyncHttpClient;
import software.amazon.awssdk.identity.spi.IdentityProvider;
import software.amazon.awssdk.regions.providers.AwsRegionProvider;

import java.util.ArrayList;
import java.util.List;

/**
 * Refuses, in development mode only, to retain an AWS SDK client, or a bean that the clients are built from, that
 * holds a class of the application. The clients and the SDK HTTP clients of this module are
 * {@link io.micronaut.context.annotation.Retain retained} across a restart, and the context refuses those built from
 * beans of the application, but it cannot see what an application listener gives a client builder: execution
 * interceptors, a credentials, endpoint or auth scheme provider. A retained client would keep running those after
 * the restart replaced their classes, and keep the retired generation reachable. It refuses:
 *
 * <ul>
 *     <li>an {@link SdkClient} whose class, credentials, endpoint or auth scheme provider, or execution interceptors,
 *     are classes of the application, or that does not tell what it holds;</li>
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
     * auth scheme providers, and its execution interceptors.
     */
    private static @Nullable String applicationClassOf(SdkClient client) {
        List<@Nullable Object> held = new ArrayList<>();
        held.add(client);
        try {
            SdkServiceClientConfiguration configuration = client.serviceClientConfiguration();
            if (configuration instanceof AwsServiceClientConfiguration aws) {
                held.add(aws.credentialsProvider());
            }
            held.add(configuration.endpointProvider().orElse(null));
            held.add(authSchemeProviderOf(configuration));
            ClientOverrideConfiguration overrides = configuration.overrideConfiguration();
            if (overrides != null) {
                held.addAll(overrides.executionInterceptors());
            }
        } catch (RuntimeException e) {
            // a client, of the application, that does not tell what it holds
            return client.getClass().getName();
        }
        for (Object object : held) {
            if (object != null && isApplicationClass(object.getClass())) {
                return object.getClass().getName();
            }
        }
        return null;
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
