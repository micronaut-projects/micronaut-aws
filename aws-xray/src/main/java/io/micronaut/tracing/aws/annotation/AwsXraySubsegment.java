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
 * Method annotation traces new {@link com.amazonaws.xray.entities.Subsegment}.
 *
 * @author Pavol Gressa
 * @since 2.5
 */
@Documented
@Retention(RUNTIME)
@Target({ElementType.METHOD})
@Type(AwsXraySegmentInterceptor.class)
@Around
public @interface AwsXraySubsegment {

    @AliasFor(member = "name")
    String value() default "";

    @AliasFor(member = "value")
    String name() default "";
}
