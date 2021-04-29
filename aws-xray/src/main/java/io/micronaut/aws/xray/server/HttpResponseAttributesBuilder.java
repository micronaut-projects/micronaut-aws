/*
 * Copyright 2017-2021 original authors
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
package io.micronaut.aws.xray.server;

import com.amazonaws.xray.entities.Entity;
import io.micronaut.context.annotation.DefaultImplementation;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.http.HttpResponse;
/**
 * @author Sergio del Amo
 * @since 2.7.0
 */
@DefaultImplementation(DefaultHttpResponseAttributesBuilder.class)
@FunctionalInterface
public interface HttpResponseAttributesBuilder {

    /**
     *
     * @param entity The X-Ray Entity (E.g. a Segment or Subsegment)
     * @param response The HTTP Response from which some information will be extracted
     */
    void putHttpResponseInformation(@NonNull Entity entity, @NonNull HttpResponse<?> response);
}
