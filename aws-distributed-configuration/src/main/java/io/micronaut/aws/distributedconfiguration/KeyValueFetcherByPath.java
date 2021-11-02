package io.micronaut.aws.distributedconfiguration;

import io.micronaut.core.annotation.Experimental;
import io.micronaut.core.annotation.NonNull;

import java.util.Optional;

@Experimental
@FunctionalInterface
public interface KeyValueFetcherByPath {

    @NonNull
    Optional<java.util.Map<String,String>> keyValuesByPrefix(@NonNull String keyOrPath, String version);
}
