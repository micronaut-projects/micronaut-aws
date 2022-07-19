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
 * @since 3.5.2
 */
@Introspected
public class WebSocketConnection {

    @NonNull
    @NotBlank
    private final String region;

    @NonNull
    @NotBlank
    private final String apiId;

    @NonNull
    @NotBlank
    private final String stage;

    @NonNull
    @NotBlank
    private final String connectionId;

    @Nullable
    private final String domainName;

    public WebSocketConnection(@NonNull String region,
                               @NonNull String apiId,
                               @NonNull String stage,
                               @NonNull String connectionId,
                               @Nullable String domainName) {
        this.region = region;
        this.apiId = apiId;
        this.stage = stage;
        this.connectionId = connectionId;
        this.domainName = domainName;
    }

    /**
     *
     * @return the region
     */
    @NonNull
    public String getRegion() {
        return region;
    }

    /**
     *
     * @return the app id
     */
    @NonNull
    public String getApiId() {
        return apiId;
    }

    /**
     *
     * @return the stage
     */
    @NonNull
    public String getStage() {
        return stage;
    }

    /**
     *
     * @return the connection id
     */
    @NonNull
    public String getConnectionId() {
        return connectionId;
    }

    /**
     *
     * @return the domain name
     */
    @Nullable
    public String getDomainName() {
        return domainName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        WebSocketConnection that = (WebSocketConnection) o;

        if (!apiId.equals(that.apiId)) {
            return false;
        }
        if (!stage.equals(that.stage)) {
            return false;
        }
        if (!connectionId.equals(that.connectionId)) {
            return false;
        }
        return domainName != null ? domainName.equals(that.domainName) : that.domainName == null;
    }

    @Override
    public int hashCode() {
        int result = apiId.hashCode();
        result = 31 * result + stage.hashCode();
        result = 31 * result + connectionId.hashCode();
        result = 31 * result + (domainName != null ? domainName.hashCode() : 0);
        return result;
    }
}
