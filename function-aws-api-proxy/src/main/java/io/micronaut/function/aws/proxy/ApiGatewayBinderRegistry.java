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
package io.micronaut.function.aws.proxy;

import io.micronaut.context.annotation.Replaces;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.http.bind.DefaultRequestBinderRegistry;
import io.micronaut.http.bind.binders.DefaultBodyAnnotationBinder;
import io.micronaut.http.bind.binders.RequestArgumentBinder;
import io.micronaut.http.codec.MediaTypeCodecRegistry;
import io.micronaut.servlet.http.ServletBinderRegistry;
import jakarta.inject.Singleton;

import java.util.List;

@Singleton
@Internal
@Replaces(DefaultRequestBinderRegistry.class)
class ApiGatewayBinderRegistry<T> extends ServletBinderRegistry<T> {

    ApiGatewayBinderRegistry(
        MediaTypeCodecRegistry mediaTypeCodecRegistry,
        ConversionService conversionService,
        List<RequestArgumentBinder> binders,
        DefaultBodyAnnotationBinder<T> defaultBodyAnnotationBinder
    ) {
        super(mediaTypeCodecRegistry, conversionService, binders, defaultBodyAnnotationBinder);
    }
}
