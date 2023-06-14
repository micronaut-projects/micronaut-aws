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
package io.micronaut.aws.lambda.events;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.serde.annotation.Serdeable;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Port of AWS Lambda Events class which represents an ApplicationLoadBalancerResponseEvent.
 * This class adds Serdeable, nullability annotations and get rid of Lombok.
 * @see <a href="https://github.com/aws/aws-lambda-java-libs/blob/main/aws-lambda-java-events/src/main/java/com/amazonaws/services/lambda/runtime/events/ApplicationLoadBalancerResponseEvent.java">ApplicationLoadBalancerResponseEvent</a>
 * @author Sergio del Amo
 * @since 4.0.0
 */
@Serdeable
public class ApplicationLoadBalancerResponseEvent implements Serializable, Cloneable {
    private int statusCode;

    @Nullable
    private String statusDescription;

    private boolean isBase64Encoded;

    @Nullable
    private Map<String, String> headers;

    @Nullable
    private Map<String, List<String>> multiValueHeaders;

    @Nullable
    private String body;

    public ApplicationLoadBalancerResponseEvent() {
    }

    public int getStatusCode() {
        return this.statusCode;
    }

    @Nullable
    public String getStatusDescription() {
        return this.statusDescription;
    }

    public boolean getIsBase64Encoded() {
        return this.isBase64Encoded;
    }

    @Nullable
    public Map<String, String> getHeaders() {
        return this.headers;
    }

    @Nullable
    public Map<String, List<String>> getMultiValueHeaders() {
        return this.multiValueHeaders;
    }

    @Nullable
    public String getBody() {
        return this.body;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public void setStatusDescription(@Nullable String statusDescription) {
        this.statusDescription = statusDescription;
    }

    public void setIsBase64Encoded(boolean isBase64Encoded) {
        this.isBase64Encoded = isBase64Encoded;
    }

    public void setHeaders(@Nullable Map<String, String> headers) {
        this.headers = headers;
    }

    public void setMultiValueHeaders(@Nullable Map<String, List<String>> multiValueHeaders) {
        this.multiValueHeaders = multiValueHeaders;
    }

    public void setBody(@Nullable String body) {
        this.body = body;
    }

    protected boolean canEqual(Object other) {
        return other instanceof ApplicationLoadBalancerResponseEvent;
    }

    @NonNull
    public String toString() {
        return "ApplicationLoadBalancerResponseEvent(statusCode=" + this.getStatusCode() + ", statusDescription=" + this.getStatusDescription() + ", isBase64Encoded=" + this.getIsBase64Encoded() + ", headers=" + this.getHeaders() + ", multiValueHeaders=" + this.getMultiValueHeaders() + ", body=" + this.getBody() + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ApplicationLoadBalancerResponseEvent that = (ApplicationLoadBalancerResponseEvent) o;

        if (statusCode != that.statusCode) {
            return false;
        }
        if (isBase64Encoded != that.isBase64Encoded) {
            return false;
        }
        if (!Objects.equals(statusDescription, that.statusDescription)) {
            return false;
        }
        if (!Objects.equals(headers, that.headers)) {
            return false;
        }
        if (!Objects.equals(multiValueHeaders, that.multiValueHeaders)) {
            return false;
        }
        return Objects.equals(body, that.body);
    }

    @Override
    public int hashCode() {
        int result = statusCode;
        result = 31 * result + (statusDescription != null ? statusDescription.hashCode() : 0);
        result = 31 * result + (isBase64Encoded ? 1 : 0);
        result = 31 * result + (headers != null ? headers.hashCode() : 0);
        result = 31 * result + (multiValueHeaders != null ? multiValueHeaders.hashCode() : 0);
        result = 31 * result + (body != null ? body.hashCode() : 0);
        return result;
    }
}
