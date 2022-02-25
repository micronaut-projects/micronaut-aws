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
package io.micronaut.aws.xray.strategy;

import io.micronaut.context.annotation.Primary;
import io.micronaut.core.annotation.NonNull;
import jakarta.inject.Singleton;
import java.util.List;
import java.util.Optional;

/**
 * Attempts to resolve the segment name using every bean of type {@link SegmentNamingStrategy}.
 * @author Sergio del Amo
 * @since 3.2.0
 * @param <T> Request
 */
@Primary
@Singleton
public class CompositeSegmentNamingStrategy<T> implements SegmentNamingStrategy<T> {

    private final List<SegmentNamingStrategy<T>> segmentNamingStrategyList;

    public CompositeSegmentNamingStrategy(List<SegmentNamingStrategy<T>> segmentNamingStrategyList) {
        this.segmentNamingStrategyList = segmentNamingStrategyList;
    }

    @Override
    @NonNull
    public Optional<String> resolveName(@NonNull T request) {
        return segmentNamingStrategyList.stream()
                .filter(it -> it.resolveName(request).isPresent())
                .map(it -> it.resolveName(request).orElse(null))
                .findFirst();
    }
}
