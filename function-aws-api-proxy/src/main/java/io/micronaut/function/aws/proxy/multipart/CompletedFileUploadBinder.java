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

import io.micronaut.core.annotation.Internal;
import io.micronaut.core.bind.annotation.Bindable;
import io.micronaut.core.convert.ArgumentConversionContext;
import io.micronaut.core.type.Argument;
import io.micronaut.function.aws.proxy.ApiGatewayServletRequest;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.bind.binders.TypedRequestArgumentBinder;
import io.micronaut.http.multipart.CompletedFileUpload;

import java.util.Optional;

/**
 * Binds {@link CompletedFileUpload} arguments from multipart requests in the AWS API Gateway context.
 */
@Internal
public class CompletedFileUploadBinder implements TypedRequestArgumentBinder<CompletedFileUpload> {

    static final Argument<CompletedFileUpload> TYPE = Argument.of(CompletedFileUpload.class);

    @Override
    public Argument<CompletedFileUpload> argumentType() {
        return TYPE;
    }

    @Override
    public BindingResult<CompletedFileUpload> bind(
            ArgumentConversionContext<CompletedFileUpload> context,
            HttpRequest<?> source) {

        if (!(source instanceof ApiGatewayServletRequest)) {
            return BindingResult.UNSATISFIED;
        }

        ApiGatewayServletRequest<?, ?, ?> request = (ApiGatewayServletRequest<?, ?, ?>) source;

        // Get the parameter name to bind
        String paramName = context.getArgument()
                .getAnnotationMetadata()
                .stringValue(Bindable.class)
                .orElse(context.getArgument().getName());

        // Try to find a file upload with the matching name
        final CompletedFileUpload fileUpload = request.getFileUploads().get(paramName);
        if (fileUpload != null) {
            return () -> Optional.of(fileUpload);
        }

        return BindingResult.UNSATISFIED;
    }
}
