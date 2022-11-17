/*
 * Copyright 2017-2022 original authors
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

import io.micronaut.context.annotation.DefaultImplementation;
import io.micronaut.core.annotation.NonNull;
import com.amazonaws.services.lambda.runtime.Context;

/**
 * Populates Mapping Diagnostic Context with Lambda Context.
 * @author Sergio del Amo
 * @since 3.2.0
 */
@DefaultImplementation(DefaultDiagnosticInfoPopulator.class)
public interface DiagnosticInfoPopulator {
    /**
     * Populate MDC with Lambda Context values.
     * @param context Lambda Context
     */
    void populateMappingDiagnosticContextValues(@NonNull Context context);

    /**
     * Populate MDC with XRay Trace ID if it is able to parse it.
     */
    void populateMappingDiagnosticContextWithXrayTraceId();
}
