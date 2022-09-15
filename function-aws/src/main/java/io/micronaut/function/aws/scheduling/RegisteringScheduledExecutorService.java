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

import io.micronaut.scheduling.instrument.InstrumentedScheduledExecutorService;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * This class a wrapper around an existing {@link java.util.concurrent.ScheduledExecutorService}
 * that registers returned futures and waits until all the tasks are finished once
 * {@link io.micronaut.function.aws.event.AfterExecutionEvent} is published.
 *
 * @author Vladimir Orany
 * @since 3.9.1
 */
public class RegisteringScheduledExecutorService implements InstrumentedScheduledExecutorService {

    private final ConcurrentLinkedDeque<Future<?>> futures = new ConcurrentLinkedDeque<>();
    private final ScheduledExecutorService target;

    public RegisteringScheduledExecutorService(ScheduledExecutorService target) {
        this.target = target;

        this.target.scheduleAtFixedRate(this::cleanUp, 1, 1, TimeUnit.SECONDS);
    }

    @Override
    public ScheduledExecutorService getTarget() {
        return target;
    }

    public <T> Future<T> submit(Callable<T> task) {
        Future<T> future = target.submit(task);
        futures.add(future);
        return future;
    }

    @Override
    public <T> Future<T> submit(Runnable task, T result) {
        Future<T> future = target.submit(task, result);
        futures.add(future);
        return future;
    }

    @Override
    public Future<?> submit(Runnable task) {
        Future<?> future = target.submit(task);
        futures.add(future);
        return future;
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
        List<Future<T>> response = target.invokeAll(tasks);
        futures.addAll(response);
        return response;
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException {
        List<Future<T>> response = target.invokeAll(tasks, timeout, unit);
        futures.addAll(response);
        return response;
    }

    void awaitAllTaskFinished(long finish) {
        while (!futures.isEmpty() && System.currentTimeMillis() <= finish) {
            cleanUp();
        }

        if (!futures.isEmpty()) {
            throw new IllegalStateException("There are still " + futures.size() + " pending tasks " + futures);
        }
    }

    private void cleanUp() {
        if (!futures.isEmpty()) {
            futures.removeAll(futures.stream().filter(f -> f.isDone() || f.isCancelled()).collect(Collectors.toList()));
        }
    }


}
