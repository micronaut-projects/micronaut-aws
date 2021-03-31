/*
 * Copyright 2017-2021 original authors
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
package io.micronaut.tracing.aws.annotation;

import io.micronaut.aop.Around;
import io.micronaut.context.annotation.AliasFor;
import io.micronaut.context.annotation.Type;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Method annotation traces new {@link com.amazonaws.xray.entities.Segment}.
 *
 * @author Pavol Gressa
 * @since 2.5
 */
@Documented
@Retention(RUNTIME)
@Target({ElementType.METHOD})
@Type(AwsXraySegmentInterceptor.class)
@Around
public @interface AwsXraySegment {

    @AliasFor(member = "name")
    String value() default "";

    @AliasFor(member = "value")
    String name() default "";

    String namespace() default "";
}
