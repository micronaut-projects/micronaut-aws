/*
 * Copyright 2017-2019 original authors
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
package io.micronaut.function.client.aws

import io.micronaut.context.ApplicationContext
import io.micronaut.core.async.annotation.SingleResult
import io.micronaut.core.type.Argument
import io.micronaut.function.client.FunctionClient
import io.micronaut.function.client.FunctionDefinition
import io.micronaut.function.client.FunctionInvoker
import io.micronaut.function.client.FunctionInvokerChooser
import io.micronaut.http.annotation.Body
import org.reactivestreams.Publisher
import reactor.core.publisher.Flux
import spock.lang.Ignore
import spock.lang.IgnoreIf
import spock.lang.Issue
import spock.lang.PendingFeature
import spock.lang.Specification

import jakarta.inject.Named

/**
 * @author graemerocher
 * @since 1.0
 */
@IgnoreIf({
    return !new File("${System.getProperty("user.home")}/.aws/credentials").exists()
})
class AwsLambdaInvokeSpec extends Specification {

    void "test setup function definitions"() {
        given:
        ApplicationContext applicationContext = ApplicationContext.run(
                'aws.lambda.functions.test.function-name':'micronaut-function',
                'aws.lambda.functions.test.qualifier':'something'
        )

        Collection<FunctionDefinition> definitions = applicationContext.getBeansOfType(FunctionDefinition)

        expect:
        definitions.size() == 1
        definitions.first() instanceof AWSInvokeRequestDefinition

        when:
        AWSInvokeRequestDefinition invokeRequestDefinition = (AWSInvokeRequestDefinition) definitions.first()

        then:
        invokeRequestDefinition.name == 'test'
        invokeRequestDefinition.invokeRequest.functionName == 'micronaut-function'
        invokeRequestDefinition.invokeRequest.qualifier == 'something'

        cleanup:
        applicationContext.close()
    }

    @Issue([
            "https://github.com/micronaut-projects/micronaut-core/issues/9154",
            "https://github.com/micronaut-projects/micronaut-core/pull/9155"
    ])
    @PendingFeature(reason = "This should start passing once we have an M3 of micronaut-core")
    void "test setup lambda config"() {
        given:
        ApplicationContext applicationContext = ApplicationContext.run(
                'aws.lambda.functions.test.functionName':'micronaut-function',
                'aws.lambda.functions.test.qualifier':'something',
                'aws.lambda.region':'us-east-1'
        )
        AWSLambdaConfiguration configuration = applicationContext.getBean(AWSLambdaConfiguration)
        FunctionDefinition definition = applicationContext.getBean(FunctionDefinition)
        FunctionInvokerChooser chooser = applicationContext.getBean(FunctionInvokerChooser)
        Optional<FunctionInvoker> invoker = chooser.choose(definition)

        expect:
        configuration.builder.region == 'us-east-1'
        invoker.isPresent()

        cleanup:
        applicationContext.close()
    }

    @Ignore
    void "test invoke function"() {
        given:
        ApplicationContext applicationContext = ApplicationContext.run(
                'aws.lambda.functions.test.functionName':'micronaut-function',
                'aws.lambda.region':'us-east-1'
        )

        when:
        FunctionDefinition definition = applicationContext.getBean(FunctionDefinition)
        FunctionInvokerChooser chooser = applicationContext.getBean(FunctionInvokerChooser)
        Optional<FunctionInvoker> invoker = chooser.choose(definition)

        then:
        invoker.isPresent()

        when:
        FunctionInvoker invokerInstance = invoker.get()

        Book book = invokerInstance.invoke(definition, new Book(title: "The Stand"), Argument.of(Book))

        then:
        book != null
        book.title == "THE STAND"

        cleanup:
        applicationContext.close()
    }

    @Ignore
    void "test invoke client with @FunctionClient"() {
        given:
        ApplicationContext applicationContext = ApplicationContext.run(
                'aws.lambda.functions.test.functionName':'micronaut-function',
                'aws.lambda.region':'us-east-1'
        )

        when:
        MyClient myClient = applicationContext.getBean(MyClient)
        Book book = myClient.micronautFunction( new Book(title: "The Stand") )

        then:
        book != null
        book.title == "THE STAND"

        when:
        book = myClient.someOtherName( "The Stand" )

        then:
        book != null
        book.title == "THE STAND"

        when:
        book = Flux.from(myClient.reactiveInvoke( "The Stand" )).blockFirst()

        then:
        book != null
        book.title == "THE STAND"

        cleanup:
        applicationContext.close()
    }

    static class Book {
        String title
    }

    //tag::functionClient[]
    @FunctionClient
    static interface MyClient {

        Book micronautFunction(@Body Book book)

        @Named('micronaut-function')
        Book someOtherName(String title)

        @Named('micronaut-function')
        @SingleResult
        Publisher<Book> reactiveInvoke(String title)
    }
    //end::functionClient[]
}
