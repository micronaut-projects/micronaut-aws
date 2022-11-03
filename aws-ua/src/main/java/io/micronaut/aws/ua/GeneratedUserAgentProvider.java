package io.micronaut.aws.ua;

import io.micronaut.context.annotation.Requires;
import io.micronaut.core.annotation.NonNull;
import jakarta.inject.Singleton;
import java.lang.Override;
import java.lang.String;

@Singleton
@Requires(
    property = "aws.ua.enabled",
    value = "true",
    defaultValue = "true"
)
public final class GeneratedUserAgentProvider implements UserAgentProvider {
  @NonNull
  @Override
  public String userAgent() {
    return "micronaut/3.7.3";
  }
}
