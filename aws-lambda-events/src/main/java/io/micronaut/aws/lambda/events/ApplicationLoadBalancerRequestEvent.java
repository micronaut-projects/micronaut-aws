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
 * Port of AWS Lambda Events class which represents an ApplicationLoadBalancerRequestEvent.
 * This class adds Serdeable, nullability annotations and get rid of Lombok.
 * @see <a href="https://github.com/aws/aws-lambda-java-libs/blob/main/aws-lambda-java-events/src/main/java/com/amazonaws/services/lambda/runtime/events/ApplicationLoadBalancerRequestEvent.java">ApplicationLoadBalancerRequestEvent</a>
 * @author Sergio del Amo
 * @since 4.0.0
 */
@SuppressWarnings({
    "checkstyle:MissingJavadocType",
    "checkstyle:DesignForExtension"
})
@Serdeable
public class ApplicationLoadBalancerRequestEvent implements Serializable, Cloneable {
    @Nullable
    private RequestContext requestContext;

    @Nullable
    private String httpMethod;

    @Nullable
    private String path;

    @Nullable
    private Map<String, String> queryStringParameters;

    @Nullable
    private Map<String, List<String>> multiValueQueryStringParameters;

    @Nullable
    private Map<String, String> headers;

    @Nullable
    private Map<String, List<String>> multiValueHeaders;

    @Nullable
    private String body;

    private boolean isBase64Encoded;

    @Nullable
    public RequestContext getRequestContext() {
        return this.requestContext;
    }

    @Nullable
    public String getHttpMethod() {
        return this.httpMethod;
    }

    @Nullable
    public String getPath() {
        return this.path;
    }

    @Nullable
    public Map<String, String> getQueryStringParameters() {
        return this.queryStringParameters;
    }

    @Nullable
    public Map<String, List<String>> getMultiValueQueryStringParameters() {
        return this.multiValueQueryStringParameters;
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

    public boolean getIsBase64Encoded() {
        return this.isBase64Encoded;
    }

    public void setRequestContext(@Nullable RequestContext requestContext) {
        this.requestContext = requestContext;
    }

    public void setHttpMethod(@Nullable String httpMethod) {
        this.httpMethod = httpMethod;
    }

    public void setPath(@Nullable String path) {
        this.path = path;
    }

    public void setQueryStringParameters(@Nullable Map<String, String> queryStringParameters) {
        this.queryStringParameters = queryStringParameters;
    }

    public void setMultiValueQueryStringParameters(@Nullable Map<String, List<String>> multiValueQueryStringParameters) {
        this.multiValueQueryStringParameters = multiValueQueryStringParameters;
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

        ApplicationLoadBalancerRequestEvent that = (ApplicationLoadBalancerRequestEvent) o;

        if (isBase64Encoded != that.isBase64Encoded) {
            return false;
        }
        if (!Objects.equals(requestContext, that.requestContext)) {
            return false;
        }

        if (!Objects.equals(httpMethod, that.httpMethod)) {
            return false;
        }
        if (!Objects.equals(path, that.path)) {
            return false;
        }
        if (!Objects.equals(queryStringParameters, that.queryStringParameters)) {
            return false;
        }
        if (!Objects.equals(multiValueQueryStringParameters, that.multiValueQueryStringParameters)) {
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
        int result = requestContext != null ? requestContext.hashCode() : 0;
        result = 31 * result + (httpMethod != null ? httpMethod.hashCode() : 0);
        result = 31 * result + (path != null ? path.hashCode() : 0);
        result = 31 * result + (queryStringParameters != null ? queryStringParameters.hashCode() : 0);
        result = 31 * result + (multiValueQueryStringParameters != null ? multiValueQueryStringParameters.hashCode() : 0);
        result = 31 * result + (headers != null ? headers.hashCode() : 0);
        result = 31 * result + (multiValueHeaders != null ? multiValueHeaders.hashCode() : 0);
        result = 31 * result + (body != null ? body.hashCode() : 0);
        result = 31 * result + (isBase64Encoded ? 1 : 0);
        return result;
    }

    protected boolean canEqual(Object other) {
        return other instanceof ApplicationLoadBalancerRequestEvent;
    }

    @NonNull
    public String toString() {
        return "ApplicationLoadBalancerRequestEvent(requestContext=" + this.getRequestContext() + ", httpMethod=" + this.getHttpMethod() + ", path=" + this.getPath() + ", queryStringParameters=" + this.getQueryStringParameters() + ", multiValueQueryStringParameters=" + this.getMultiValueQueryStringParameters() + ", headers=" + this.getHeaders() + ", multiValueHeaders=" + this.getMultiValueHeaders() + ", body=" + this.getBody() + ", isBase64Encoded=" + this.getIsBase64Encoded() + ")";
    }

    @Serdeable
    public static class RequestContext implements Serializable, Cloneable {
        @Nullable
        private Elb elb;

        @Nullable
        public Elb getElb() {
            return this.elb;
        }

        public void setElb(@Nullable Elb elb) {
            this.elb = elb;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            RequestContext that = (RequestContext) o;

            return Objects.equals(elb, that.elb);
        }

        @Override
        public int hashCode() {
            return elb != null ? elb.hashCode() : 0;
        }

        protected boolean canEqual(Object other) {
            return other instanceof RequestContext;
        }

        @NonNull
        public String toString() {
            return "ApplicationLoadBalancerRequestEvent.RequestContext(elb=" + this.getElb() + ")";
        }
    }

    @Serdeable
    public static class Elb implements Serializable, Cloneable {
        @Nullable
        private String targetGroupArn;

        @Nullable
        public String getTargetGroupArn() {
            return this.targetGroupArn;
        }

        public void setTargetGroupArn(@Nullable String targetGroupArn) {
            this.targetGroupArn = targetGroupArn;
        }

        protected boolean canEqual(Object other) {
            return other instanceof Elb;
        }

        public String toString() {
            return "ApplicationLoadBalancerRequestEvent.Elb(targetGroupArn=" + this.getTargetGroupArn() + ")";
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            Elb elb = (Elb) o;

            return Objects.equals(targetGroupArn, elb.targetGroupArn);
        }

        @Override
        public int hashCode() {
            return targetGroupArn != null ? targetGroupArn.hashCode() : 0;
        }
    }
}
