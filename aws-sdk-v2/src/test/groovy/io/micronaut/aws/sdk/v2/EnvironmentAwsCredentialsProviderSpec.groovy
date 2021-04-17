/*
 * Copyright 2017-2019 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micronaut.aws.sdk.v2

import io.micronaut.context.ApplicationContext
import software.amazon.awssdk.auth.credentials.AwsCredentials
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider
import software.amazon.awssdk.auth.credentials.AwsSessionCredentials
import spock.lang.Specification
import spock.util.environment.RestoreSystemProperties

class EnvironmentAwsCredentialsProviderSpec extends Specification {

    static final String TEST_KEY_ID = "testKeyId"
    static final String TEST_SECRET_KEY = "testSecretKey"
    static final String TEST_SESSION_TOKEN = "testSessionToken"


    void "AWS accessKeyId and secretKey can be read from environment"() {
        given:
        ApplicationContext applicationContext = ApplicationContext.run([
                "aws.accessKeyId": TEST_KEY_ID,
                "aws.secretKey"  : TEST_SECRET_KEY
        ])

        when:
        AwsCredentials awsCredentials =
                applicationContext.getBean(AwsCredentialsProvider).resolveCredentials()

        then:
        awsCredentials.accessKeyId() == TEST_KEY_ID
        awsCredentials.secretAccessKey() == TEST_SECRET_KEY

        cleanup:
        applicationContext.close()
    }

    void "AWS alternate accessKey and secretAccessKey can be read from environment"() {
        given:
        ApplicationContext applicationContext = ApplicationContext.run([
                "aws.accessKeyId": null,
                "aws.secretKey": null,
                "aws.accessKey": TEST_KEY_ID,
                "aws.secretAccessKey": TEST_SECRET_KEY
        ])

        when:
        AwsCredentials awsCredentials =
                applicationContext.getBean(AwsCredentialsProvider).resolveCredentials()

        then:
        awsCredentials.accessKeyId() == TEST_KEY_ID
        awsCredentials.secretAccessKey() == TEST_SECRET_KEY

        cleanup:
        applicationContext.close()
    }

    void "AWS accessKeyId, secretKey, and sessionToken can be read from environment"() {
        given:
        ApplicationContext applicationContext = ApplicationContext.run([
                "aws.accessKeyId": TEST_KEY_ID,
                "aws.secretKey": TEST_SECRET_KEY,
                "aws.sessionToken": TEST_SESSION_TOKEN
        ])

        when:
        AwsSessionCredentials awsCredentials =
                applicationContext.getBean(AwsCredentialsProvider).resolveCredentials() as AwsSessionCredentials

        then:
        awsCredentials.accessKeyId() == TEST_KEY_ID
        awsCredentials.secretAccessKey() == TEST_SECRET_KEY
        awsCredentials.sessionToken() == TEST_SESSION_TOKEN

        cleanup:
        applicationContext.close()
    }

    void "AWS accessKeyId, secretKey, and sessionToken can be read via yaml configuration"() {
        given:
        ApplicationContext applicationContext = ApplicationContext.run("yaml")

        when:
        AwsSessionCredentials awsCredentials =
                applicationContext.getBean(AwsCredentialsProvider).resolveCredentials() as AwsSessionCredentials

        then:
        awsCredentials.accessKeyId() == "yamlAccessKeyId"
        awsCredentials.secretAccessKey() == "yamlSecretKey"
        awsCredentials.sessionToken() == "yamlSessionToken"

        cleanup:
        applicationContext.close()
    }


    @RestoreSystemProperties
    void "credentials can still be read from default places like system properties"() {
        given:
        System.setProperty('aws.accessKeyId', TEST_KEY_ID)
        System.setProperty('aws.secretKey', TEST_SECRET_KEY)
        ApplicationContext applicationContext = ApplicationContext.run()

        when:
        AwsCredentials awsCredentials =
                applicationContext.getBean(AwsCredentialsProvider).resolveCredentials()

        then:
        awsCredentials.accessKeyId() == TEST_KEY_ID
        awsCredentials.secretAccessKey() == TEST_SECRET_KEY

        cleanup:
        applicationContext.close()
    }
}
