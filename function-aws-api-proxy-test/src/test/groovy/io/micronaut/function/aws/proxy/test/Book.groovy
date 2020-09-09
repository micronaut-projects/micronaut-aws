package io.micronaut.function.aws.proxy.test

import edu.umd.cs.findbugs.annotations.NonNull
import io.micronaut.core.annotation.Introspected
import javax.validation.constraints.NotBlank
import groovy.transform.CompileStatic

@CompileStatic
@Introspected
class Book {

    @NonNull
    @NotBlank
    String name
}
