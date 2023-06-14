package io.micronaut.aws.lambda.events

import io.micronaut.context.BeanContext
import io.micronaut.core.type.Argument
import io.micronaut.serde.SerdeIntrospections
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import spock.lang.Specification

@MicronautTest(startApplication = false)
class APIGatewayProxyResponseEventSpec extends Specification {

    @Inject
    BeanContext beanContext

    void "APIGatewayProxyResponseEvent is annotated with @Serdeable.Deserializable"() {
        given:
        SerdeIntrospections serdeIntrospections = beanContext.getBean(SerdeIntrospections)

        when:
        serdeIntrospections.getDeserializableIntrospection(Argument.of(APIGatewayProxyResponseEvent))

        then:
        noExceptionThrown()
    }

    void "APIGatewayProxyResponseEvent is annotated with @Serdeable.Serializable"() {
        given:
        SerdeIntrospections serdeIntrospections = beanContext.getBean(SerdeIntrospections)

        when:
        serdeIntrospections.getSerializableIntrospection(Argument.of(APIGatewayProxyResponseEvent))

        then:
        noExceptionThrown()
    }
}
