package io.micronaut.aws.lambda.events

import io.micronaut.context.BeanContext
import io.micronaut.core.type.Argument
import io.micronaut.serde.SerdeIntrospections
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import spock.lang.Specification

@MicronautTest(startApplication = false)
class APIGatewayV2HTTPResponseSpec extends Specification {

    @Inject
    BeanContext beanContext

    void "APIGatewayV2HTTPResponse implements equals and hash code"() {
        given:
        APIGatewayV2HTTPResponse a = new APIGatewayV2HTTPResponse()
        a.setStatusCode(200)
        a.setBody('{"message":"Hello World"}')

        APIGatewayV2HTTPResponse b = new APIGatewayV2HTTPResponse()
        b.setStatusCode(200)
        b.setBody('{"message":"Hello World"}')

        expect:
        a == b
    }

    void "APIGatewayV2HTTPResponse is annotated with @Serdeable.Deserializable"() {
        given:
        SerdeIntrospections serdeIntrospections = beanContext.getBean(SerdeIntrospections)

        when:
        serdeIntrospections.getDeserializableIntrospection(Argument.of(APIGatewayV2HTTPResponse))

        then:
        noExceptionThrown()
    }

    void "APIGatewayV2HTTPResponse is annotated with @Serdeable.Serializable"() {
        given:
        SerdeIntrospections serdeIntrospections = beanContext.getBean(SerdeIntrospections)

        when:
        serdeIntrospections.getSerializableIntrospection(Argument.of(APIGatewayV2HTTPResponse))

        then:
        noExceptionThrown()
    }


}
