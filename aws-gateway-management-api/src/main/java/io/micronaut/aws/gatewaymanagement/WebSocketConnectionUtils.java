/*
 * Copyright 2017-2022 original authors
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
package io.micronaut.aws.gatewaymanagement;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.http.uri.UriBuilder;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.net.URI;

/**
 *
 * @author Sergio del Amo
 * @since 3.5.2
 */
public final class WebSocketConnectionUtils {
    private WebSocketConnectionUtils() {

    }

    @NonNull
    public static URI uriOf(@NonNull @NotNull @Valid WebSocketConnection webSocketConnection) {
        return UriBuilder.of("https://" + webSocketConnection.getApiId() + ".execute-api.us-east-1.amazonaws.com")
                .path(webSocketConnection.getStage())
                .build();
    }
}
