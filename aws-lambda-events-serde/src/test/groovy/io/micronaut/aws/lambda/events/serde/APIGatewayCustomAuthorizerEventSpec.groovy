/*
 * Copyright 2022 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.micronaut.aws.lambda.events.serde

import com.amazonaws.services.lambda.runtime.events.APIGatewayCustomAuthorizerEvent
import io.micronaut.serde.ObjectMapper
import spock.lang.Shared
import spock.lang.Specification

class APIGatewayCustomAuthorizerEventSpec extends Specification {
    @Shared
    ObjectMapper objectMapper = CustomPojoSerializerUtils.getObjectMapper()

    void "APIGatewayCustomAuthorizerEvent can be serialized"() {
        given:
        File f = new File("src/test/resources/apigateway-authorizer.json")

        expect:
        f.exists()

        when:
        String json = f.text
        APIGatewayCustomAuthorizerEvent event = objectMapper.readValue(json, APIGatewayCustomAuthorizerEvent)

        then:
        event
        "TOKEN" == event.type
        "incoming-client-token" == event.authorizationToken
        "arn:aws:execute-api:us-east-1:123456789012:example/prod/POST/{proxy+}" == event.methodArn
    }
}
