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
package io.micronaut.function.aws.proxy.model.bind;

import com.amazonaws.serverless.proxy.model.AwsProxyRequest;
import io.micronaut.core.convert.ArgumentConversionContext;
import io.micronaut.core.type.Argument;
import io.micronaut.function.aws.proxy.MicronautAwsProxyRequest;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.bind.binders.TypedRequestArgumentBinder;

import jakarta.inject.Singleton;
import java.util.Optional;

/**
 * Allows binding the {@link AwsProxyRequest} object to a method argument.
 *
 * @author graemerocher
 * @since 1.1
 */
@Singleton
public class AwsProxyRequestArgumentBinder implements TypedRequestArgumentBinder<AwsProxyRequest> {
    @Override
    public Argument<AwsProxyRequest> argumentType() {
        return Argument.of(AwsProxyRequest.class);
    }

    @Override
    public BindingResult<AwsProxyRequest> bind(ArgumentConversionContext<AwsProxyRequest> context, HttpRequest<?> source) {
        if (source instanceof MicronautAwsProxyRequest) {
            final AwsProxyRequest awsProxyRequest = ((MicronautAwsProxyRequest<?>) source).getAwsProxyRequest();
            return () -> Optional.ofNullable(awsProxyRequest);
        }
        return BindingResult.UNSATISFIED;
    }
}
