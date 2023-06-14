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
 * Port of AWS Lambda Events class which represents an APIGatewayProxyRequestEvent.
 * This class adds Serdeable, nullability annotations and @Creator annotations to it.
 * @see <a href="https://github.com/aws/aws-lambda-java-libs/blob/main/aws-lambda-java-events/src/main/java/com/amazonaws/services/lambda/runtime/events/APIGatewayProxyRequestEvent.java">APIGatewayProxyRequestEvent</a>
 * @author Sergio del Amo
 * @since 4.0.0
 */
@SuppressWarnings({
    "checkstyle:MissingJavadocType",
    "checkstyle:DesignForExtension"
})
@Serdeable
public class APIGatewayProxyRequestEvent implements Serializable, Cloneable {

    private static final long serialVersionUID = 4189228800688527467L;

    @Nullable
    private String version;

    @Nullable
    private String resource;

    @Nullable
    private String path;

    @Nullable
    private String httpMethod;

    @Nullable
    private Map<String, String> headers;

    @Nullable
    private Map<String, List<String>> multiValueHeaders;

    @Nullable
    private Map<String, String> queryStringParameters;

    @Nullable
    private Map<String, List<String>> multiValueQueryStringParameters;

    @Nullable
    private Map<String, String> pathParameters;

    @Nullable
    private Map<String, String> stageVariables;

    @Nullable
    private ProxyRequestContext requestContext;

    @Nullable
    private String body;

    @Nullable
    private Boolean isBase64Encoded;

    /**
     * @return The payload format version
     */
    @Nullable
    public String getVersion() {
        return version;
    }

    /**
     * @param version The payload format version
     */
    public void setVersion(@Nullable String version) {
        this.version = version;
    }

    /**
     * @param version The payload format version
     * @return the APIGatewayProxyRequestEvent
     */
    @NonNull
    public APIGatewayProxyRequestEvent withVersion(@Nullable String version) {
        this.setVersion(version);
        return this;
    }

    /**
     * @return The resource path defined in API Gateway
     */
    @Nullable
    public String getResource() {
        return resource;
    }

    /**
     * @param resource The resource path defined in API Gateway
     */
    public void setResource(@Nullable String resource) {
        this.resource = resource;
    }

    /**
     * @param resource The resource path defined in API Gateway
     * @return the APIGatewayProxyRequestEvent
     */
    @NonNull
    public APIGatewayProxyRequestEvent withResource(@Nullable String resource) {
        this.setResource(resource);
        return this;
    }

    /**
     * @return The url path for the caller
     */
    @Nullable
    public String getPath() {
        return path;
    }

    /**
     * @param path The url path for the caller
     */
    public void setPath(@Nullable String path) {
        this.path = path;
    }

    /**
     * @param path The url path for the caller
     * @return APIGatewayProxyRequestEvent object
     */
    @NonNull
    public APIGatewayProxyRequestEvent withPath(@Nullable String path) {
        this.setPath(path);
        return this;
    }

    /**
     * @return The HTTP method used
     */
    @Nullable
    public String getHttpMethod() {
        return httpMethod;
    }

    /**
     * @param httpMethod The HTTP method used
     */
    public void setHttpMethod(@Nullable String httpMethod) {
        this.httpMethod = httpMethod;
    }

    /**
     * @param httpMethod The HTTP method used
     * @return APIGatewayProxyRequestEvent
     */
    @NonNull
    public APIGatewayProxyRequestEvent withHttpMethod(@Nullable String httpMethod) {
        this.setHttpMethod(httpMethod);
        return this;
    }

    /**
     * @return The headers sent with the request
     */
    @Nullable
    public Map<String, String> getHeaders() {
        return headers;
    }

    /**
     * @param headers The headers sent with the request
     */
    public void setHeaders(@Nullable Map<String, String> headers) {
        this.headers = headers;
    }

    /**
     * @param headers The headers sent with the request
     * @return APIGatewayProxyRequestEvent object
     */
    @NonNull
    public APIGatewayProxyRequestEvent withHeaders(@Nullable Map<String, String> headers) {
        this.setHeaders(headers);
        return this;
    }

    /**
     * @return The multi value headers sent with the request
     */
    @Nullable
    public Map<String, List<String>> getMultiValueHeaders() {
        return multiValueHeaders;
    }

    /**
     * @param multiValueHeaders The multi value headers sent with the request
     */
    public void setMultiValueHeaders(@Nullable Map<String, List<String>> multiValueHeaders) {
        this.multiValueHeaders = multiValueHeaders;
    }

    /**
     * @param multiValueHeaders The multi value headers sent with the request
     * @return APIGatewayProxyRequestEvent object
     */
    @NonNull
    public APIGatewayProxyRequestEvent withMultiValueHeaders(@Nullable Map<String, List<String>> multiValueHeaders) {
        this.setMultiValueHeaders(multiValueHeaders);
        return this;
    }

    /**
     * @return The query string parameters that were part of the request
     */
    @Nullable
    public Map<String, String> getQueryStringParameters() {
        return queryStringParameters;
    }

    /**
     * @param queryStringParameters The query string parameters that were part of the request
     */
    public void setQueryStringParameters(@Nullable Map<String, String> queryStringParameters) {
        this.queryStringParameters = queryStringParameters;
    }

