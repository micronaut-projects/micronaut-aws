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
package io.micronaut.function.aws.event;

import io.micronaut.core.annotation.Nullable;

/**
 * This event is published after the execution of {@link io.micronaut.function.aws.MicronautRequestHandler#execute(Object)}
 * and {@link io.micronaut.function.aws.MicronautRequestStreamHandler#execute(java.io.InputStream, java.io.OutputStream)} methods to allow
 * performing actions before the Lambda function run is finished and the JVM is hibernated.
 * <p>
 * This event must be processed synchronously to guarantee it has been processed before the Lambda funciton is hibernated.
 *
 * @author Vladimir Orany
 * @since 3.9.0
 */
public final class AfterExecutionEvent {

    @Nullable
    private final Throwable exception;
    @Nullable
    private final Object output;

    private AfterExecutionEvent(@Nullable Object output, @Nullable Throwable exception) {
        this.output = output;
        this.exception = exception;
    }

    /**
     * Creates a new {@link AfterExecutionEvent} with an optional result of the execution.
     * @param output an optional result of the exectuion
     * @return a new {@link AfterExecutionEvent} with an optional result of the execution
     */
    public static AfterExecutionEvent success(@Nullable Object output) {
        return new AfterExecutionEvent(output, null);
    }

    /**
     * Creates a new {@link AfterExecutionEvent} with an exception been thrown.
     * @param exception the exception which has been thrown during the execution
     * @return a new {@link AfterExecutionEvent} with an exception been thrown.
     */
    public static AfterExecutionEvent failure(Throwable exception) {
        return new AfterExecutionEvent(null, exception);
    }

    /**
     * @return <code>true</code> if there were no exception thrown
     */
    public boolean isSuccess() {
        return exception == null;
    }

    /**
     * @return the optional result of the execution
     */
    @Nullable
    public Object getOutput() {
        return output;
    }

    /**
     * @return the optional exception which has been thrown
     */
    @Nullable
    public Throwable getException() {
        return exception;
    }

}
