package io.micronaut.aws.lambda.events

import io.micronaut.context.BeanContext
import io.micronaut.core.type.Argument
import io.micronaut.serde.SerdeIntrospections
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import spock.lang.Specification

@MicronautTest(startApplication = false)
class APIGatewayProxyRequestEventSpec extends Specification {

    @Inject
    BeanContext beanContext

    void "APIGatewayProxyRequestEvent is annotated with @Serdeable.Deserializable"(Class clazz) {
        given:
        SerdeIntrospections serdeIntrospections = beanContext.getBean(SerdeIntrospections)

        when:
        serdeIntrospections.getDeserializableIntrospection(Argument.of(clazz))

        then:
        noExceptionThrown()

        where:
        clazz << [
                APIGatewayProxyRequestEvent,
                APIGatewayProxyRequestEvent.ProxyRequestContext,
                APIGatewayProxyRequestEvent.RequestIdentity
        ]
    }

    void "APIGatewayProxyRequestEvent is annotated with @Serdeable.Serializable"(Class clazz) {
        given:
        SerdeIntrospections serdeIntrospections = beanContext.getBean(SerdeIntrospections)

        when:
        serdeIntrospections.getSerializableIntrospection(Argument.of(clazz))

        then:
        noExceptionThrown()

        where:
        clazz << [
                APIGatewayProxyRequestEvent,
                APIGatewayProxyRequestEvent.ProxyRequestContext,
                APIGatewayProxyRequestEvent.RequestIdentity
        ]
    }
}
