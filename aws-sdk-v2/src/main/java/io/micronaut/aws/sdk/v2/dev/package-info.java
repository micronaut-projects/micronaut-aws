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
 * Development mode support for the AWS SDK v2 clients: credentials and region providers that copy the {@code aws}
 * configuration instead of holding the environment, so that the clients built on them can be retained across a
 * restart, and a retention policy that refuses the clients holding classes of the application. Every bean of the
 * package exists only in development mode.
 *
 * @author graemerocher
 * @since 5.2.0
 */
@Internal
@NullMarked
package io.micronaut.aws.sdk.v2.dev;

import io.micronaut.core.annotation.Internal;
import org.jspecify.annotations.NullMarked;
