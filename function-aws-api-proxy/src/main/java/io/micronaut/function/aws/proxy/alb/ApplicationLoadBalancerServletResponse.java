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
import io.micronaut.core.convert.ConversionService;
import io.micronaut.function.aws.proxy.AbstractServletHttpResponse;
import io.micronaut.function.aws.proxy.BinaryContentConfiguration;
import io.micronaut.function.aws.proxy.MapCollapseUtils;

import java.util.Base64;

/**
 * Implementation of {@link io.micronaut.servlet.http.ServletHttpResponse} for {@link ApplicationLoadBalancerResponseEvent}.
 *
 * @param <B> The body type
 * @author Sergio del Amo
 * @since 4.0.0
 */
public class ApplicationLoadBalancerServletResponse<B> extends AbstractServletHttpResponse<ApplicationLoadBalancerResponseEvent, B> {

    protected ApplicationLoadBalancerServletResponse(ConversionService conversionService, BinaryContentConfiguration binaryContentConfiguration) {
        super(conversionService, binaryContentConfiguration);
    }

    @Override
    public ApplicationLoadBalancerResponseEvent getNativeResponse() {
        ApplicationLoadBalancerResponseEvent nativeResponse = new ApplicationLoadBalancerResponseEvent();
        nativeResponse.setHeaders(MapCollapseUtils.getSingleValueHeaders(headers));
        nativeResponse.setMultiValueHeaders(MapCollapseUtils.getMulitHeaders(headers));
        nativeResponse.setStatusCode(status);
        if (binaryContentConfiguration.isBinary(getHeaders().getContentType().orElse(null))) {
            nativeResponse.setIsBase64Encoded(true);
            nativeResponse.setBody(Base64.getMimeEncoder().encodeToString(body.toByteArray()));
        } else {
            nativeResponse.setIsBase64Encoded(false);
            nativeResponse.setBody(body.toString(getCharacterEncoding()));
        }
        return nativeResponse;
    }
}
