package io.micronaut.function.aws;

/**
 * Base interface for constants related to lambda execution.
 *
 * @since 1.3.1
 * @author graemerocher
 */
public interface MicronautLambdaContext {
    /**
     * An environment used when running Lambda functions.
     */
    String ENVIRONMENT_LAMBDA = "lambda";
}
