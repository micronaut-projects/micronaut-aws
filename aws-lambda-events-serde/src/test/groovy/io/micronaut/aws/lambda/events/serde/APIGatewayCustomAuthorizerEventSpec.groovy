package io.micronaut.aws.lambda.events.serde

import com.amazonaws.services.lambda.runtime.events.APIGatewayCustomAuthorizerEvent
import io.micronaut.json.JsonMapper
import spock.lang.Specification

class APIGatewayCustomAuthorizerEventSpec extends Specification {

    JsonMapper objectMapper = CustomPojoSerializerUtils.getJsonMapper()

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
