package io.micronaut.aws.secretsmanager

import io.micronaut.context.ApplicationContext
import spock.lang.Ignore
import spock.lang.Requires
import spock.lang.Specification

@Requires({
    return resolveAwsRegion() && hasAwsCredentialConfiguration()
})
@Ignore(value = "the test is coupled to a developer machine")
class SecretsManagerConfigImportAwsIntegrationSpec extends Specification {

    private static final String SECRET_NAME = '/config/myawesomesecret'
    private static final String EXPECTED_API_KEY = '12345-abcde'
    private static final String EXPECTED_USER = 'admin'

    void 'config import loads JSON properties from AWS Secrets Manager using default credentials chain'() {
        given:
        ApplicationContext context = ApplicationContext.run([
            'aws.secretsmanager.enabled': true,
            'micronaut.config.import': [
                "aws-secretsmanager:///${SECRET_NAME.substring(1)}?region=${resolveAwsRegion()}"
            ]
        ])

        expect:
        context.getRequiredProperty('api_key', String) == EXPECTED_API_KEY
        context.getRequiredProperty('user', String) == EXPECTED_USER

        cleanup:
        context.close()
    }

    private static String resolveAwsRegion() {
        String envRegion = System.getenv('AWS_REGION') ?: System.getenv('AWS_DEFAULT_REGION')
        if (envRegion) {
            return envRegion
        }
        String systemRegion = System.getProperty('aws.region') ?: System.getProperty('test.aws.region')
        if (systemRegion) {
            return systemRegion
        }
        String userHome = System.getProperty('user.home')
        if (!userHome) {
            return null
        }
        File configFile = new File(userHome, '.aws/config')
        if (!configFile.isFile()) {
            return null
        }
        String profile = System.getenv('AWS_PROFILE') ?: System.getenv('AWS_DEFAULT_PROFILE') ?: 'default'
        boolean inTargetProfile = false
        for (String line : configFile.readLines()) {
            String trimmed = line.trim()
            if (trimmed.startsWith('[') && trimmed.endsWith(']')) {
                String section = trimmed.substring(1, trimmed.length() - 1)
                inTargetProfile = section == 'default' ? profile == 'default' : section == "profile ${profile}"
                continue
            }
            if (inTargetProfile && trimmed.startsWith('region')) {
                int separatorIndex = trimmed.indexOf('=')
                if (separatorIndex > -1) {
                    return trimmed.substring(separatorIndex + 1).trim()
                }
            }
        }
        return null
    }

    private static boolean hasAwsCredentialConfiguration() {
        if ((System.getenv('AWS_ACCESS_KEY_ID') ?: '') && (System.getenv('AWS_SECRET_ACCESS_KEY') ?: '')) {
            return true
        }
        if ((System.getenv('AWS_PROFILE') ?: '') || (System.getenv('AWS_DEFAULT_PROFILE') ?: '')) {
            return true
        }
        String userHome = System.getProperty('user.home')
        if (!userHome) {
            return false
        }
        File credentialsFile = new File(userHome, '.aws/credentials')
        File configFile = new File(userHome, '.aws/config')
        return credentialsFile.isFile() || configFile.isFile()
    }
}
