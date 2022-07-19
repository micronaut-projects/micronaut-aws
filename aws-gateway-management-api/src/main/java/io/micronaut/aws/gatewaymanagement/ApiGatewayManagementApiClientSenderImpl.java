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
import jakarta.inject.Singleton;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.apigatewaymanagementapi.ApiGatewayManagementApiClient;
import software.amazon.awssdk.services.apigatewaymanagementapi.ApiGatewayManagementApiClientBuilder;
import software.amazon.awssdk.services.apigatewaymanagementapi.model.PostToConnectionRequest;
import software.amazon.awssdk.services.apigatewaymanagementapi.model.PostToConnectionResponse;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author Sergio del Amo
 * @since 3.5.2
 */
@Singleton
public class ApiGatewayManagementApiClientSenderImpl implements ApiGatewayManagementApiClientSender {

    private final Map<WebSocketConnection, ApiGatewayManagementApiClient> clients = new ConcurrentHashMap<>();

    private final ApiGatewayManagementApiClientBuilder builder;

    public ApiGatewayManagementApiClientSenderImpl(ApiGatewayManagementApiClientBuilder builder) {
        this.builder = builder;
    }

    @Override
    @NonNull
    public PostToConnectionResponse send(@NonNull @NotNull @Valid WebSocketMessage webSocketMessage) {
        return send(webSocketMessage, SdkBytes.fromString(webSocketMessage.getMessage(), StandardCharsets.UTF_8));
    }

    @Override
    @NonNull
    public PostToConnectionResponse send(@NonNull @NotNull @Valid WebSocketConnection connection,
                                         @NonNull SdkBytes sdkBytes) {
        return clients.computeIfAbsent(connection, webSocketConnection -> builder
                        .endpointOverride(WebSocketConnectionUtils.uriOf(webSocketConnection))
                        .build())
                .postToConnection(PostToConnectionRequest.builder()
                        .data(sdkBytes)
                        .connectionId(connection.getConnectionId())
                        .build());
    }
}