    /**
     * @param queryStringParameters The query string parameters that were part of the request
     * @return APIGatewayProxyRequestEvent
     */
    @NonNull
    public APIGatewayProxyRequestEvent withQueryStringParameters(@Nullable Map<String, String> queryStringParameters) {
        this.setQueryStringParameters(queryStringParameters);
        return this;
    }

    /**
     * @return The multi value query string parameters that were part of the request
     */
    @Nullable
    public Map<String, List<String>> getMultiValueQueryStringParameters() {
        return multiValueQueryStringParameters;
    }

    /**
     * @param multiValueQueryStringParameters The multi value query string parameters that were part of the request
     */
    public void setMultiValueQueryStringParameters(@Nullable Map<String, List<String>> multiValueQueryStringParameters) {
        this.multiValueQueryStringParameters = multiValueQueryStringParameters;
    }

    /**
     * @param multiValueQueryStringParameters The multi value query string parameters that were part of the request
     * @return APIGatewayProxyRequestEvent
     */
    @NonNull
    public APIGatewayProxyRequestEvent withMultiValueQueryStringParameters(@Nullable Map<String, List<String>> multiValueQueryStringParameters) {
        this.setMultiValueQueryStringParameters(multiValueQueryStringParameters);
        return this;
    }

    /**
     * @return The path parameters that were part of the request
     */
    @Nullable
    public Map<String, String> getPathParameters() {
        return pathParameters;
    }

    /**
     * @param pathParameters The path parameters that were part of the request
     */
    public void setPathParameters(@Nullable Map<String, String> pathParameters) {
        this.pathParameters = pathParameters;
    }

    /**
     * @param pathParameters The path parameters that were part of the request
     * @return APIGatewayProxyRequestEvent object
     */
    @NonNull
    public APIGatewayProxyRequestEvent withPathParameters(@Nullable Map<String, String> pathParameters) {
        this.setPathParameters(pathParameters);
        return this;
    }

    /**
     * @return The stage variables defined for the stage in API Gateway
     */
    @Nullable
    public Map<String, String> getStageVariables() {
        return stageVariables;
    }

    /**
     * @param stageVariables The stage variables defined for the stage in API Gateway
     */
    public void setStageVariables(@Nullable Map<String, String> stageVariables) {
        this.stageVariables = stageVariables;
    }

    /**
     * @param stageVariables The stage variables defined for the stage in API Gateway
     * @return APIGatewayProxyRequestEvent
     */
    @NonNull
    public APIGatewayProxyRequestEvent withStageVariables(@Nullable Map<String, String> stageVariables) {
        this.setStageVariables(stageVariables);
        return this;
    }

    /**
     * @return The request context for the request
     */
    @Nullable
    public ProxyRequestContext getRequestContext() {
        return requestContext;
    }

    /**
     * @param requestContext The request context for the request
     */
    public void setRequestContext(@Nullable ProxyRequestContext requestContext) {
        this.requestContext = requestContext;
    }

    /**
     * @param requestContext The request context for the request
     * @return APIGatewayProxyRequestEvent object
     */
    @NonNull
    public APIGatewayProxyRequestEvent withRequestContext(@Nullable ProxyRequestContext requestContext) {
        this.setRequestContext(requestContext);
        return this;
    }

    /**
     * @return The HTTP request body.
     */
    @Nullable
    public String getBody() {
        return body;
    }

    /**
     * @param body The HTTP request body.
     */
    public void setBody(@Nullable String body) {
        this.body = body;
    }

