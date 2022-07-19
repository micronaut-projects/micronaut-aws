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

import io.micronaut.core.annotation.Introspected;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;

import javax.validation.constraints.NotBlank;

/**
 *
 * @author Sergio del Amo
 * @since 2.8.0
 */
@Introspected
public class WebSocketMessage extends WebSocketConnection {
    @NonNull
    @NotBlank
    private final String message;

    public WebSocketMessage(@NonNull WebSocketConnection webSocketConnection,
                            @NonNull String message) {
        this(webSocketConnection.getRegion(),
                webSocketConnection.getApiId(),
                webSocketConnection.getStage(),
                webSocketConnection.getConnectionId(),
                webSocketConnection.getDomainName(),
                message);
    }

    public WebSocketMessage(@NonNull String region,
                            @NonNull String apiId,
                            @NonNull String stage,
                            @NonNull String connectionId,
                            @Nullable String domainName,
                            @NonNull String message) {
        super(region, apiId, stage, connectionId, domainName);
        this.message = message;
    }

    /**
     *
     * @return the web socket message
     */
    @NonNull
    public String getMessage() {
        return message;
    }
}
