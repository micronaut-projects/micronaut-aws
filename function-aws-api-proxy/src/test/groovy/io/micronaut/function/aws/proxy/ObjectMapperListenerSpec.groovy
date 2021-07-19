package io.micronaut.function.aws.proxy

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import io.micronaut.context.ApplicationContext
import io.micronaut.context.annotation.Requires
import io.micronaut.context.event.BeanCreatedEvent
import io.micronaut.context.event.BeanCreatedEventListener
import io.micronaut.inject.qualifiers.Qualifiers
import spock.lang.Issue
import spock.lang.Specification

import javax.inject.Named
import javax.inject.Singleton

@Issue("https://github.com/micronaut-projects/micronaut-aws/issues/186")
class ObjectMapperListenerSpec extends Specification {

    void "the aws object mapper can be customised" () {
        given:
        MicronautLambdaContainerHandler handler = new MicronautLambdaContainerHandler(
                ApplicationContext.builder().properties([
                        'spec.name': 'ObjectMapperListenerSpec',
                        'jackson.property-naming-strategy': 'SNAKE_CASE',
                        'aws.proxy.shared-object-mapper': false
                ])
        )

        when:
        ObjectMapper global = handler.applicationContext.getBean(ObjectMapper)
        ObjectMapper aws = handler.applicationContext.getBean(ObjectMapper, Qualifiers.byName("aws"))

        then:
        global.deserializationConfig.propertyNamingStrategy == PropertyNamingStrategy.SNAKE_CASE
        aws.deserializationConfig.propertyNamingStrategy == PropertyNamingStrategy.UPPER_CAMEL_CASE
    }

    @Singleton
    @Requires(property = 'spec.name', value = 'ObjectMapperListenerSpec')
    static class ObjectMapperListener implements BeanCreatedEventListener<ObjectMapper> {

        @Override
        ObjectMapper onCreated(BeanCreatedEvent<ObjectMapper> event) {
            ObjectMapper bean = event.bean
            event.getBeanDefinition()
                    .findAnnotation(Named.class)
                    .map{ n -> n.getValues().get("value") as String }
                    .filter{v -> v == "aws"}
                    .ifPresent{
                        bean.propertyNamingStrategy = PropertyNamingStrategy.UPPER_CAMEL_CASE
                    }
            return bean
        }
    }

}
