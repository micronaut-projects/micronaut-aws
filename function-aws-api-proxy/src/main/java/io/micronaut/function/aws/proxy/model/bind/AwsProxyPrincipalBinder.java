/*
 * Copyright 2017-2019 original authors
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

import io.micronaut.context.annotation.Requires;
import io.micronaut.core.convert.ArgumentConversionContext;
import io.micronaut.core.type.Argument;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.bind.binders.TypedRequestArgumentBinder;
import io.micronaut.security.authentication.PrincipalArgumentBinder;

import javax.inject.Singleton;
import java.security.Principal;

/**
 * Binds the {@link Principal} if Micronaut security is not present.
 *
 * @author graemerocher
 * @since 1.1
 */
@Requires(missingBeans = PrincipalArgumentBinder.class)
@Singleton
public class AwsProxyPrincipalBinder implements TypedRequestArgumentBinder<Principal> {

    @Override
    public Argument<Principal> argumentType() {
        return Argument.of(Principal.class);
    }

    @Override
    public BindingResult<Principal> bind(ArgumentConversionContext<Principal> context, HttpRequest<?> source) {
        return source::getUserPrincipal;
    }
}
