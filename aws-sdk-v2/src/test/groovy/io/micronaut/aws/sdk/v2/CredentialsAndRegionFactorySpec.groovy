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
package io.micronaut.aws.sdk.v2

import io.micronaut.context.ApplicationContext
import io.micronaut.context.annotation.BootstrapContextCompatible
import io.micronaut.context.annotation.Requires
import io.micronaut.context.env.BootstrapPropertySourceLocator
import io.micronaut.context.env.Environment
import io.micronaut.context.env.PropertySource
import jakarta.annotation.PreDestroy
import jakarta.inject.Singleton
import software.amazon.awssdk.auth.credentials.AwsCredentialsProviderChain
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider
import spock.lang.Specification

import java.util.concurrent.CopyOnWriteArrayList

/**
 * Closing a context closes its {@link AwsCredentialsProviderChain}, and with it the chain's default credentials
 * provider. That provider must belong to the context: the one {@link DefaultCredentialsProvider#create()} returns is
 * shared by the whole JVM, so closing it breaks every other context that still uses it.
 */
class CredentialsAndRegionFactorySpec extends Specification {

    static final String SPEC_NAME = "CredentialsAndRegionFactorySpec"

    def setup() {
        BootstrapCredentialsLocator.instances.clear()
    }

    void "each context builds its own default credentials provider instead of using the JVM-wide one"() {
        given:
        ApplicationContext first = ApplicationContext.run()
        ApplicationContext second = ApplicationContext.run()

        when:
        DefaultCredentialsProvider firstDefault = defaultCredentialsProvider(first.getBean(AwsCredentialsProviderChain))
        DefaultCredentialsProvider secondDefault = defaultCredentialsProvider(second.getBean(AwsCredentialsProviderChain))

        then: "closing one context closes neither the JVM-wide provider nor the other context's provider"
        firstDefault != null
        secondDefault != null
        !firstDefault.is(DefaultCredentialsProvider.create())
        !secondDefault.is(DefaultCredentialsProvider.create())
        !firstDefault.is(secondDefault)

        cleanup:
        first.close()
        second.close()
    }

    void "a refresh that closes the bootstrap context it replaces leaves the application context's credentials provider open"() {
        given:
        ApplicationContext context = ApplicationContext.builder()
                .properties(['spec.name': SPEC_NAME])
                .bootstrapEnvironment(true)
                .start()
        DefaultCredentialsProvider applicationDefault = defaultCredentialsProvider(context.getBean(AwsCredentialsProviderChain))

        when:
        context.environment.refresh()

        then: "since Micronaut core 5.2.16, the refresh closes the bootstrap context of the start, with its credentials provider"
        BootstrapCredentialsLocator.instances.size() == 2
        BootstrapCredentialsLocator.instances[0].destroyed
        !BootstrapCredentialsLocator.instances[1].destroyed

        and: "no bootstrap context shares its default credentials provider with the application context"
        applicationDefault != null
        BootstrapCredentialsLocator.instances.every {
            DefaultCredentialsProvider bootstrapDefault = defaultCredentialsProvider(it.credentialsProvider)
            bootstrapDefault != null && !bootstrapDefault.is(applicationDefault)
        }

        cleanup:
        context.close()
    }

    private static DefaultCredentialsProvider defaultCredentialsProvider(AwsCredentialsProviderChain chain) {
        chain.@credentialsProviders.find { it instanceof DefaultCredentialsProvider } as DefaultCredentialsProvider
    }

    /**
     * Stands in for a distributed configuration client: a bootstrap context bean that uses the AWS credentials.
     */
    @BootstrapContextCompatible
    @Singleton
    @Requires(property = "spec.name", value = SPEC_NAME)
    static class BootstrapCredentialsLocator implements BootstrapPropertySourceLocator {
        static final List<BootstrapCredentialsLocator> instances = new CopyOnWriteArrayList<>()

        final AwsCredentialsProviderChain credentialsProvider
        volatile boolean destroyed

        BootstrapCredentialsLocator(AwsCredentialsProviderChain credentialsProvider) {
            this.credentialsProvider = credentialsProvider
            instances.add(this)
        }

        @PreDestroy
        void destroy() {
            destroyed = true
        }

        @Override
        Iterable<PropertySource> findPropertySources(Environment environment) {
            return List.of()
        }
    }
}
