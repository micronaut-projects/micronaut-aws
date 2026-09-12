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
package io.micronaut.function.aws.proxy.payload2;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import io.micronaut.context.ApplicationContext;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.function.BinaryTypeConfiguration;
import io.micronaut.http.HttpHeaders;
import io.micronaut.servlet.http.BodyBuilder;
import io.micronaut.servlet.http.ServletExchange;
import io.micronaut.servlet.http.ServletHttpHandler;
import jakarta.inject.Singleton;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation of {@link ServletHttpHandler} for input {@link com.amazonaws.services.lambda.runtime.events.ApplicationLoadBalancerRequestEvent} and response {@link com.amazonaws.services.lambda.runtime.events.ApplicationLoadBalancerResponseEvent}.
 * Uses comma separated header values instead of "multiValueHeaders". And puts cookies in "cookies"
 * instead of as "Set-Cookie" headers.
 * @see <a href="https://docs.aws.amazon.com/apigateway/latest/developerguide/http-api-develop-integrations-lambda.html#http-api-develop-integrations-lambda.proxy-format">
 *     Create AWS Lambda proxy integrations for HTTP APIs in API Gateway
 *     </a>
 */
@Internal
@Singleton
public class APIGatewayV2HTTPEventHandler extends ServletHttpHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {

    public APIGatewayV2HTTPEventHandler(ApplicationContext applicationContext) {
        super(applicationContext, applicationContext.getBean(ConversionService.class));
    }

    @Override
    protected ServletExchange<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> createExchange(
        APIGatewayV2HTTPEvent request,
        APIGatewayV2HTTPResponse response
    ) {
        translateApiGatewayV2Cookies(request);

        // If you see cookies: [...] but no headers.cookie, the V2 Payload was not translated to a standard request
        //log.info("=== RAW EVENT JSON: " + JsonMapper.createDefault().writeValueAsString(request));

        return new APIGatewayV2HTTPEventServletRequest<>(
            request,
            new APIGatewayV2HTTPResponseServletResponse<>(
                getApplicationContext().getConversionService(),
                getApplicationContext().getBean(BinaryTypeConfiguration.class)
            ),
            applicationContext.getConversionService(),
            applicationContext.getBean(BodyBuilder.class)
        );
    }

    /**
     * API Gateway v2 events have a top-level cookies array, but standard requests expect the cookies in a header
     */
    private static void translateApiGatewayV2Cookies(APIGatewayV2HTTPEvent apiGatewayV2Req) {
        // If cookies[] are present, but there is no "cookie" header, then synthesize the header.
        List<String> cookies = apiGatewayV2Req.getCookies();
        if (cookies != null && !cookies.isEmpty()) {
            final String cookieHeaderName = HttpHeaders.COOKIE.toLowerCase();

            Map<String, String> headers = apiGatewayV2Req.getHeaders();
            if (headers == null || !headers.containsKey(cookieHeaderName)) {
                if (headers == null) {
                    headers = new HashMap<>();
                    apiGatewayV2Req.setHeaders(headers);
                }
                headers.put(cookieHeaderName, String.join("; ", cookies));
            }
        }
    }
}
