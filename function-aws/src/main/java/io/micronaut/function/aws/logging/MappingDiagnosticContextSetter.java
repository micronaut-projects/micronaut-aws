/*
 * Copyright 2017-2020 original authors
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
package io.micronaut.function.aws.logging;

import com.amazonaws.services.lambda.runtime.Context;
import edu.umd.cs.findbugs.annotations.NonNull;
import io.micronaut.context.annotation.DefaultImplementation;

import javax.validation.constraints.NotNull;

/**
 * Contract to populate {@link org.slf4j.MDC} with Lambda {@link Context} and the Amazon XRay trace id.
 *
 * @see <a href="https://www.slf4j.org/manual.html#mdc>SL4J MDC</a>
 * @since 2.2.2
 * @author Sergio del Amo
 */
@DefaultImplementation(DefaultMappingDiagnosticContextSetter.class)
public interface MappingDiagnosticContextSetter {

    /**
     * Populate {@link org.slf4j.MDC} with Lambda {@link Context}.
     * @param context The Lambda execution environment context object.
     */
    void populateMappingDiagnosticContextValues(@NonNull @NotNull Context context);

    /**
     * Populate {@link org.slf4j.MDC} with Amazon XRay trace id.
     */
    void populateMappingDiagnosticContextWithXrayTraceId();
}
