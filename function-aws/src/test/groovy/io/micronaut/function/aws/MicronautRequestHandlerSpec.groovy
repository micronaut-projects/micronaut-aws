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
package io.micronaut.function.aws

import com.amazonaws.services.lambda.runtime.Context
import groovy.transform.Canonical
import io.micronaut.context.env.Environment
import spock.lang.Specification

import jakarta.inject.Inject
import jakarta.inject.Singleton

/**
 * @author Graeme Rocher
 * @since 1.0
 */
class MicronautRequestHandlerSpec extends Specification {

    void "test micronaut request handler"() {
        given:
        System.setProperty(Environment.ENVIRONMENTS_PROPERTY, "foo")
        expect:
        new RoundHandler().handleRequest(1.6f, Mock(Context)) == 2
        cleanup:
        System.setProperty(Environment.ENVIRONMENTS_PROPERTY, "")
    }

    void "test micronaut request handler conversion from Map"() {
        expect:
        new PointHandler().handleRequest([x:10, y:20], Mock(Context)) == new Point(10,20)
    }


    @Singleton
    static class MathService {
        Integer round(Float input) {
            return Math.round(input)
        }
    }

    static class RoundHandler extends MicronautRequestHandler<Float, Integer> {

        @Inject MathService mathService
        @Inject Environment env

        @Override
        Integer execute(Float input) {
            assert env.activeNames.contains(Environment.FUNCTION)
            assert env.activeNames.contains("foo")
            return mathService.round(input)
        }
    }

    static class PointHandler extends MicronautRequestHandler<Point, Point> {

        @Override
        Point execute(Point input) {
            input
        }
    }

    @Canonical
    static class Point {
        Integer x,y
    }
}
