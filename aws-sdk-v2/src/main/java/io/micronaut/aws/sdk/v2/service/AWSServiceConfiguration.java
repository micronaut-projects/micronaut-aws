/*
 * Copyright 2017-2022 original authors
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
package io.micronaut.aws.sdk.v2.service;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;

import java.net.URI;

/**
 * Configuration of AWS Services.
 *
 * @author Stephen Cprek
 * @since 3.10.0
 *
 */
public interface AWSServiceConfiguration {
    /**
     * @return The endpoint with which the AWS SDK should communicate
     * @since 3.10.0
     */
    @Nullable
    URI getEndpointOverride();

    /**
     *
     * @return AWS Service name. For example for s3 {@link software.amazon.awssdk.services.s3.S3Client#SERVICE_NAME}.
     */
    @NonNull
    String getServiceName();
}

