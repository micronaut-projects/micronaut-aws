package io.micronaut.aws.sdk.v2;

import io.micronaut.context.env.Environment;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.regions.providers.AwsRegionProvider;

/**
 * A {@link AwsRegionProvider} that reads from the {@link Environment}.
 *
 * @author Vladimír Oraný
 * @since 2.0.0
 */
public class EnvironmentAwsRegionProvider implements AwsRegionProvider {

    /**
     * Environment variable name for the AWS access key ID.
     */
    public static final String REGION_ENV_VAR = "aws.region";

    private final Environment environment;

    /**
     * Constructor.
     * @param environment environment
     */
    public EnvironmentAwsRegionProvider(Environment environment) {
        this.environment = environment;
    }

    @Override
    public Region getRegion() {
        return environment.getProperty(REGION_ENV_VAR, String.class)
                .map(Region::of)
                .orElse(null);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}
