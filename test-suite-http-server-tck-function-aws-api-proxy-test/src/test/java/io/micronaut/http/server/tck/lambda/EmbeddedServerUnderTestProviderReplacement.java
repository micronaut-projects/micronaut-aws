package io.micronaut.http.server.tck.lambda;

import io.micronaut.core.annotation.Experimental;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.http.tck.ServerUnderTest;
import io.micronaut.http.tck.ServerUnderTestProvider;
import java.util.Map;

@Experimental
public class EmbeddedServerUnderTestProviderReplacement implements ServerUnderTestProvider {

    @Override
    @NonNull
    public ServerUnderTest getServer(@NonNull Map<String, Object> properties) {
        return new EmbeddedServerUnderTestReplacement(properties);
    }
}
