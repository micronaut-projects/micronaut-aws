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
package io.micronaut.aws.lambda.events.serde;

import com.amazonaws.services.lambda.runtime.events.ConnectEvent;
import com.amazonaws.services.lambda.runtime.serialization.events.mixins.ConnectEventMixin;
import io.micronaut.serde.annotation.SerdeImport;

/**
 * {@link SerdeImport} for {@link ConnectEvent}.
 *
 * @author Dan Hollingsworth
 * @since 4.0.0
 */
@SerdeImport(value = ConnectEvent.ContactData.class, mixin = ConnectEventMixin.ContactDataMixin.class)
@SerdeImport(value = ConnectEvent.CustomerEndpoint.class, mixin = ConnectEventMixin.CustomerEndpointMixin.class)
@SerdeImport(value = ConnectEvent.Details.class, mixin = ConnectEventMixin.DetailsMixin.class)
@SerdeImport(value = ConnectEvent.SystemEndpoint.class, mixin = ConnectEventMixin.SystemEndpointMixin.class)
@SerdeImport(value = ConnectEvent.class, mixin = ConnectEventMixin.class)
public class ConnectEventSerde {

}
