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

import io.micronaut.core.annotation.Creator;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.serde.annotation.Serdeable;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Port of AWS Lambda Events class which represents an APIGatewayV2HTTPResponse.
 * This class adds Serdeable, nullability annotations, @Creator annotations and removes Lombok.
 * @see <a href="https://github.com/aws/aws-lambda-java-libs/blob/main/aws-lambda-java-events/src/main/java/com/amazonaws/services/lambda/runtime/events/APIGatewayV2HTTPResponse.java">APIGatewayV2HTTPResponse</a>
 * @author Sergio del Amo
 * @since 4.0.0
 */
@SuppressWarnings({
    "checkstyle:MissingJavadocType",
    "checkstyle:DesignForExtension"
})
@Serdeable
public class APIGatewayV2HTTPResponse {
    private int statusCode;
    @Nullable
    private Map<String, String> headers;

    @Nullable
    private Map<String, List<String>> multiValueHeaders;

    @Nullable
    private List<String> cookies;

    @Nullable
    private String body;

    private boolean isBase64Encoded;

    @Creator
    public APIGatewayV2HTTPResponse(int statusCode,
                                    @Nullable Map<String, String> headers,
                                    @Nullable Map<String, List<String>> multiValueHeaders,
                                    @Nullable List<String> cookies,
                                    @Nullable String body,
                                    boolean isBase64Encoded) {
        this.statusCode = statusCode;
        this.headers = headers;
        this.multiValueHeaders = multiValueHeaders;
        this.cookies = cookies;
        this.body = body;
        this.isBase64Encoded = isBase64Encoded;
    }

    public APIGatewayV2HTTPResponse() {
    }

    public static APIGatewayV2HTTPResponseBuilder builder() {
        return new APIGatewayV2HTTPResponseBuilder();
    }

    public int getStatusCode() {
        return this.statusCode;
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
    public List<String> getCookies() {
        return this.cookies;
    }

    @Nullable
    public String getBody() {
        return this.body;
    }

    public boolean getIsBase64Encoded() {
        return this.isBase64Encoded;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public void setHeaders(@Nullable Map<String, String> headers) {
        this.headers = headers;
    }

    public void setMultiValueHeaders(@Nullable Map<String, List<String>> multiValueHeaders) {
        this.multiValueHeaders = multiValueHeaders;
    }

    public void setCookies(@Nullable List<String> cookies) {
        this.cookies = cookies;
    }

    public void setBody(@Nullable String body) {
        this.body = body;
    }

    public void setIsBase64Encoded(boolean isBase64Encoded) {
        this.isBase64Encoded = isBase64Encoded;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        APIGatewayV2HTTPResponse that = (APIGatewayV2HTTPResponse) o;

        if (statusCode != that.statusCode) {
            return false;
        }
        if (isBase64Encoded != that.isBase64Encoded) {
            return false;
        }
        if (!Objects.equals(headers, that.headers)) {
            return false;
        }
        if (!Objects.equals(multiValueHeaders, that.multiValueHeaders)) {
            return false;
        }
        if (!Objects.equals(cookies, that.cookies)) {
            return false;
        }
        return Objects.equals(body, that.body);
    }

    @Override
    public int hashCode() {
        int result = statusCode;
        result = 31 * result + (headers != null ? headers.hashCode() : 0);
        result = 31 * result + (multiValueHeaders != null ? multiValueHeaders.hashCode() : 0);
        result = 31 * result + (cookies != null ? cookies.hashCode() : 0);
        result = 31 * result + (body != null ? body.hashCode() : 0);
        result = 31 * result + (isBase64Encoded ? 1 : 0);
        return result;
    }

    protected boolean canEqual(Object other) {
        return other instanceof APIGatewayV2HTTPResponse;
    }

    @NonNull
    public String toString() {
        return "APIGatewayV2HTTPResponse(statusCode=" + this.getStatusCode() + ", headers=" + this.getHeaders() + ", multiValueHeaders=" + this.getMultiValueHeaders() + ", cookies=" + this.getCookies() + ", body=" + this.getBody() + ", isBase64Encoded=" + this.getIsBase64Encoded() + ")";
    }

    public static class APIGatewayV2HTTPResponseBuilder {
        private int statusCode;
        @Nullable
        private Map<String, String> headers;

        @Nullable
        private Map<String, List<String>> multiValueHeaders;

        @Nullable
        private List<String> cookies;

        @Nullable
        private String body;

        @Nullable
        private boolean isBase64Encoded;

        APIGatewayV2HTTPResponseBuilder() {
        }

        @NonNull
        public APIGatewayV2HTTPResponseBuilder withStatusCode(int statusCode) {
            this.statusCode = statusCode;
            return this;
        }

        @NonNull
        public APIGatewayV2HTTPResponseBuilder withHeaders(@Nullable Map<String, String> headers) {
            this.headers = headers;
            return this;
        }

        @NonNull
        public APIGatewayV2HTTPResponseBuilder withMultiValueHeaders(@Nullable Map<String, List<String>> multiValueHeaders) {
            this.multiValueHeaders = multiValueHeaders;
            return this;
        }

        @NonNull
        public APIGatewayV2HTTPResponseBuilder withCookies(@Nullable List<String> cookies) {
            this.cookies = cookies;
            return this;
        }

        @NonNull
        public APIGatewayV2HTTPResponseBuilder withBody(@Nullable String body) {
            this.body = body;
            return this;
        }

        @NonNull
        public APIGatewayV2HTTPResponseBuilder withIsBase64Encoded(boolean isBase64Encoded) {
            this.isBase64Encoded = isBase64Encoded;
            return this;
        }

        @NonNull
        public APIGatewayV2HTTPResponse build() {
            return new APIGatewayV2HTTPResponse(this.statusCode, this.headers, this.multiValueHeaders, this.cookies, this.body, this.isBase64Encoded);
        }

        @NonNull
        public String toString() {
            return "APIGatewayV2HTTPResponse.APIGatewayV2HTTPResponseBuilder(statusCode=" + this.statusCode + ", headers=" + this.headers + ", multiValueHeaders=" + this.multiValueHeaders + ", cookies=" + this.cookies + ", body=" + this.body + ", isBase64Encoded=" + this.isBase64Encoded + ")";
        }
    }
}
