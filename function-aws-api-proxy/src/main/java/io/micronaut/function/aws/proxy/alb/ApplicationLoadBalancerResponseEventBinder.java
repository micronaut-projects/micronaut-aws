/*
 * Copyright 2017-2023 original authors
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
package io.micronaut.function.aws.proxy.alb;

import com.amazonaws.services.lambda.runtime.events.ApplicationLoadBalancerResponseEvent;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.convert.ArgumentConversionContext;
import io.micronaut.core.type.Argument;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.bind.binders.TypedRequestArgumentBinder;
import jakarta.inject.Singleton;

import java.util.Optional;

/**
 * Request binder for the {@link ApplicationLoadBalancerResponseEvent} object.
 *
 * @author Sergio del Amo
 * @since 4.0.0
 */
@Internal
@Singleton
public class ApplicationLoadBalancerResponseEventBinder implements TypedRequestArgumentBinder<ApplicationLoadBalancerResponseEvent> {

    static final Argument<ApplicationLoadBalancerResponseEvent> TYPE = Argument.of(ApplicationLoadBalancerResponseEvent.class);

    @Override
    public Argument<ApplicationLoadBalancerResponseEvent> argumentType() {
        return TYPE;
    }

    @Override
    public BindingResult<ApplicationLoadBalancerResponseEvent> bind(
        ArgumentConversionContext<ApplicationLoadBalancerResponseEvent> context,
        HttpRequest<?> source
    ) {
        if (source instanceof ApplicationLoadBalancerServletRequest<?> req) {
            return () -> Optional.of(req.getResponse().getNativeResponse());
        }
        return BindingResult.UNSATISFIED;
    }
}
