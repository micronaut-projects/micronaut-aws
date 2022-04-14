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
package io.micronaut.aws.lambda.events.serde;

import com.amazonaws.services.lambda.runtime.events.CloudFrontEvent;
import com.amazonaws.services.lambda.runtime.serialization.events.mixins.CloudFrontEventMixin;
import io.micronaut.serde.annotation.SerdeImport;

/**
 * {@link SerdeImport} for {@link CloudFrontEvent}.
 *
 * @author Sergio del Amo
 * @since 1.0.0
 */
@SerdeImport(value = CloudFrontEvent.Header.class)
@SerdeImport(value = CloudFrontEvent.Config.class)
@SerdeImport(value = CloudFrontEvent.Response.class)
@SerdeImport(value = CloudFrontEvent.Request.class)
@SerdeImport(value = CloudFrontEvent.CF.class)
@SerdeImport(value = CloudFrontEvent.Record.class)
@SerdeImport(value = CloudFrontEvent.class, mixin = CloudFrontEventMixin.class)
public class CloudFrontEventSerde {
}
