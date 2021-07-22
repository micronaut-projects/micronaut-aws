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

//tag::import[]
import io.micronaut.function.client.FunctionClient
import jakarta.inject.Named
//end::import[]

import io.micronaut.runtime.server.EmbeddedServer
//tag::rxImport[]
import org.reactivestreams.Publisher
import reactor.core.publisher.Mono

//end::rxImport[]
import spock.lang.Specification

/**
 * @author graemerocher
 * @since 1.0
 */
class LocalFunctionInvokeSpec extends Specification {

    //tag::invokeLocalFunction[]
    void "test invoking a local function"() {
        given:
        EmbeddedServer server = ApplicationContext.run(EmbeddedServer)
        MathClient mathClient = server.getApplicationContext().getBean(MathClient)

        expect:
        mathClient.max() == Integer.MAX_VALUE.toLong()
        mathClient.rnd(1.6) == 2
        mathClient.sum(new Sum(a:5,b:10)) == 15

    }
    //end::invokeLocalFunction[]

    //tag::invokeRxLocalFunction[]
    void "test invoking a local function - rx"() {
        given:
        EmbeddedServer server = ApplicationContext.run(EmbeddedServer)
        ReactiveMathClient mathClient = server.getApplicationContext().getBean(ReactiveMathClient)

        expect:
        Mono.from(mathClient.max()).block() == Integer.MAX_VALUE.toLong()
        Mono.from(mathClient.rnd(1.6)).block() == 2
        Mono.from(mathClient.sum(new Sum(a:5,b:10))).block() == 15
    }
    //end::invokeRxLocalFunction[]

    //tag::beginFunctionClient[]
    @FunctionClient
    static interface MathClient {
    //end::beginFunctionClient[]

        //tag::functionMax[]
        Long max() //<1>
        //end::functionMax[]

        //tag::functionRnd[]
        @Named("round")
        int rnd(float value)
        //end::functionRnd[]

        long sum(Sum sum)
        //tag::endFunctionClient[]
    }
    //end::endFunctionClient[]


    //tag::rxFunctionClient[]
    @FunctionClient
    static interface ReactiveMathClient {
        Publisher<Long> max()

        @Named("round")
        Publisher<Integer> rnd(float value)

        Publisher<Long> sum(Sum sum)
    }
    //end::rxFunctionClient[]
}
