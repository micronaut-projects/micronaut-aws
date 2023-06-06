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

import com.amazonaws.services.lambda.runtime.Context;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.ApplicationContextProvider;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;

/**
 * Configures MDC and Bean Context with the current LambdaContext.
 * @author Sergio del Amo
 * @since 3.2.2
 */
public final class HandlerUtils {
    private HandlerUtils() {
    }

    /**
     * It uses {@link LambdaContextFactory} to registers Lambda Context beans as singletons in the bean context.
     * It uses {@link DiagnosticInfoPopulator} to populate the MDC context with Lambda Context values.
     *
     * @param applicationContextProvider Application Context Provider
     * @param lambdaContext Lambda Context
     */
    public static void configureWithContext(@NonNull ApplicationContextProvider applicationContextProvider,
                                            @Nullable Context lambdaContext) {
        ApplicationContext applicationContext = applicationContextProvider.getApplicationContext();
        DiagnosticInfoPopulator mdcPopulator = null;
        if (applicationContext.containsBean(DiagnosticInfoPopulator.class)) {
            mdcPopulator = applicationContext.getBean(DiagnosticInfoPopulator.class);
        }
        if (lambdaContext != null) {
            if (applicationContext.containsBean(LambdaContextFactory.class)) {
                applicationContext.getBean(LambdaContextFactory.class)
                        .registerSingletons(lambdaContext);
            }
            if (mdcPopulator != null) {
                mdcPopulator.populateMappingDiagnosticContextValues(lambdaContext);
            }
        }
        if (mdcPopulator != null) {
            mdcPopulator.populateMappingDiagnosticContextWithXrayTraceId();
        }
    }
}
