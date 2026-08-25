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
/**
 * AWS Common Runtime (CRT) HTTP client configuration and factory.
 *
 * @since 5.1.0
 */
@NullMarked
@Requires(classes = {AwsCrtHttpClient.class, AwsCrtAsyncHttpClient.class})
@Configuration
package io.micronaut.aws.sdk.v2.client.crt;

import io.micronaut.context.annotation.Configuration;
import io.micronaut.context.annotation.Requires;
import org.jspecify.annotations.NullMarked;
import software.amazon.awssdk.http.crt.AwsCrtAsyncHttpClient;
import software.amazon.awssdk.http.crt.AwsCrtHttpClient;
