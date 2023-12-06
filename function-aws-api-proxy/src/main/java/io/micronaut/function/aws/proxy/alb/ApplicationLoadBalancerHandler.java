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

import com.amazonaws.services.lambda.runtime.events.ApplicationLoadBalancerRequestEvent;
import com.amazonaws.services.lambda.runtime.events.ApplicationLoadBalancerResponseEvent;
import io.micronaut.context.ApplicationContext;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.function.BinaryTypeConfiguration;
import io.micronaut.servlet.http.BodyBuilder;
import io.micronaut.servlet.http.ServletExchange;
import io.micronaut.servlet.http.ServletHttpHandler;

/**
 * Implementation of {@link ServletHttpHandler} for AWS Gateway Proxy Events.
 *
 * @author Sergio del Amo
 * @since 4.0.0
 */
public class ApplicationLoadBalancerHandler extends ServletHttpHandler<ApplicationLoadBalancerRequestEvent, ApplicationLoadBalancerResponseEvent> {
    public ApplicationLoadBalancerHandler(ApplicationContext applicationContext) {
        super(applicationContext, applicationContext.getBean(ConversionService.class));
    }

    @Override
    protected ServletExchange<ApplicationLoadBalancerRequestEvent, ApplicationLoadBalancerResponseEvent> createExchange(
        ApplicationLoadBalancerRequestEvent request,
        ApplicationLoadBalancerResponseEvent response
    ) {
        return new ApplicationLoadBalancerServletRequest<>(
            request,
            new ApplicationLoadBalancerServletResponse<>(
                getApplicationContext().getConversionService(),
                getApplicationContext().getBean(BinaryTypeConfiguration.class)
            ),
            applicationContext.getConversionService(),
            applicationContext.getBean(BodyBuilder.class)
        );
    }
}
