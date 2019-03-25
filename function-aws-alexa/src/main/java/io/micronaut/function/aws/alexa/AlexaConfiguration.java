package io.micronaut.function.aws.alexa;

import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.context.annotation.Requires;

import static io.micronaut.function.aws.alexa.AlexaConfiguration.PREFIX;

@ConfigurationProperties(PREFIX)
@Requires(env = AlexaFunction.ENV_ALEXA)
public class AlexaConfiguration {

    static final String PREFIX = "alexa";

    private String skillId;

    public String getSkillId() {
        return skillId;
    }

    public void setSkillId(String skillId) {
        this.skillId = skillId;
    }
}
