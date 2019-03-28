package io.micronaut.function.aws.alexa;

import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.context.annotation.Requires;

import static io.micronaut.function.aws.alexa.AlexaConfiguration.PREFIX;

/**
 * This allows configuring properties that area AWS Alexa specific such as skill-id for skill verification.
 */
@ConfigurationProperties(PREFIX)
@Requires(env = AlexaFunction.ENV_ALEXA)
public class AlexaConfiguration {

    static final String PREFIX = "alexa";

    private String skillId;

    /**
     * The Skill ID of this Alexa skill.
     * @return skill id
     */
    public String getSkillId() {
        return skillId;
    }

    /**
     * The Skill ID of this Alexa skill.
     * @param skillId skill id
     */
    public void setSkillId(String skillId) {
        this.skillId = skillId;
    }
}
