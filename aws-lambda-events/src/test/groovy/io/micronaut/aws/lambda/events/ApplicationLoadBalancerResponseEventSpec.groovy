package io.micronaut.aws.lambda.events

import io.micronaut.context.BeanContext
import io.micronaut.core.type.Argument
import io.micronaut.serde.SerdeIntrospections
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import spock.lang.Specification

@MicronautTest(startApplication = false)
class ApplicationLoadBalancerResponseEventSpec extends Specification {

    @Inject
    BeanContext beanContext

    void "ApplicationLoadBalancerResponseEvent implements equals and hash code"() {
        given:
        ApplicationLoadBalancerResponseEvent a = new ApplicationLoadBalancerResponseEvent()
        a.setStatusCode(200)
        a.setBody('{"message":"Hello World"}')

        ApplicationLoadBalancerResponseEvent b = new ApplicationLoadBalancerResponseEvent()
        b.setStatusCode(200)
        b.setBody('{"message":"Hello World"}')

        expect:
        a == b
    }

    void "ApplicationLoadBalancerResponseEvent is annotated with @Serdeable.Deserializable"(Class clazz) {
        given:
        SerdeIntrospections serdeIntrospections = beanContext.getBean(SerdeIntrospections)

        when:
        serdeIntrospections.getDeserializableIntrospection(Argument.of(clazz))

        then:
        noExceptionThrown()

        where:
        clazz << [
                ApplicationLoadBalancerResponseEvent
        ]
    }

    void "ApplicationLoadBalancerResponseEvent is annotated with @Serdeable.Serializable"(Class clazz) {
        given:
        SerdeIntrospections serdeIntrospections = beanContext.getBean(SerdeIntrospections)

        when:
        serdeIntrospections.getSerializableIntrospection(Argument.of(clazz))

        then:
        noExceptionThrown()

        where:
        clazz << [
                ApplicationLoadBalancerResponseEvent
        ]
    }
}
