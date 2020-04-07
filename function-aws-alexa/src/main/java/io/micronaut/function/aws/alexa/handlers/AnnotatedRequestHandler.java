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
package io.micronaut.function.aws.alexa.handlers;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.RequestHandler;
import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.AnnotationMetadataProvider;
import io.micronaut.core.reflect.ClassUtils;
import io.micronaut.core.reflect.InstantiationUtils;
import io.micronaut.core.util.StringUtils;
import io.micronaut.function.aws.alexa.annotation.IntentHandler;

import java.util.Arrays;
import java.util.Optional;

import static com.amazon.ask.request.Predicates.intentName;

/**
 * Interface used by {@link IntentHandler} to implement requests.
 *
 * @author graemerocher
 * @since 1.1
 */
@FunctionalInterface
public interface AnnotatedRequestHandler extends RequestHandler {

    @Override
    default boolean canHandle(HandlerInput handlerInput) {
        final Class<? extends AnnotatedRequestHandler> type = getClass();
        final String annotationMetadata = type.getPackage().getName() + ".$" + type.getSimpleName() + "DefinitionClass";
        final AnnotationMetadata metadata = ClassUtils.forName(annotationMetadata, type.getClassLoader()).flatMap(aClass -> {
            final Object o = InstantiationUtils.tryInstantiate(aClass).orElse(null);
            if (o instanceof AnnotationMetadataProvider) {
                return Optional.of(((AnnotationMetadataProvider) o).getAnnotationMetadata());
            }
            return Optional.empty();
        }).orElse(AnnotationMetadata.EMPTY_METADATA);
        final String[] names = metadata.getValue(IntentHandler.class, String[].class).orElse(StringUtils.EMPTY_STRING_ARRAY);
        return Arrays.stream(names).anyMatch(n -> handlerInput.matches(intentName(n)));
    }
}
