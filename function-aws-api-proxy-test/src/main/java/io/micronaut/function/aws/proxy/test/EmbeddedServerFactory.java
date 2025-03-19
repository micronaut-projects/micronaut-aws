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
package io.micronaut.function.aws.proxy.test;

import io.micronaut.context.ApplicationContext;
import io.micronaut.context.ApplicationContextBuilder;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.env.PropertySource;
import io.micronaut.core.annotation.Internal;
import io.micronaut.function.aws.proxy.payload2.APIGatewayV2HTTPEventFunction;
import io.micronaut.http.server.HttpServerConfiguration;
import io.micronaut.runtime.server.EmbeddedServer;
import jakarta.inject.Singleton;

@Internal
@Factory
class EmbeddedServerFactory {

    @Singleton
    HttpHandlerApplicationContextAware httpHandlerApplicationContextAware(ApplicationContext applicationContext) {
        APIGatewayV2HTTPEventFunction function = createLambdaHandler(applicationContext);
        return new AwsProxyHttpHandler(function);
    }

    @Singleton
    EmbeddedServer createServer(HttpServerConfiguration httpServerConfiguration,
                                HttpHandlerApplicationContextAware httpHandlerApplicationContextAware) {
        return new HttpServerEmbeddedServer(httpHandlerApplicationContextAware, httpServerConfiguration);
    }

    private static APIGatewayV2HTTPEventFunction createLambdaHandler(ApplicationContext ctx) {
        ApplicationContextBuilder builder = ApplicationContext.builder();
        for (PropertySource propertySource : ctx.getEnvironment().getPropertySources()) {
            builder = builder.propertySources(propertySource);
        }
        return new APIGatewayV2HTTPEventFunction(builder.build());
    }
}
