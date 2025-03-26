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

import com.sun.net.httpserver.HttpHandler;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.ApplicationContextBuilder;
import io.micronaut.context.ApplicationContextProvider;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.env.PropertySource;
import io.micronaut.context.exceptions.ConfigurationException;
import io.micronaut.core.annotation.Experimental;
import io.micronaut.core.annotation.Internal;
import io.micronaut.function.aws.proxy.payload2.APIGatewayV2HTTPEventFunction;
import jakarta.inject.Named;
import jakarta.inject.Singleton;

@Experimental
@Internal
@Factory
class EmbeddedServerFactory {

    @Named("HttpServer")
    @Singleton
    ApplicationContextProvider httpServerApplicationContextProvider(ApplicationContext applicationContext) {
        ApplicationContextBuilder builder = ApplicationContext.builder();
        for (PropertySource propertySource : applicationContext.getEnvironment().getPropertySources()) {
            builder = builder.propertySources(propertySource);
        }
        return new APIGatewayV2HTTPEventFunction(builder.build());
    }

    @Singleton
    HttpHandler createHandler(@Named("HttpServer") ApplicationContextProvider applicationContextProvider) {
        if (applicationContextProvider instanceof APIGatewayV2HTTPEventFunction function) {
            return new AwsProxyHttpHandler(function);
        }
        throw new ConfigurationException("ApplicationContextProvider with name qualifier HttpServer should be of type APIGatewayV2HTTPEventFunction");
    }
}
