/*
 * Copyright 2017-2020 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micronaut.function.aws.proxy.model.bind;

import com.amazonaws.serverless.proxy.RequestReader;
import com.amazonaws.services.lambda.runtime.Context;
import io.micronaut.core.convert.ArgumentConversionContext;
import io.micronaut.core.type.Argument;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.bind.binders.TypedRequestArgumentBinder;

import javax.inject.Singleton;

/**
 * Allows binding the {@link com.amazonaws.serverless.proxy.model.AwsProxyRequest} object to a method argument.
 *
 * @author graemerocher
 * @since 1.1
 */
@Singleton
public class ContextArgumentBinder implements TypedRequestArgumentBinder<Context> {
    @Override
    public Argument<Context> argumentType() {
        return Argument.of(Context.class);
    }

    @Override
    public BindingResult<Context> bind(ArgumentConversionContext<Context> context, HttpRequest<?> source) {
       return () -> source.getAttribute(RequestReader.LAMBDA_CONTEXT_PROPERTY).map(Context.class::cast);
    }
}
