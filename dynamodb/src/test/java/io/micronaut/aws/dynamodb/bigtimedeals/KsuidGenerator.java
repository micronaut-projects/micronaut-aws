package io.micronaut.aws.dynamodb.bigtimedeals;

import com.github.ksuid.Ksuid;
import jakarta.inject.Singleton;

@Singleton
public class KsuidGenerator implements IdGenerator {
    @Override
    public String generate() {
        return Ksuid.newKsuid().toString();
    }
}
