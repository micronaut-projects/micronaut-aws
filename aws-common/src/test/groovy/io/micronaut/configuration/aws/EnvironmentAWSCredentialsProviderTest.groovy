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
package io.micronaut.configuration.aws

import com.amazonaws.auth.AWSCredentials
import com.amazonaws.auth.BasicSessionCredentials
import io.micronaut.context.ApplicationContext
import io.micronaut.context.env.Environment
import io.micronaut.context.env.PropertySource
import io.micronaut.core.util.CollectionUtils
import spock.lang.Specification

class EnvironmentAWSCredentialsProviderTest extends Specification {

    String TEST_KEY_ID = "testKeyId"
    String TEST_SECRET_KEY = "testSecretKey"
    String TEST_SESSION_TOKEN = "testSessionToken"


    def "AWS accessKeyId and secretKey can be read from environment"() {
        given:
        ApplicationContext applicationContext = ApplicationContext.run(
                PropertySource.of(CollectionUtils.mapOf(
                        "aws.accessKeyId", "$TEST_KEY_ID",
                        "aws.secretKey", "$TEST_SECRET_KEY"))
        )

        Environment environment = applicationContext.getEnvironment()
        EnvironmentAWSCredentialsProvider awsCredentialsProvider = new
                EnvironmentAWSCredentialsProvider((environment))

        when:
        AWSCredentials awsCredentials = awsCredentialsProvider.getCredentials()

        then:
        awsCredentials.getAWSAccessKeyId().equals(TEST_KEY_ID)
        awsCredentials.getAWSSecretKey().equals(TEST_SECRET_KEY)
    }

    def "AWS alternate accessKey and secretAccessKey can be read from environment"() {
        given:
        ApplicationContext applicationContext = ApplicationContext.run(
                PropertySource.of(CollectionUtils.mapOf(
                        //  nulling out non alternate values so that alternates will get picked up
                        "aws.accessKeyId", null,
                        "aws.secretKey", null,
                        "aws.accessKey", "$TEST_KEY_ID",
                        "aws.secretAccessKey", "$TEST_SECRET_KEY")))

        Environment environment = applicationContext.getEnvironment()
        EnvironmentAWSCredentialsProvider awsCredentialsProvider = new
                EnvironmentAWSCredentialsProvider((environment))

        when:
        AWSCredentials awsCredentials = awsCredentialsProvider.getCredentials()

        then:
        awsCredentials.getAWSAccessKeyId().equals(TEST_KEY_ID)
        awsCredentials.getAWSSecretKey().equals(TEST_SECRET_KEY)
    }

    def "AWS accessKeyId, secretKey, and sessionToken can be read from environment"() {
        given:
        ApplicationContext applicationContext = ApplicationContext.run(
                PropertySource.of(CollectionUtils.mapOf(
                        "aws.accessKeyId", "$TEST_KEY_ID",
                        "aws.secretKey", "$TEST_SECRET_KEY",
                        "aws.sessionToken", "$TEST_SESSION_TOKEN")))

        Environment environment = applicationContext.getEnvironment()
        EnvironmentAWSCredentialsProvider awsCredentialsProvider = new
                EnvironmentAWSCredentialsProvider((environment))

        when:
        BasicSessionCredentials awsCredentials = awsCredentialsProvider.getCredentials()

        then:
        awsCredentials.getAWSAccessKeyId().equals(TEST_KEY_ID)
        awsCredentials.getAWSSecretKey().equals(TEST_SECRET_KEY)
        awsCredentials.getSessionToken().equals(TEST_SESSION_TOKEN)
    }

    def "AWS accessKeyId, secretKey, and sessionToken can be read via yaml configuration"() {
        given:
        ApplicationContext applicationContext = ApplicationContext.run()
        Environment environment = applicationContext.getEnvironment()
        EnvironmentAWSCredentialsProvider awsCredentialsProvider = new
                EnvironmentAWSCredentialsProvider((environment))

        when:
        BasicSessionCredentials awsCredentials = awsCredentialsProvider.getCredentials()

        then:
        awsCredentials.getAWSAccessKeyId().equals("yamlAccessKeyId")
        awsCredentials.getAWSSecretKey().equals("yamlSecretKey")
        awsCredentials.getSessionToken().equals("yamlSessionToken")
    }

}
