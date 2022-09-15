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

import io.micronaut.context.annotation.ConfigurationProperties;

import java.time.Duration;

/**
 * Configuration for support of {@link io.micronaut.scheduling.annotation.Async} execution on AWS Lambda.
 *
 * @author Vladimir Orany
 * @since 3.9.1
 */
@ConfigurationProperties("micronaut.aws.async")
public class AsyncSupportConfiguration {

    private Duration awaitTermination = Duration.ofSeconds(10);

    /**
     * @return the maximum time for how long should the {@link AsyncSupport} wait for all tasks to be completed.
     */
    public Duration getAwaitTermination() {
        return awaitTermination;
    }

    /**
     * @param awaitTermination the maximum time for how long should the {@link AsyncSupport} wait for all tasks to be completed.
     */
    public void setAwaitTermination(Duration awaitTermination) {
        this.awaitTermination = awaitTermination;
    }

}
