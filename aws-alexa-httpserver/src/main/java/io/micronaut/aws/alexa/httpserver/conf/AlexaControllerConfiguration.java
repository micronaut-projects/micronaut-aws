/*
 * Copyright 2017-2020 original authors
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
package io.micronaut.aws.alexa.httpserver.conf;

import com.amazon.ask.model.Application;
import com.amazon.ask.model.Context;
import com.amazon.ask.model.LaunchRequest;
import com.amazon.ask.model.RequestEnvelope;
import com.amazon.ask.model.ResponseEnvelope;
import com.amazon.ask.model.Session;
import com.amazon.ask.model.User;
import com.amazon.ask.model.interfaces.system.SystemState;
import edu.umd.cs.findbugs.annotations.Nullable;
import io.micronaut.core.util.Toggleable;
import io.micronaut.serde.annotation.SerdeImport;

/**
 * Defines configuration for the Alexa controller.
 *
 * @author sdelamo
 * @since 2.0.0
 */
@SerdeImport(RequestEnvelope.class)
@SerdeImport(ResponseEnvelope.class)
@SerdeImport(Session.class)
@SerdeImport(User.class)
@SerdeImport(Application.class)
@SerdeImport(Context.class)
@SerdeImport(SystemState.class)
@SerdeImport(LaunchRequest.class)
public interface AlexaControllerConfiguration extends Toggleable {

    /**
     *
     * @return The path for the alexa endpoint
     */
    @Nullable
    String getPath();
}
