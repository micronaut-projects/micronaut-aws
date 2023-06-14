package io.micronaut.aws.lambda.events

import io.micronaut.context.BeanContext
import io.micronaut.core.type.Argument
import io.micronaut.serde.SerdeIntrospections
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import spock.lang.Specification

@MicronautTest(startApplication = false)
class ApplicationLoadBalancerRequestEventSpec extends Specification {

    @Inject
    BeanContext beanContext

    void "ApplicationLoadBalancerRequestEvent implements equals and hash code"() {
        given:
        ApplicationLoadBalancerRequestEvent a = new ApplicationLoadBalancerRequestEvent()
        a.path = "/messages"
        a.setBody('{"message":"Hello World"}')

        ApplicationLoadBalancerRequestEvent b = new ApplicationLoadBalancerRequestEvent()
        b.path = "/messages"
        b.setBody('{"message":"Hello World"}')

        expect:
        a == b
    }

    void "ApplicationLoadBalancerRequestEvent is annotated with @Serdeable.Deserializable"(Class clazz) {
        given:
        SerdeIntrospections serdeIntrospections = beanContext.getBean(SerdeIntrospections)

        when:
        serdeIntrospections.getDeserializableIntrospection(Argument.of(clazz))

        then:
        noExceptionThrown()

        where:
        clazz << [
                ApplicationLoadBalancerRequestEvent,
                ApplicationLoadBalancerRequestEvent.RequestContext,
                ApplicationLoadBalancerRequestEvent.Elb
        ]
    }

    void "ApplicationLoadBalancerRequestEvent is annotated with @Serdeable.Serializable"(Class clazz) {
        given:
        SerdeIntrospections serdeIntrospections = beanContext.getBean(SerdeIntrospections)

        when:
        serdeIntrospections.getSerializableIntrospection(Argument.of(clazz))

        then:
        noExceptionThrown()

        where:
        clazz << [
                ApplicationLoadBalancerRequestEvent,
                ApplicationLoadBalancerRequestEvent.RequestContext,
                ApplicationLoadBalancerRequestEvent.Elb
        ]
    }
}