    /**
     * @param body The HTTP request body
     * @return APIGatewayProxyRequestEvent
     */
    public APIGatewayProxyRequestEvent withBody(@Nullable String body) {
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
    public APIGatewayProxyRequestEvent withIsBase64Encoded(@Nullable Boolean isBase64Encoded) {
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
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        if (getVersion() != null) {
            sb.append("version: ").append(getVersion()).append(",");
        }
        if (getResource() != null) {
            sb.append("resource: ").append(getResource()).append(",");
        }
        if (getPath() != null) {
            sb.append("path: ").append(getPath()).append(",");
        }
        if (getHttpMethod() != null) {
            sb.append("httpMethod: ").append(getHttpMethod()).append(",");
        }
        if (getHeaders() != null) {
            sb.append("headers: ").append(getHeaders().toString()).append(",");
        }
        if (getMultiValueHeaders() != null) {
            sb.append("multiValueHeaders: ").append(getMultiValueHeaders().toString()).append(",");
        }
        if (getQueryStringParameters() != null) {
            sb.append("queryStringParameters: ").append(getQueryStringParameters().toString()).append(",");
        }
        if (getMultiValueQueryStringParameters() != null) {
            sb.append("multiValueQueryStringParameters: ").append(getMultiValueQueryStringParameters().toString()).append(",");
        }
        if (getPathParameters() != null) {
            sb.append("pathParameters: ").append(getPathParameters().toString()).append(",");
        }
        if (getStageVariables() != null) {
            sb.append("stageVariables: ").append(getStageVariables().toString()).append(",");
        }
        if (getRequestContext() != null) {
            sb.append("requestContext: ").append(getRequestContext().toString()).append(",");
        }
        if (getBody() != null) {
            sb.append("body: ").append(getBody()).append(",");
        }
        if (getIsBase64Encoded() != null) {
            sb.append("isBase64Encoded: ").append(getIsBase64Encoded());
        }
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

        APIGatewayProxyRequestEvent that = (APIGatewayProxyRequestEvent) o;

        if (!Objects.equals(version, that.version)) {
            return false;
        }
        if (!Objects.equals(resource, that.resource)) {
            return false;
        }
        if (!Objects.equals(path, that.path)) {
            return false;
        }
        if (!Objects.equals(httpMethod, that.httpMethod)) {
            return false;
        }
        if (!Objects.equals(headers, that.headers)) {
            return false;
        }
        if (!Objects.equals(multiValueHeaders, that.multiValueHeaders)) {
            return false;
        }
        if (!Objects.equals(queryStringParameters, that.queryStringParameters)) {
            return false;
        }
        if (!Objects.equals(multiValueQueryStringParameters, that.multiValueQueryStringParameters)) {
            return false;
        }
        if (!Objects.equals(pathParameters, that.pathParameters)) {
            return false;
        }
        if (!Objects.equals(stageVariables, that.stageVariables)) {
            return false;
        }
        if (!Objects.equals(requestContext, that.requestContext)) {
            return false;
        }
        if (!Objects.equals(body, that.body)) {
            return false;
        }
        return Objects.equals(isBase64Encoded, that.isBase64Encoded);
    }

    @Override
    public int hashCode() {
        int result = version != null ? version.hashCode() : 0;
        result = 31 * result + (resource != null ? resource.hashCode() : 0);
        result = 31 * result + (path != null ? path.hashCode() : 0);
        result = 31 * result + (httpMethod != null ? httpMethod.hashCode() : 0);
        result = 31 * result + (headers != null ? headers.hashCode() : 0);
        result = 31 * result + (multiValueHeaders != null ? multiValueHeaders.hashCode() : 0);
        result = 31 * result + (queryStringParameters != null ? queryStringParameters.hashCode() : 0);
        result = 31 * result + (multiValueQueryStringParameters != null ? multiValueQueryStringParameters.hashCode() : 0);
        result = 31 * result + (pathParameters != null ? pathParameters.hashCode() : 0);
        result = 31 * result + (stageVariables != null ? stageVariables.hashCode() : 0);
        result = 31 * result + (requestContext != null ? requestContext.hashCode() : 0);
        result = 31 * result + (body != null ? body.hashCode() : 0);
        result = 31 * result + (isBase64Encoded != null ? isBase64Encoded.hashCode() : 0);
        return result;
    }

    @Override
    @NonNull
    public APIGatewayProxyRequestEvent clone() {
        try {
            return (APIGatewayProxyRequestEvent) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new IllegalStateException("Got a CloneNotSupportedException from Object.clone()", e);
        }
    }

    /**
     * class that represents proxy request context.
     */
    @Serdeable
    public static class ProxyRequestContext implements Serializable, Cloneable {

        private static final long serialVersionUID = 8783459961042799774L;

        @Nullable
        private String accountId;

        @Nullable
        private String stage;

        @Nullable
        private String resourceId;

        @Nullable
        private String requestId;

        @Nullable
        private String operationName;

        @Nullable
        private RequestIdentity identity;

        @Nullable
        private String resourcePath;

        @Nullable
        private String httpMethod;

        @Nullable
        private String apiId;

        private String path;

        @Nullable
        private Map<String, Object> authorizer;

        @Nullable
        private String extendedRequestId;

        @Nullable
        private String requestTime;

        @Nullable
        private Long requestTimeEpoch;

        @Nullable
        private String domainName;

        @Nullable
        private String domainPrefix;

        @Nullable
        private String protocol;

        /**
         * @return account id that owns Lambda function
         */
        @Nullable
        public String getAccountId() {
            return accountId;
        }

        /**
         * @param accountId account id that owns Lambda function
         */
        public void setAccountId(@Nullable String accountId) {
            this.accountId = accountId;
        }

        /**
         * @param accountId account id that owns Lambda function
         * @return ProxyRequestContext object
         */
        @NonNull
        public ProxyRequestContext withAccountId(@Nullable String accountId) {
            this.setAccountId(accountId);
            return this;
        }

        @Nullable
        public Map<String, Object> getAuthorizer() {
            return authorizer;
        }

        public void setAuthorizer(final @Nullable Map<String, Object> authorizer) {
            this.authorizer = authorizer;
        }

        /**
         * @return  API Gateway stage name
         */
        @Nullable
        public String getStage() {
            return stage;
        }

        /**
         * @param stage API Gateway stage name
         */
        public void setStage(@Nullable String stage) {
            this.stage = stage;
        }

        /**
         * @param stage API Gateway stage name
         * @return ProxyRequestContext object
         */
        @NonNull
        public ProxyRequestContext withStage(@Nullable String stage) {
            this.setStage(stage);
            return this;
        }

        /**
         * @return resource id
         */
        @Nullable
        public String getResourceId() {
            return resourceId;
        }

        /**
         * @param resourceId resource id
         */
        public void setResourceId(@Nullable String resourceId) {
            this.resourceId = resourceId;
        }

        /**
         * @param resourceId resource id
         * @return ProxyRequestContext object
         */
        @NonNull
        public ProxyRequestContext withResourceId(@Nullable String resourceId) {
            this.setResourceId(resourceId);
            return this;
        }

        /**
         * @return unique request id
         */
        @Nullable
        public String getRequestId() {
            return requestId;
        }

        /**
         * @param requestId unique request id
         */
        public void setRequestId(@Nullable String requestId) {
            this.requestId = requestId;
        }

        /**
         * @param requestId unique request id
         * @return ProxyRequestContext object
         */
        @NonNull
        public ProxyRequestContext withRequestId(@Nullable String requestId) {
            this.setRequestId(requestId);
            return this;
        }

        /**
         * @return The identity information for the request caller
         */
        @Nullable
        public RequestIdentity getIdentity() {
            return identity;
        }

        /**
         * @param identity The identity information for the request caller
         */
        public void setIdentity(@Nullable RequestIdentity identity) {
            this.identity = identity;
        }

        /**
         * @param identity The identity information for the request caller
         * @return ProxyRequestContext object
         */
        @NonNull
        public ProxyRequestContext withIdentity(@Nullable RequestIdentity identity) {
            this.setIdentity(identity);
            return this;
        }

        /**
         * @return The resource path defined in API Gateway
         */
        @Nullable
        public String getResourcePath() {
            return resourcePath;
        }

        /**
         * @param resourcePath The resource path defined in API Gateway
         */
        public void setResourcePath(@Nullable String resourcePath) {
            this.resourcePath = resourcePath;
        }

        /**
         * @param resourcePath The resource path defined in API Gateway
         * @return ProxyRequestContext object
         */
        @NonNull
        public ProxyRequestContext withResourcePath(@Nullable String resourcePath) {
            this.setResourcePath(resourcePath);
            return this;
        }

        /**
         * @return The HTTP method used
         */
        @Nullable
        public String getHttpMethod() {
            return httpMethod;
        }

        /**
         * @param httpMethod the HTTP method used
         */
        public void setHttpMethod(@Nullable String httpMethod) {
            this.httpMethod = httpMethod;
        }

        /**
         * @param httpMethod the HTTP method used
         * @return ProxyRequestContext object
         */
        @NonNull
        public ProxyRequestContext withHttpMethod(@Nullable String httpMethod) {
            this.setHttpMethod(httpMethod);
            return this;
        }

        /**
         * @return The API Gateway rest API Id.
         */
        @Nullable
        public String getApiId() {
            return apiId;
        }

        /**
         * @param apiId The API Gateway rest API Id.
         */
        public void setApiId(@Nullable String apiId) {
            this.apiId = apiId;
        }

        /**
         * @param apiId The API Gateway rest API Id
         * @return ProxyRequestContext object
         */
        @NonNull
        public ProxyRequestContext withApiId(@Nullable String apiId) {
            this.setApiId(apiId);
            return this;
        }

        /**
         * @return The API Gateway path (Does not include base url)
         */
        @Nullable
        public String getPath() {
            return this.path;
        }

        /**
         * @param path The API Gateway path (Does not include base url)
         */
        public void setPath(@Nullable String path) {
            this.path = path;
        }

        /**
         * @param path The API Gateway path (Does not include base url)
         * @return ProxyRequestContext object
         */
        @NonNull
        public ProxyRequestContext withPath(@Nullable String path) {
            this.setPath(path);
            return this;
        }

        /**
         * @return The name of the operation being performed
         * */
        @Nullable
        public String getOperationName() {
            return operationName;
        }

        /**
         * @param operationName The name of the operation being performed
         * */
        public void setOperationName(@Nullable String operationName) {
            this.operationName = operationName;
        }

        public ProxyRequestContext withOperationName(@Nullable String operationName) {
            this.setOperationName(operationName);
            return this;
        }

        /**
         * @return The API Gateway Extended Request Id
         */
        @Nullable
        public String getExtendedRequestId() {
            return extendedRequestId;
        }

        /**
         * @param extendedRequestId The API Gateway Extended Request Id
         */
        public void setExtendedRequestId(@Nullable String extendedRequestId) {
            this.extendedRequestId = extendedRequestId;
        }

        /**
         * @param extendedRequestId The API Gateway Extended Request Id
         * @return ProxyRequestContext object
         */
        @NonNull
        public ProxyRequestContext withExtendedRequestId(@Nullable String extendedRequestId) {
            this.setExtendedRequestId(extendedRequestId);
            return this;
        }

        /**
         * @return The CLF-formatted request time (dd/MMM/yyyy:HH:mm:ss +-hhmm).
         */
        @Nullable
        public String getRequestTime() {
            return requestTime;
        }

        /**
         * @param requestTime The CLF-formatted request time (dd/MMM/yyyy:HH:mm:ss +-hhmm).
         */
        public void setRequestTime(@Nullable String requestTime) {
            this.requestTime = requestTime;
        }

        /**
         * @param requestTime The CLF-formatted request time (dd/MMM/yyyy:HH:mm:ss +-hhmm).
         * @return  ProxyRequestContext object
         */
        @NonNull
        public ProxyRequestContext withRequestTime(@Nullable String requestTime) {
            this.setRequestTime(requestTime);
            return this;
        }

        /**
         * @return The Epoch-formatted request time (in millis)
         */
        @Nullable
        public Long getRequestTimeEpoch() {
            return requestTimeEpoch;
        }

        /**
         * @param requestTimeEpoch The Epoch-formatted request time (in millis)
         */
        public void setRequestTimeEpoch(@Nullable Long requestTimeEpoch) {
            this.requestTimeEpoch = requestTimeEpoch;
        }

        /**
         * @param requestTimeEpoch The Epoch-formatted request time (in millis)
         * @return ProxyRequestContext object
         */
        @NonNull
        public ProxyRequestContext withRequestTimeEpoch(@Nullable Long requestTimeEpoch) {
            this.setRequestTimeEpoch(requestTimeEpoch);
            return this;
        }

        /**
         * @return The full domain name used to invoke the API. This should be the same as the incoming Host header.
         */
        @Nullable
        public String getDomainName() {
            return domainName;
        }

        /**
         * @param domainName The full domain name used to invoke the API.
         *                   This should be the same as the incoming Host header.
         */
        public void setDomainName(@Nullable String domainName) {
            this.domainName = domainName;
        }

        /**
         * @param domainName The full domain name used to invoke the API.
         *                   This should be the same as the incoming Host header.
         * @return ProxyRequestContext object
         */
        @NonNull
        public ProxyRequestContext withDomainName(@Nullable String domainName) {
            this.setDomainName(domainName);
            return this;
        }

        /**
         * @return The first label of the domainName. This is often used as a caller/customer identifier.
         */
        @Nullable
        public String getDomainPrefix() {
            return domainPrefix;
        }

        /**
         * @param domainPrefix The first label of the domainName. This is often used as a caller/customer identifier.
         */
        public void setDomainPrefix(@Nullable String domainPrefix) {
            this.domainPrefix = domainPrefix;
        }

        /**
         * @param domainPrefix The first label of the domainName. This is often used as a caller/customer identifier.
         * @return ProxyRequestContext object
         */
        @NonNull
        public ProxyRequestContext withDomainPrefix(@Nullable String domainPrefix) {
            this.setDomainPrefix(domainPrefix);
            return this;
        }

        /**
         * @return The request protocol, for example, HTTP/1.1.
         */
        @Nullable
        public String getProtocol() {
            return protocol;
        }

        /**
         * @param protocol  The request protocol, for example, HTTP/1.1.
         */
        public void setProtocol(@Nullable String protocol) {
            this.protocol = protocol;
        }

        /**
         * @param protocol  The request protocol, for example, HTTP/1.1.
         * @return ProxyRequestContext object
         */
        @NonNull
        public ProxyRequestContext withProtocol(@Nullable String protocol) {
            this.setProtocol(protocol);
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
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("{");
            if (getAccountId() != null) {
                sb.append("accountId: ").append(getAccountId()).append(",");
            }
            if (getResourceId() != null) {
                sb.append("resourceId: ").append(getResourceId()).append(",");
            }
            if (getStage() != null) {
                sb.append("stage: ").append(getStage()).append(",");
            }
            if (getRequestId() != null) {
                sb.append("requestId: ").append(getRequestId()).append(",");
            }
            if (getIdentity() != null) {
                sb.append("identity: ").append(getIdentity().toString()).append(",");
            }
            if (getResourcePath() != null) {
                sb.append("resourcePath: ").append(getResourcePath()).append(",");
            }
            if (getHttpMethod() != null) {
                sb.append("httpMethod: ").append(getHttpMethod()).append(",");
            }
            if (getApiId() != null) {
                sb.append("apiId: ").append(getApiId()).append(",");
            }
            if (getPath() != null) {
                sb.append("path: ").append(getPath()).append(",");
            }
            if (getAuthorizer() != null) {
                sb.append("authorizer: ").append(getAuthorizer().toString());
            }
            if (getOperationName() != null) {
                sb.append("operationName: ").append(getOperationName().toString());
            }
            if (getExtendedRequestId() != null) {
                sb.append("extendedRequestId: ").append(getExtendedRequestId()).append(",");
            }
            if (getRequestTime() != null) {
                sb.append("requestTime: ").append(getRequestTime()).append(",");
            }
            if (getProtocol() != null) {
                sb.append("protocol: ").append(getProtocol()).append(",");
            }
            if (getRequestTimeEpoch() != null) {
                sb.append("requestTimeEpoch: ").append(getRequestTimeEpoch()).append(",");
            }
            if (getDomainPrefix() != null) {
                sb.append("domainPrefix: ").append(getDomainPrefix()).append(",");
            }
            if (getDomainName() != null) {
                sb.append("domainName: ").append(getDomainName());
            }
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

            ProxyRequestContext that = (ProxyRequestContext) o;

            if (!Objects.equals(accountId, that.accountId)) {
                return false;
            }
            if (!Objects.equals(stage, that.stage)) {
                return false;
            }
            if (!Objects.equals(resourceId, that.resourceId)) {
                return false;
            }
            if (!Objects.equals(requestId, that.requestId)) {
                return false;
            }
            if (!Objects.equals(operationName, that.operationName)) {
                return false;
            }
            if (!Objects.equals(identity, that.identity)) {
                return false;
            }
            if (!Objects.equals(resourcePath, that.resourcePath)) {
                return false;
            }
            if (!Objects.equals(httpMethod, that.httpMethod)) {
                return false;
            }
            if (!Objects.equals(apiId, that.apiId)) {
                return false;
            }
            if (!Objects.equals(path, that.path)) {
                return false;
            }
            if (!Objects.equals(authorizer, that.authorizer)) {
                return false;
            }
            if (!Objects.equals(extendedRequestId, that.extendedRequestId)) {
                return false;
            }
            if (!Objects.equals(requestTime, that.requestTime)) {
                return false;
            }
            if (!Objects.equals(requestTimeEpoch, that.requestTimeEpoch)) {
                return false;
            }
            if (!Objects.equals(domainName, that.domainName)) {
                return false;
            }
            if (!Objects.equals(domainPrefix, that.domainPrefix)) {
                return false;
            }
            return Objects.equals(protocol, that.protocol);
        }

        @Override
        public int hashCode() {
            int result = accountId != null ? accountId.hashCode() : 0;
            result = 31 * result + (stage != null ? stage.hashCode() : 0);
            result = 31 * result + (resourceId != null ? resourceId.hashCode() : 0);
            result = 31 * result + (requestId != null ? requestId.hashCode() : 0);
            result = 31 * result + (operationName != null ? operationName.hashCode() : 0);
            result = 31 * result + (identity != null ? identity.hashCode() : 0);
            result = 31 * result + (resourcePath != null ? resourcePath.hashCode() : 0);
            result = 31 * result + (httpMethod != null ? httpMethod.hashCode() : 0);
            result = 31 * result + (apiId != null ? apiId.hashCode() : 0);
            result = 31 * result + (path != null ? path.hashCode() : 0);
            result = 31 * result + (authorizer != null ? authorizer.hashCode() : 0);
            result = 31 * result + (extendedRequestId != null ? extendedRequestId.hashCode() : 0);
            result = 31 * result + (requestTime != null ? requestTime.hashCode() : 0);
            result = 31 * result + (requestTimeEpoch != null ? requestTimeEpoch.hashCode() : 0);
            result = 31 * result + (domainName != null ? domainName.hashCode() : 0);
            result = 31 * result + (domainPrefix != null ? domainPrefix.hashCode() : 0);
            result = 31 * result + (protocol != null ? protocol.hashCode() : 0);
            return result;
        }

        @Override
        @NonNull
        public ProxyRequestContext clone() {
            try {
                return (ProxyRequestContext) super.clone();
            } catch (CloneNotSupportedException e) {
                throw new IllegalStateException("Got a CloneNotSupportedException from Object.clone()", e);
            }
        }
    }

    @Serdeable
    public static class RequestIdentity implements Serializable, Cloneable {

        private static final long serialVersionUID = -5283829736983640346L;

        @Nullable
        private String cognitoIdentityPoolId;

        @Nullable
        private String accountId;

        @Nullable
        private String cognitoIdentityId;

        @Nullable
        private String caller;

        @Nullable
        private String apiKey;

        @Nullable
        private String principalOrgId;

        @Nullable
        private String sourceIp;

        @Nullable
        private String cognitoAuthenticationType;

        @Nullable
        private String cognitoAuthenticationProvider;

        @Nullable
        private String userArn;

        @Nullable
        private String userAgent;

        private String user;

        @Nullable private String accessKey;

        /**
         * @return The Cognito identity pool id.
         */
        @Nullable
        public String getCognitoIdentityPoolId() {
            return cognitoIdentityPoolId;
        }

        /**
         * @param cognitoIdentityPoolId The Cognito identity pool id.
         */
        public void setCognitoIdentityPoolId(@Nullable String cognitoIdentityPoolId) {
            this.cognitoIdentityPoolId = cognitoIdentityPoolId;
        }

        /**
         * @param cognitoIdentityPoolId The Cognito Identity pool id
         * @return RequestIdentity object
         */
        @NonNull
        public RequestIdentity withCognitoIdentityPoolId(@Nullable String cognitoIdentityPoolId) {
            this.setCognitoIdentityPoolId(cognitoIdentityPoolId);
            return this;
        }

        /**
         * @return The account id that owns the executing Lambda function
         */
        @Nullable
        public String getAccountId() {
            return accountId;
        }

        /**
         * @param accountId The account id that owns the executing Lambda function
         */
        public void setAccountId(@Nullable String accountId) {
            this.accountId = accountId;
        }

        /**
         * @param accountId The account id that owns the executing Lambda function
         * @return RequestIdentity object
         */
        @NonNull
        public RequestIdentity withAccountId(@Nullable String accountId) {
            this.setAccountId(accountId);
            return this;
        }

        /**
         * @return The cognito identity id.
         */
        @Nullable
        public String getCognitoIdentityId() {
            return cognitoIdentityId;
        }

        /**
         * @param cognitoIdentityId The cognito identity id.
         */
        public void setCognitoIdentityId(@Nullable String cognitoIdentityId) {
            this.cognitoIdentityId = cognitoIdentityId;
        }

        /**
         * @param cognitoIdentityId The cognito identity id
         * @return RequestIdentity object
         */
        @NonNull
        public RequestIdentity withCognitoIdentityId(@Nullable String cognitoIdentityId) {
            this.setCognitoIdentityId(cognitoIdentityId);
            return this;
        }

        /**
         * @return the caller
         */
        @Nullable
        public String getCaller() {
            return caller;
        }

        /**
         * @param caller the caller
         */
        public void setCaller(@Nullable String caller) {
            this.caller = caller;
        }

        /**
         * @param caller the caller
         * @return RequestIdentity object
         */
        @NonNull
        public RequestIdentity withCaller(@Nullable String caller) {
            this.setCaller(caller);
            return this;
        }

        /**
         * @return the api key
         */
        @Nullable
        public String getApiKey() {
            return apiKey;
        }

        /**
         * @param apiKey the api key
         */
        public void setApiKey(@Nullable String apiKey) {
            this.apiKey = apiKey;
        }

        /**
         * @param apiKey the api key
         * @return RequestIdentity object
         */
        @NonNull
        public RequestIdentity withApiKey(@Nullable String apiKey) {
            this.setApiKey(apiKey);
            return this;
        }

        /**
         * @return the principal org Id
         */
        @Nullable
        public String getPrincipalOrgId() {
            return principalOrgId;
        }

        /**
         * @param principalOrgId the principal org Id
         */
        public void setPrincipalOrgId(@Nullable String principalOrgId) {
            this.principalOrgId = principalOrgId;
        }

        /**
         * @param principalOrgId the principal org Id
         * @return RequestIdentity object
         */
        @NonNull
        public RequestIdentity withPrincipalOrgId(@Nullable String principalOrgId) {
            this.setPrincipalOrgId(principalOrgId);
            return this;
        }

        /**
         * @return source ip address
         */
        @Nullable
        public String getSourceIp() {
            return sourceIp;
        }

        /**
         * @param sourceIp source ip address
         */
        public void setSourceIp(@Nullable String sourceIp) {
            this.sourceIp = sourceIp;
        }

        /**
         * @param sourceIp source ip address
         * @return RequestIdentity object
         */
        @NonNull
        public RequestIdentity withSourceIp(@Nullable String sourceIp) {
            this.setSourceIp(sourceIp);
            return this;
        }

        /**
         * @return The Cognito authentication type used for authentication
         */
        @Nullable
        public String getCognitoAuthenticationType() {
            return cognitoAuthenticationType;
        }

        /**
         * @param cognitoAuthenticationType The Cognito authentication type used for authentication
         */
        public void setCognitoAuthenticationType(@Nullable String cognitoAuthenticationType) {
            this.cognitoAuthenticationType = cognitoAuthenticationType;
        }

        /**
         * @param cognitoAuthenticationType The Cognito authentication type used for authentication
         * @return RequestIdentity object
         */
        @NonNull
        public RequestIdentity withCognitoAuthenticationType(@Nullable String cognitoAuthenticationType) {
            this.setCognitoAuthenticationType(cognitoAuthenticationType);
            return this;
        }

        /**
         * @return The Cognito authentication provider
         */
        @Nullable
        public String getCognitoAuthenticationProvider() {
            return cognitoAuthenticationProvider;
        }

        /**
         * @param cognitoAuthenticationProvider The Cognito authentication provider
         */
        public void setCognitoAuthenticationProvider(@Nullable String cognitoAuthenticationProvider) {
            this.cognitoAuthenticationProvider = cognitoAuthenticationProvider;
        }

        /**
         * @param cognitoAuthenticationProvider The Cognito authentication provider
         * @return RequestIdentity object
         */
        @NonNull
        public RequestIdentity withCognitoAuthenticationProvider(@Nullable String cognitoAuthenticationProvider) {
            this.setCognitoAuthenticationProvider(cognitoAuthenticationProvider);
            return this;
        }

        /**
         * @return the user arn
         */
        @Nullable
        public String getUserArn() {
            return userArn;
        }

        /**
         * @param userArn user arn
         */
        public void setUserArn(@Nullable String userArn) {
            this.userArn = userArn;
        }

        /**
         * @param userArn user arn
         * @return RequestIdentity object
         */
        @NonNull
        public RequestIdentity withUserArn(@Nullable String userArn) {
            this.setUserArn(userArn);
            return this;
        }

        /**
         * @return user agent
         */
        @Nullable
        public String getUserAgent() {
            return userAgent;
        }

        /**
         * @param userAgent user agent
         */
        public void setUserAgent(@Nullable String userAgent) {
            this.userAgent = userAgent;
        }

        /**
         * @param userAgent user agent
         * @return RequestIdentityType
         */
        @NonNull
        public RequestIdentity withUserAgent(@Nullable String userAgent) {
            this.setUserAgent(userAgent);
            return this;
        }

        /**
         * @return user
         */
        @Nullable
        public String getUser() {
            return user;
        }

        /**
         * @param user user
         */
        public void setUser(@Nullable String user) {
            this.user = user;
        }

        /**
         * @param user user
         * @return RequestIdentity
         */
        @NonNull
        public RequestIdentity withUser(@Nullable String user) {
            this.setUser(user);
            return this;
        }

        /**
         * @return access key
         */
        @Nullable
        public String getAccessKey() {
            return this.accessKey;
        }

        /**
         * @param accessKey Cognito access key
         */
        public void setAccessKey(@Nullable String accessKey) {
            this.accessKey = accessKey;
        }

        /**
         * @param accessKey Cognito access key
         * @return RequestIdentity
         */
        @NonNull
        public RequestIdentity withAccessKey(@Nullable String accessKey) {
            this.setAccessKey(accessKey);
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
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("{");
            if (getCognitoIdentityPoolId() != null) {
                sb.append("cognitoIdentityPoolId: ").append(getCognitoIdentityPoolId()).append(",");
            }
            if (getAccountId() != null) {
                sb.append("accountId: ").append(getAccountId()).append(",");
            }
            if (getCognitoIdentityId() != null) {
                sb.append("cognitoIdentityId: ").append(getCognitoIdentityId()).append(",");
            }
            if (getCaller() != null) {
                sb.append("caller: ").append(getCaller()).append(",");
            }
            if (getApiKey() != null) {
                sb.append("apiKey: ").append(getApiKey()).append(",");
            }
            if (getPrincipalOrgId() != null) {
                sb.append("principalOrgId: ").append(getPrincipalOrgId()).append(",");
            }
            if (getSourceIp() != null) {
                sb.append("sourceIp: ").append(getSourceIp()).append(",");
            }
            if (getCognitoAuthenticationType() != null) {
                sb.append("eventTriggerConfigId: ").append(getCognitoAuthenticationType()).append(",");
            }
            if (getCognitoAuthenticationProvider() != null) {
                sb.append("cognitoAuthenticationProvider: ").append(getCognitoAuthenticationProvider()).append(",");
            }
            if (getUserArn() != null) {
                sb.append("userArn: ").append(getUserArn()).append(",");
            }
            if (getUserAgent() != null) {
                sb.append("userAgent: ").append(getUserAgent()).append(",");
            }
            if (getUser() != null) {
                sb.append("user: ").append(getUser()).append(",");
            }
            if (getAccessKey() != null) {
                sb.append("accessKey: ").append(getAccessKey());
            }
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

            RequestIdentity that = (RequestIdentity) o;

            if (!Objects.equals(cognitoIdentityPoolId, that.cognitoIdentityPoolId)) {
                return false;
            }
            if (!Objects.equals(accountId, that.accountId)) {
                return false;
            }
            if (!Objects.equals(cognitoIdentityId, that.cognitoIdentityId)) {
                return false;
            }
            if (!Objects.equals(caller, that.caller)) {
                return false;
            }
            if (!Objects.equals(apiKey, that.apiKey)) {
                return false;
            }
            if (!Objects.equals(principalOrgId, that.principalOrgId)) {
                return false;
            }
            if (!Objects.equals(sourceIp, that.sourceIp)) {
                return false;
            }
            if (!Objects.equals(cognitoAuthenticationType, that.cognitoAuthenticationType)) {
                return false;
            }
            if (!Objects.equals(cognitoAuthenticationProvider, that.cognitoAuthenticationProvider)) {
                return false;
            }
            if (!Objects.equals(userArn, that.userArn)) {
                return false;
            }
            if (!Objects.equals(userAgent, that.userAgent)) {
                return false;
            }
            if (!Objects.equals(user, that.user)) {
                return false;
            }
            return Objects.equals(accessKey, that.accessKey);
        }

        @Override
        public int hashCode() {
            int result = cognitoIdentityPoolId != null ? cognitoIdentityPoolId.hashCode() : 0;
            result = 31 * result + (accountId != null ? accountId.hashCode() : 0);
            result = 31 * result + (cognitoIdentityId != null ? cognitoIdentityId.hashCode() : 0);
            result = 31 * result + (caller != null ? caller.hashCode() : 0);
            result = 31 * result + (apiKey != null ? apiKey.hashCode() : 0);
            result = 31 * result + (principalOrgId != null ? principalOrgId.hashCode() : 0);
            result = 31 * result + (sourceIp != null ? sourceIp.hashCode() : 0);
            result = 31 * result + (cognitoAuthenticationType != null ? cognitoAuthenticationType.hashCode() : 0);
            result = 31 * result + (cognitoAuthenticationProvider != null ? cognitoAuthenticationProvider.hashCode() : 0);
            result = 31 * result + (userArn != null ? userArn.hashCode() : 0);
            result = 31 * result + (userAgent != null ? userAgent.hashCode() : 0);
            result = 31 * result + (user != null ? user.hashCode() : 0);
            result = 31 * result + (accessKey != null ? accessKey.hashCode() : 0);
            return result;
        }

        @Override
        @NonNull
        public RequestIdentity clone() {
            try {
                return (RequestIdentity) super.clone();
            } catch (CloneNotSupportedException e) {
                throw new IllegalStateException("Got a CloneNotSupportedException from Object.clone()", e);
            }
        }
    }

}
