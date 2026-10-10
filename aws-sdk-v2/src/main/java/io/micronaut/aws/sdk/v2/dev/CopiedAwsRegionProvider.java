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

import io.micronaut.aws.sdk.v2.EnvironmentAwsRegionProvider;
import io.micronaut.core.annotation.Internal;
import org.jspecify.annotations.Nullable;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.regions.providers.AwsRegionProvider;

/**
 * The development mode counterpart of {@link EnvironmentAwsRegionProvider}: the region of
 * {@value EnvironmentAwsRegionProvider#REGION_ENV_VAR}, from the value it copied when it was made rather than from
 * the environment, which it does not hold, or null when there is none, so that the chain asks the next provider.
 *
 * <p>The environment provider reads the value on every call, so that it follows a refreshed environment. This one
 * copies it once, and in development mode a change under {@code aws} releases it, with the clients holding it, and
 * the next context makes another from the changed value.</p>
 *
 * @author graemerocher
 * @since 5.2.0
 */
@Internal
final class CopiedAwsRegionProvider implements AwsRegionProvider {

    private final @Nullable String region;

    /**
     * @param environment The values copied from the environment
     */
    CopiedAwsRegionProvider(AwsEnvironmentSnapshot environment) {
        this.region = environment.getRegion();
    }

    @Override
    public @Nullable Region getRegion() {
        // as the environment provider, which makes the region on each call: an invalid one fails the call, not the bean
        return region == null ? null : Region.of(region);
    }

    @Override
    public String toString() {
        return EnvironmentAwsRegionProvider.class.getSimpleName();
    }
}
