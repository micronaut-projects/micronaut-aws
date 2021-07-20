package io.micronaut.function.aws.proxy


import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import io.micronaut.context.ApplicationContext
import io.micronaut.context.exceptions.NoSuchBeanException
import io.micronaut.inject.qualifiers.Qualifiers
import spock.lang.Issue
import spock.lang.Specification

@Issue("https://github.com/micronaut-projects/micronaut-aws/issues/186")
class ObjectMapperSpec extends Specification {

    void "by default, the object mapper is shared" () {
        given:
        MicronautLambdaContainerHandler handler = new MicronautLambdaContainerHandler(ApplicationContext.builder())

        when:
        ObjectMapper objectMapper = handler.applicationContext.getBean(ObjectMapper)

        then:
        objectMapper.deserializationConfig.propertyNamingStrategy == null

        when:
        handler.applicationContext.getBean(ObjectMapper, Qualifiers.byName("aws"))

        then:
        thrown(NoSuchBeanException)
    }

    void "when changing the global object mapper configuration, by default it is still shared"() {
        given:
        MicronautLambdaContainerHandler handler = new MicronautLambdaContainerHandler(
                ApplicationContext.builder().properties([
                        'jackson.property-naming-strategy': 'SNAKE_CASE'
                ])
        )

        expect:
        handler.applicationContext.getBean(ObjectMapper) != null

        when:
        handler.applicationContext.getBean(ObjectMapper, Qualifiers.byName("aws"))

        then:
        thrown(NoSuchBeanException)
    }


    void "when changing global object mapper configuration, it can be configured to create a new one for aws" () {
        given:
        MicronautLambdaContainerHandler handler = new MicronautLambdaContainerHandler(
                ApplicationContext.builder().properties([
                        'jackson.property-naming-strategy': 'SNAKE_CASE',
                        'aws.proxy.shared-object-mapper': false
                ])
        )

        when:
        ObjectMapper global = handler.applicationContext.getBean(ObjectMapper)
        ObjectMapper aws = handler.applicationContext.getBean(ObjectMapper, Qualifiers.byName("aws"))

        then:
        global.deserializationConfig.propertyNamingStrategy == PropertyNamingStrategies.SNAKE_CASE ||
                global.deserializationConfig.propertyNamingStrategy == PropertyNamingStrategy.SNAKE_CASE
        aws.deserializationConfig.propertyNamingStrategy == null
    }

}
