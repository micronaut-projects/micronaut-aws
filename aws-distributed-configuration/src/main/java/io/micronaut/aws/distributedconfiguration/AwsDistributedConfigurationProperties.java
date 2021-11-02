package io.micronaut.aws.distributedconfiguration;

import io.micronaut.aws.AWSConfiguration;
import io.micronaut.context.annotation.BootstrapContextCompatible;
import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.core.annotation.Experimental;

import javax.validation.constraints.NotNull;

/**
 * Properties used for Secret loading by keys. Where keys are specified after SECRETS: and separated by ;.
 * I have left name will change once PR Discussion is settled.
 * @author Matej Nedic
 * @since ?
 */
@BootstrapContextCompatible
@ConfigurationProperties(AwsDistributedConfigurationProperties.PREFIX)
@Experimental
public class AwsDistributedConfigurationProperties {

    public static final String PREFIX = AWSConfiguration.PREFIX + ".distributed-configuration";
    public static final String STARTS_WITH = "SECRETS:";

    //Validation on null or on missing SECRETS: prefix starts_with should fail application?
    @NotNull
    private String secrets;

    public String getSecrets() {
        return secrets;
    }

    public void setSecrets(String secrets) {
        this.secrets = secrets;
    }

}
