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
 * Port of AWS Lambda Events class which represents an APIGatewayProxyResponseEvent.
 * This class adds Serdeable, nullability annotations and @Creator annotations to it.
 * @see <a href="https://github.com/aws/aws-lambda-java-libs/blob/main/aws-lambda-java-events/src/main/java/com/amazonaws/services/lambda/runtime/events/APIGatewayProxyResponseEvent.java">APIGatewayProxyResponseEvent</a>
 * @author Sergio del Amo
 * @since 4.0.0
 */
@Serdeable
public class APIGatewayProxyResponseEvent implements Serializable, Cloneable {

    private static final long serialVersionUID = 2263167344670024172L;

    @Nullable
    private Integer statusCode;

    @Nullable
    private Map<String, String> headers;

    @Nullable
    private Map<String, List<String>> multiValueHeaders;

    @Nullable
    private String body;

    @Nullable
    private Boolean isBase64Encoded;

    /**
     * @return The HTTP status code for the request
     */
    @Nullable
    public Integer getStatusCode() {
        return statusCode;
    }

    /**
     * @param statusCode The HTTP status code for the request
     */
    public void setStatusCode(@Nullable Integer statusCode) {
        this.statusCode = statusCode;
    }

    /**
     * @param statusCode The HTTP status code for the request
     * @return APIGatewayProxyResponseEvent object
     */
    @NonNull
    public APIGatewayProxyResponseEvent withStatusCode(@Nullable Integer statusCode) {
        this.setStatusCode(statusCode);
        return this;
    }

    /**
     * @return The Http headers return in the response
     */
    @Nullable
    public Map<String, String> getHeaders() {
        return headers;
    }

    /**
     * @param headers The Http headers return in the response
     */
    public void setHeaders(@Nullable Map<String, String> headers) {
        this.headers = headers;
    }

    /**
     * @param headers The Http headers return in the response
     * @return APIGatewayProxyResponseEvent
     */
    @NonNull
    public APIGatewayProxyResponseEvent withHeaders(@Nullable Map<String, String> headers) {
        this.setHeaders(headers);
        return this;
    }

    /**
     * @return the Http multi value headers to return in the response
     */
    @Nullable
    public Map<String, List<String>> getMultiValueHeaders() {
        return multiValueHeaders;
    }

    /**
     * @param multiValueHeaders the Http multi value headers to return in the response
     */
    public void setMultiValueHeaders(@Nullable Map<String, List<String>> multiValueHeaders) {
        this.multiValueHeaders = multiValueHeaders;
    }

    /**
     *
     * @param multiValueHeaders the Http multi value headers to return in the response
     * @return APIGatewayProxyResponseEvent
     */
    @NonNull
    public APIGatewayProxyResponseEvent withMultiValueHeaders(@Nullable Map<String, List<String>> multiValueHeaders) {
        this.setMultiValueHeaders(multiValueHeaders);
        return this;
    }

    /**
     * @return The response body
     */
    @NonNull
    public String getBody() {
        return body;
    }

    /**
     * @param body The response body
     */
    public void setBody(@Nullable String body) {
        this.body = body;
    }

    /**
     * @param body The response body
     * @return APIGatewayProxyResponseEvent object
     */
    @NonNull
    public APIGatewayProxyResponseEvent withBody(@Nullable String body) {
        this.setBody(body);
        return this;
    }

    /**
     * @return whether the body String is base64 encoded.
     */
    @Nullable
    public Boolean getIsBase64Encoded() {
        return this.isBase64Encoded;
    }

    /**
     * @param isBase64Encoded Whether the body String is base64 encoded
     */
    public void setIsBase64Encoded(@Nullable Boolean isBase64Encoded) {
        this.isBase64Encoded = isBase64Encoded;
    }

    /**
     * @param isBase64Encoded Whether the body String is base64 encoded
     * @return APIGatewayProxyRequestEvent
     */
    @NonNull
    public APIGatewayProxyResponseEvent withIsBase64Encoded(@Nullable Boolean isBase64Encoded) {
        this.setIsBase64Encoded(isBase64Encoded);
        return this;
    }

    /**
     * Returns a string representation of this object; useful for testing and debugging.
     *
     * @return A string representation of this object.
     *
     * @see Object#toString()
     */
    @Override
    @NonNull
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        if (getStatusCode() != null)
            sb.append("statusCode: ").append(getStatusCode()).append(",");
        if (getHeaders() != null)
            sb.append("headers: ").append(getHeaders().toString()).append(",");
        if (getMultiValueHeaders() != null)
            sb.append("multiValueHeaders: ").append(getMultiValueHeaders().toString()).append(",");
        if (getBody() != null)
            sb.append("body: ").append(getBody());
        sb.append("}");
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        APIGatewayProxyResponseEvent that = (APIGatewayProxyResponseEvent) o;

        if (!Objects.equals(statusCode, that.statusCode)) {
            return false;
        }
        if (!Objects.equals(headers, that.headers)) {
            return false;
        }
        if (!Objects.equals(multiValueHeaders, that.multiValueHeaders)) {
            return false;
        }
        if (!Objects.equals(body, that.body)) {
            return false;
        }
        return Objects.equals(isBase64Encoded, that.isBase64Encoded);
    }

    @Override
    public int hashCode() {
        int result = statusCode != null ? statusCode.hashCode() : 0;
        result = 31 * result + (headers != null ? headers.hashCode() : 0);
        result = 31 * result + (multiValueHeaders != null ? multiValueHeaders.hashCode() : 0);
        result = 31 * result + (body != null ? body.hashCode() : 0);
        result = 31 * result + (isBase64Encoded != null ? isBase64Encoded.hashCode() : 0);
        return result;
    }

    @Override
    @NonNull
    public APIGatewayProxyResponseEvent clone() {
        try {
            return (APIGatewayProxyResponseEvent) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new IllegalStateException("Got a CloneNotSupportedException from Object.clone()", e);
        }
    }

}
