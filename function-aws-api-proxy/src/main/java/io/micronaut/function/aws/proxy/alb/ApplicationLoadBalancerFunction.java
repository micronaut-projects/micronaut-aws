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

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import io.micronaut.aws.lambda.events.ApplicationLoadBalancerRequestEvent;
import io.micronaut.aws.lambda.events.ApplicationLoadBalancerResponseEvent;
import io.micronaut.context.ApplicationContext;
import io.micronaut.function.aws.HandlerUtils;
import io.micronaut.function.executor.FunctionInitializer;
import io.micronaut.servlet.http.ServletHttpHandler;

/**
 * {@link RequestHandler} for input {@link ApplicationLoadBalancerRequestEvent} and response {@link ApplicationLoadBalancerResponseEvent}.
 * @since 4.0.0
 * @author Sergio del Amo
 */
public class ApplicationLoadBalancerFunction extends FunctionInitializer implements
    RequestHandler<ApplicationLoadBalancerRequestEvent, ApplicationLoadBalancerResponseEvent> {
    private final ServletHttpHandler<ApplicationLoadBalancerRequestEvent, ApplicationLoadBalancerResponseEvent> httpHandler;

    public ApplicationLoadBalancerFunction() {
        httpHandler = initializeHandler();
    }

    public ApplicationLoadBalancerFunction(ApplicationContext ctx) {
        super(ctx);
        startThis(applicationContext);
        httpHandler = initializeHandler();
    }

    private ServletHttpHandler<ApplicationLoadBalancerRequestEvent, ApplicationLoadBalancerResponseEvent> initializeHandler() {
        ApplicationLoadBalancerHandler applicationLoadBalancerHandler = new ApplicationLoadBalancerHandler(applicationContext);
        Runtime.getRuntime().addShutdownHook(
            new Thread(applicationLoadBalancerHandler::close)
        );
        return applicationLoadBalancerHandler;
    }

    @Override
    public ApplicationLoadBalancerResponseEvent handleRequest(ApplicationLoadBalancerRequestEvent input, Context context) {
        HandlerUtils.configureWithContext(this, context);
        return httpHandler.exchange(input, new ApplicationLoadBalancerResponseEvent()).getResponse().getNativeResponse();
    }
}
