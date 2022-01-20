package io.micronaut.aws.lambda.events.serde

import com.amazonaws.services.lambda.runtime.events.APIGatewayCustomAuthorizerEvent
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent
import io.micronaut.context.BeanContext
import io.micronaut.serde.ObjectMapper
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import spock.lang.Specification

@MicronautTest(startApplication = false)
class APIGatewayCustomAuthorizerEventSpec extends Specification  {
    @Inject
    ObjectMapper objectMapper

    @Inject
    BeanContext beanContext

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
