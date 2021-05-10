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
package io.micronaut.function.aws;
// tag::imports[]
import io.micronaut.context.env.Environment;
import javax.inject.Inject;
// end::imports[]

// tag::class[]
public class RoundHandler extends MicronautRequestHandler<Float, Integer> { // <1>

    @Inject
    MathService mathService; // <2>

    @Inject
    Environment env;

    @Override
    public Integer execute(Float input) {
        return mathService.round(input); // <3>
    }
}
// end::class[]
