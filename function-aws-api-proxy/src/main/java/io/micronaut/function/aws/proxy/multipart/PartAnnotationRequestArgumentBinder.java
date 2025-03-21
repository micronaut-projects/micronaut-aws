/*
 * Copyright 2017-2025 original authors
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
package io.micronaut.function.aws.proxy.multipart;

import io.micronaut.core.bind.annotation.AbstractArgumentBinder;
import io.micronaut.core.bind.annotation.Bindable;
import io.micronaut.core.convert.ArgumentConversionContext;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.type.Argument;
import io.micronaut.function.aws.proxy.ApiGatewayServletRequest;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.annotation.Part;
import io.micronaut.http.bind.binders.AnnotatedRequestArgumentBinder;

/**
 * Binds method parameters annotated with {@link Part} to values from multipart requests.
 * This implementation handles both regular form fields and file uploads from the
 * {@link ApiGatewayServletRequest}.
 *
 * @param <T> the type parameter
 */
public class PartAnnotationRequestArgumentBinder<T> extends AbstractArgumentBinder<T> implements AnnotatedRequestArgumentBinder<Part, T> {
    private final CompletedFileUploadBinder completedFileUploadBinder;

    public PartAnnotationRequestArgumentBinder(ConversionService conversionService, CompletedFileUploadBinder completedFileUploadBinder) {
        super(conversionService);
        this.completedFileUploadBinder = completedFileUploadBinder;
    }

    @Override
    public Class<Part> getAnnotationType() {
        return Part.class;
    }

    @Override
    public BindingResult<T> bind(ArgumentConversionContext<T> context, HttpRequest<?> source) {
        if (!(source instanceof ApiGatewayServletRequest)) {
            return BindingResult.UNSATISFIED;
        }

        ApiGatewayServletRequest<?, ?, ?> request = (ApiGatewayServletRequest<?, ?, ?>) source;

        if (completedFileUploadBinder.matches(context.getArgument().getType())) {
            return completedFileUploadBinder.bind((ArgumentConversionContext) context, request);
        }

        Argument<T> argument = context.getArgument();
        String inputName = argument.getAnnotationMetadata().stringValue(Bindable.NAME).orElse(argument.getName());

        return () -> conversionService.convert(request.getParameters().get(inputName), context);
    }
}
