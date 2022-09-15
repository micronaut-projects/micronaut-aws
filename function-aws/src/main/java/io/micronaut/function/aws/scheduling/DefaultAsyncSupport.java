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
package io.micronaut.function.aws.scheduling;

import io.micronaut.context.event.ApplicationEventListener;
import io.micronaut.context.event.BeanCreatedEvent;
import io.micronaut.context.event.BeanCreatedEventListener;
import io.micronaut.function.aws.event.AfterExecutionEvent;
import jakarta.inject.Singleton;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * The default implementation of {@link AsyncSupport} that intercepts creation of {@link ExecutorService}
 * and wraps the {@link ScheduledExecutorService} beans into {@link RegisteringScheduledExecutorService}.
 * It also listens to {@link AfterExecutionEvent} to wait until all tasks are finished.
 *
 * @author Vladimir Orany
 * @since 3.9.1
 */
@Singleton
public class DefaultAsyncSupport implements AsyncSupport, BeanCreatedEventListener<ExecutorService>, ApplicationEventListener<AfterExecutionEvent> {

    private final List<RegisteringScheduledExecutorService> executors = new ArrayList<>();

    private final AsyncSupportConfiguration configuration;

    public DefaultAsyncSupport(AsyncSupportConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public ExecutorService onCreated(BeanCreatedEvent<ExecutorService> event) {
        if (event.getBean() instanceof RegisteringScheduledExecutorService) {
            return event.getBean();
        }

        if (event.getBean() instanceof ScheduledExecutorService) {
            RegisteringScheduledExecutorService registeringExecutorService = new RegisteringScheduledExecutorService((ScheduledExecutorService) event.getBean());
            executors.add(registeringExecutorService);
            return registeringExecutorService;
        }

        return event.getBean();
    }

    @Override
    public void awaitAsyncFinished(long time, TimeUnit unit) {
        long finish = System.currentTimeMillis() + TimeUnit.MILLISECONDS.convert(time, unit);
        for (RegisteringScheduledExecutorService executor : executors) {
            executor.awaitAllTaskFinished(finish);
        }
    }

    @Override
    public void onApplicationEvent(AfterExecutionEvent event) {
        awaitAsyncFinished(configuration.getAwaitTermination().toMillis(), TimeUnit.MILLISECONDS);
    }
}
