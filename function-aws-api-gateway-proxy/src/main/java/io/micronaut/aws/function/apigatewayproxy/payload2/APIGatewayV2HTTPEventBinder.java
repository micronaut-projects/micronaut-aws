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
package io.micronaut.aws.function.apigatewayproxy.payload2;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.convert.ArgumentConversionContext;
import io.micronaut.core.type.Argument;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.bind.binders.TypedRequestArgumentBinder;

import java.util.Optional;

/**
 * Request binder for the APIGatewayProxyRequestEvent object.
 *
 * @author Tim Yates
 * @since 4.0.0
 */
@Internal
public class APIGatewayV2HTTPEventBinder implements TypedRequestArgumentBinder<APIGatewayV2HTTPEvent> {

    private static final Argument<APIGatewayV2HTTPEvent> TYPE = Argument.of(APIGatewayV2HTTPEvent.class);

    @Override
    public Argument<APIGatewayV2HTTPEvent> argumentType() {
        return TYPE;
    }

    @Override
    public BindingResult<APIGatewayV2HTTPEvent> bind(
        ArgumentConversionContext<APIGatewayV2HTTPEvent> context,
        HttpRequest<?> source
    ) {
        if (source instanceof APIGatewayV2HTTPEventServletRequest<?> req) {
            return () -> Optional.of(req.getNativeRequest());
        }
        return BindingResult.UNSATISFIED;
    }
}
