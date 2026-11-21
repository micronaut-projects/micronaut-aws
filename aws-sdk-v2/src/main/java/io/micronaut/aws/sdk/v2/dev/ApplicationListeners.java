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

import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanDependencyGraph;
import io.micronaut.context.ConfigurableBeanContext;
import io.micronaut.context.event.BeanCreatedEventListener;
import io.micronaut.context.event.BeanInitializedEventListener;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.type.Argument;
import io.micronaut.inject.BeanDefinition;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

/**
 * The listeners of the application that customize beans as the context creates or initializes them. What such a
 * listener gives a bean, an execution interceptor or a retry strategy added to a client builder for instance, is not
 * injected, so the context does not see it as it decides what a restart retains: a retained client would keep running
 * it after the restart replaced its class, and keep the retired generation reachable. A bean made from a bean such a
 * listener applies to is therefore not retained, whatever the listener did, rather than the objects the SDK built
 * from it being read.
 *
 * <p>Read once, from the definitions of the context, as the context stops for a restart.</p>
 *
 * @author graemerocher
 * @since 5.2.0
 */
@Internal
final class ApplicationListeners {

    private static final List<Class<?>> LISTENER_TYPES = List.of(BeanCreatedEventListener.class, BeanInitializedEventListener.class);

    private final BeanContext beanContext;
    private final List<Listener> listeners;

    /**
     * @param beanContext The context
     * @param isApplicationClass Whether a class is of the application, which the restart replaces
     */
    ApplicationListeners(BeanContext beanContext, Predicate<Class<?>> isApplicationClass) {
        this.beanContext = beanContext;
        List<Listener> found = new ArrayList<>();
        for (Class<?> listenerType : LISTENER_TYPES) {
            for (BeanDefinition<?> definition : beanContext.getBeanDefinitions(listenerType)) {
                Class<?> type = definition.getBeanType();
                // a listener a factory of the application produces, a lambda for instance, is typed by the interface:
                // its definition, and the factory that declares it, are of the application
                if (isApplicationClass.test(type)
                    || isApplicationClass.test(definition.getClass())
                    || definition.getDeclaringType().filter(isApplicationClass).isPresent()) {
                    List<Argument<?>> arguments = definition.getTypeArguments(listenerType);
                    found.add(new Listener(type, arguments.isEmpty() ? Object.class : arguments.get(0).getType()));
                }
            }
        }
        this.listeners = List.copyOf(found);
    }

    /**
     * The first listener of the application that applies to the bean of a definition, or to a bean it was made from:
     * one it received, or one of those received, as the dependency graph of the context records them, or else the
     * arguments it was created with.
     *
     * @param definition The definition
     * @return What the listener applies to, for the log, or null when none does
     */
    @Nullable String customizing(BeanDefinition<?> definition) {
        if (listeners.isEmpty()) {
            return null;
        }
        for (Class<?> type : madeFrom(definition)) {
            for (Listener listener : listeners) {
                if (listener.listened().isAssignableFrom(type)) {
                    return "the listener " + listener.type().getName() + " of the application customizes the " + type.getName() + " it is made from";
                }
            }
        }
        return null;
    }

    private Set<Class<?>> madeFrom(BeanDefinition<?> definition) {
        Set<Class<?>> types = new LinkedHashSet<>();
        types.add(definition.getBeanType());
        BeanDependencyGraph graph = beanContext instanceof ConfigurableBeanContext configurable ? configurable.findDependencyGraph().orElse(null) : null;
        if (graph != null) {
            for (BeanDefinition<?> dependency : graph.transitiveDependenciesOf(definition)) {
                types.add(dependency.getBeanType());
            }
        } else {
            for (Argument<?> argument : definition.getConstructor().getArguments()) {
                types.add(argument.getType());
            }
        }
        return types;
    }

    /**
     * A listener of the application, and the type of the beans it listens to.
     *
     * @param type The class of the listener
     * @param listened The type it listens to
     */
    private record Listener(Class<?> type, Class<?> listened) {
    }
}
