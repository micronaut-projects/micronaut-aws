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

import com.amazonaws.services.lambda.runtime.ClientContext;
import com.amazonaws.services.lambda.runtime.CognitoIdentity;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import io.micronaut.context.BeanContext;
import io.micronaut.core.annotation.NonNull;
import jakarta.inject.Singleton;

/**
 * Registers singletons of type {@link Context}.
 * If the Lambda Context contains non-null {@link LambdaLogger}, {@link ClientContext} and {@link CognitoIdentity} they are registered as Singleton as well.
 * @author Sergio del Amo
 * @since 3.2.2
 */
@Singleton
public class DefaultLambdaContextFactory implements LambdaContextFactory {

    private final BeanContext beanContext;

    /**
     *
     * @param beanContext Bean Context.
     */
    public DefaultLambdaContextFactory(BeanContext beanContext) {
        this.beanContext = beanContext;
    }

    @Override
    public void registerSingletons(@NonNull Context context) {
        beanContext.registerSingleton(context);
        LambdaLogger logger = context.getLogger();
        if (logger != null) {
            beanContext.registerSingleton(logger);
        }
        ClientContext clientContext = context.getClientContext();
        if (clientContext != null) {
            beanContext.registerSingleton(clientContext);
        }
        CognitoIdentity identity = context.getIdentity();
        if (identity != null) {
            beanContext.registerSingleton(identity);
        }
    }
}
