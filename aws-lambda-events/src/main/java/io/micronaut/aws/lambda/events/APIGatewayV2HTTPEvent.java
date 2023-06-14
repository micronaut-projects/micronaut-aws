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
 * Port of AWS Lambda Events class which represents an APIGatewayV2HTTPEvent.
 * This class adds Serdeable, nullability annotations, @Creator annotations and removes Lombok.
 * @see <a href="https://github.com/aws/aws-lambda-java-libs/blob/main/aws-lambda-java-events/src/main/java/com/amazonaws/services/lambda/runtime/events/APIGatewayV2HTTPEvent.java">APIGatewayV2HTTPEvent</a>
 * @author Sergio del Amo
 * @since 4.0.0
 */
@SuppressWarnings({
    "checkstyle:MissingJavadocType",
    "checkstyle:DesignForExtension"
})
@Serdeable
public class APIGatewayV2HTTPEvent {
    @Nullable
    private String version;

    @Nullable
    private String routeKey;

    @Nullable
    private String rawPath;

    @Nullable
    private String rawQueryString;

    @Nullable
    private List<String> cookies;

    @Nullable
    private Map<String, String> headers;

    @Nullable
    private Map<String, String> queryStringParameters;

    @Nullable
    private Map<String, String> pathParameters;

    @Nullable
    private Map<String, String> stageVariables;

    @Nullable
    private String body;

    private boolean isBase64Encoded;

    @Nullable
    private RequestContext requestContext;

    public APIGatewayV2HTTPEvent() {
    }

    @Creator
    public APIGatewayV2HTTPEvent(@Nullable String version,
                                 @Nullable String routeKey,
                                 @Nullable String rawPath,
                                 @Nullable String rawQueryString,
                                 @Nullable List<String> cookies,
                                 @Nullable Map<String, String> headers,
                                 @Nullable Map<String, String> queryStringParameters,
                                 @Nullable Map<String, String> pathParameters,
                                 @Nullable Map<String, String> stageVariables,
                                 @Nullable String body,
                                 boolean isBase64Encoded,
                                 @Nullable RequestContext requestContext) {
        this.version = version;
        this.routeKey = routeKey;
        this.rawPath = rawPath;
        this.rawQueryString = rawQueryString;
        this.cookies = cookies;
        this.headers = headers;
        this.queryStringParameters = queryStringParameters;
        this.pathParameters = pathParameters;
        this.stageVariables = stageVariables;
        this.body = body;
        this.isBase64Encoded = isBase64Encoded;
        this.requestContext = requestContext;
    }

    @NonNull
    public static APIGatewayV2HTTPEventBuilder builder() {
        return new APIGatewayV2HTTPEventBuilder();
    }

    @Nullable
    public String getVersion() {
        return this.version;
    }

    @Nullable
    public String getRouteKey() {
        return this.routeKey;
    }

    @Nullable
    public String getRawPath() {
        return this.rawPath;
    }

    @Nullable
    public String getRawQueryString() {
        return this.rawQueryString;
    }

    @Nullable
    public List<String> getCookies() {
        return this.cookies;
    }

    @Nullable
    public Map<String, String> getHeaders() {
        return this.headers;
    }

    @Nullable
    public Map<String, String> getQueryStringParameters() {
        return this.queryStringParameters;
    }

    @Nullable
    public Map<String, String> getPathParameters() {
        return this.pathParameters;
    }

    @Nullable
    public Map<String, String> getStageVariables() {
        return this.stageVariables;
    }

    @Nullable
    public String getBody() {
        return this.body;
    }

    public boolean getIsBase64Encoded() {
        return this.isBase64Encoded;
    }

    @Nullable
    public RequestContext getRequestContext() {
        return this.requestContext;
    }

    public void setVersion(@Nullable String version) {
        this.version = version;
    }

    public void setRouteKey(@Nullable String routeKey) {
        this.routeKey = routeKey;
    }

    public void setRawPath(@Nullable String rawPath) {
        this.rawPath = rawPath;
    }

    public void setRawQueryString(@Nullable String rawQueryString) {
        this.rawQueryString = rawQueryString;
    }

    public void setCookies(@Nullable List<String> cookies) {
        this.cookies = cookies;
    }

    public void setHeaders(@Nullable Map<String, String> headers) {
        this.headers = headers;
    }

    public void setQueryStringParameters(@Nullable Map<String, String> queryStringParameters) {
        this.queryStringParameters = queryStringParameters;
    }

    public void setPathParameters(@Nullable Map<String, String> pathParameters) {
        this.pathParameters = pathParameters;
    }

    public void setStageVariables(@Nullable Map<String, String> stageVariables) {
        this.stageVariables = stageVariables;
    }

    public void setBody(@Nullable String body) {
        this.body = body;
    }

    public void setIsBase64Encoded(boolean isBase64Encoded) {
        this.isBase64Encoded = isBase64Encoded;
    }

    public void setRequestContext(@Nullable RequestContext requestContext) {
        this.requestContext = requestContext;
    }

    protected boolean canEqual(Object other) {
        return other instanceof APIGatewayV2HTTPEvent;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        APIGatewayV2HTTPEvent that = (APIGatewayV2HTTPEvent) o;

        if (isBase64Encoded != that.isBase64Encoded) {
            return false;
        }
        if (!Objects.equals(version, that.version)) {
            return false;
        }
        if (!Objects.equals(routeKey, that.routeKey)) {
            return false;
        }
        if (!Objects.equals(rawPath, that.rawPath)) {
            return false;
        }
        if (!Objects.equals(rawQueryString, that.rawQueryString)) {
            return false;
        }
        if (!Objects.equals(cookies, that.cookies)) {
            return false;
        }
        if (!Objects.equals(headers, that.headers)) {
            return false;
        }
        if (!Objects.equals(queryStringParameters, that.queryStringParameters)) {
            return false;
        }
        if (!Objects.equals(pathParameters, that.pathParameters)) {
            return false;
        }
        if (!Objects.equals(stageVariables, that.stageVariables)) {
            return false;
        }
        if (!Objects.equals(body, that.body)) {
            return false;
        }
        return Objects.equals(requestContext, that.requestContext);
    }

    @Override
    public int hashCode() {
        int result = version != null ? version.hashCode() : 0;
        result = 31 * result + (routeKey != null ? routeKey.hashCode() : 0);
        result = 31 * result + (rawPath != null ? rawPath.hashCode() : 0);
        result = 31 * result + (rawQueryString != null ? rawQueryString.hashCode() : 0);
        result = 31 * result + (cookies != null ? cookies.hashCode() : 0);
        result = 31 * result + (headers != null ? headers.hashCode() : 0);
        result = 31 * result + (queryStringParameters != null ? queryStringParameters.hashCode() : 0);
        result = 31 * result + (pathParameters != null ? pathParameters.hashCode() : 0);
        result = 31 * result + (stageVariables != null ? stageVariables.hashCode() : 0);
        result = 31 * result + (body != null ? body.hashCode() : 0);
        result = 31 * result + (isBase64Encoded ? 1 : 0);
        result = 31 * result + (requestContext != null ? requestContext.hashCode() : 0);
        return result;
    }

    @NonNull
    public String toString() {
        return "APIGatewayV2HTTPEvent(version=" + this.getVersion() + ", routeKey=" + this.getRouteKey() + ", rawPath=" + this.getRawPath() + ", rawQueryString=" + this.getRawQueryString() + ", cookies=" + this.getCookies() + ", headers=" + this.getHeaders() + ", queryStringParameters=" + this.getQueryStringParameters() + ", pathParameters=" + this.getPathParameters() + ", stageVariables=" + this.getStageVariables() + ", body=" + this.getBody() + ", isBase64Encoded=" + this.getIsBase64Encoded() + ", requestContext=" + this.getRequestContext() + ")";
    }

    public static class APIGatewayV2HTTPEventBuilder {
        @Nullable
        private String version;

        @Nullable
        private String routeKey;

        @Nullable
        private String rawPath;

        @Nullable
        private String rawQueryString;

        @Nullable
        private List<String> cookies;

        @Nullable
        private Map<String, String> headers;

        @Nullable
        private Map<String, String> queryStringParameters;

        @Nullable
        private Map<String, String> pathParameters;
        private Map<String, String> stageVariables;
        private String body;
        private boolean isBase64Encoded;
        private RequestContext requestContext;

        @NonNull
        public APIGatewayV2HTTPEventBuilder withVersion(@Nullable String version) {
            this.version = version;
            return this;
        }

        @NonNull
        public APIGatewayV2HTTPEventBuilder withRouteKey(@Nullable String routeKey) {
            this.routeKey = routeKey;
            return this;
        }

        @NonNull
        public APIGatewayV2HTTPEventBuilder withRawPath(@Nullable String rawPath) {
            this.rawPath = rawPath;
            return this;
        }

        @NonNull
        public APIGatewayV2HTTPEventBuilder withRawQueryString(@Nullable String rawQueryString) {
            this.rawQueryString = rawQueryString;
            return this;
        }

        @NonNull
        public APIGatewayV2HTTPEventBuilder withCookies(@Nullable List<String> cookies) {
            this.cookies = cookies;
            return this;
        }

        @NonNull
        public APIGatewayV2HTTPEventBuilder withHeaders(@Nullable Map<String, String> headers) {
            this.headers = headers;
            return this;
        }

        @NonNull
        public APIGatewayV2HTTPEventBuilder withQueryStringParameters(@Nullable Map<String, String> queryStringParameters) {
            this.queryStringParameters = queryStringParameters;
            return this;
        }

        @NonNull
        public APIGatewayV2HTTPEventBuilder withPathParameters(@Nullable Map<String, String> pathParameters) {
            this.pathParameters = pathParameters;
            return this;
        }

        @NonNull
        public APIGatewayV2HTTPEventBuilder withStageVariables(@Nullable Map<String, String> stageVariables) {
            this.stageVariables = stageVariables;
            return this;
        }

        @NonNull
        public APIGatewayV2HTTPEventBuilder withBody(@Nullable String body) {
            this.body = body;
            return this;
        }

        @NonNull
        public APIGatewayV2HTTPEventBuilder withIsBase64Encoded(boolean isBase64Encoded) {
            this.isBase64Encoded = isBase64Encoded;
            return this;
        }

        @NonNull
        public APIGatewayV2HTTPEventBuilder withRequestContext(@Nullable RequestContext requestContext) {
            this.requestContext = requestContext;
            return this;
        }

        @NonNull
        public APIGatewayV2HTTPEvent build() {
            return new APIGatewayV2HTTPEvent(this.version, this.routeKey, this.rawPath, this.rawQueryString, this.cookies, this.headers, this.queryStringParameters, this.pathParameters, this.stageVariables, this.body, this.isBase64Encoded, this.requestContext);
        }

        @NonNull
        public String toString() {
            return "APIGatewayV2HTTPEvent.APIGatewayV2HTTPEventBuilder(version=" + this.version + ", routeKey=" + this.routeKey + ", rawPath=" + this.rawPath + ", rawQueryString=" + this.rawQueryString + ", cookies=" + this.cookies + ", headers=" + this.headers + ", queryStringParameters=" + this.queryStringParameters + ", pathParameters=" + this.pathParameters + ", stageVariables=" + this.stageVariables + ", body=" + this.body + ", isBase64Encoded=" + this.isBase64Encoded + ", requestContext=" + this.requestContext + ")";
        }
    }

    @Serdeable
    public static class RequestContext {
        @Nullable
        private String routeKey;

        @Nullable
        private String accountId;

        @Nullable
        private String stage;

        @Nullable
        private String apiId;

        @Nullable
        private String domainName;

        @Nullable
        private String domainPrefix;

        @Nullable
        private String time;

        @Nullable
        private long timeEpoch;

        @Nullable
        private Http http;

        @Nullable
        private Authorizer authorizer;

        @Nullable
        private String requestId;

        public RequestContext() {
        }

        @Creator
        public RequestContext(@Nullable String routeKey,
                              @Nullable String accountId,
                              @Nullable String stage,
                              @Nullable String apiId,
                              @Nullable String domainName,
                              @Nullable String domainPrefix,
                              @Nullable String time,
                              long timeEpoch,
                              @Nullable Http http,
                              @Nullable Authorizer authorizer,
                              @Nullable String requestId) {
            this.routeKey = routeKey;
            this.accountId = accountId;
            this.stage = stage;
            this.apiId = apiId;
            this.domainName = domainName;
            this.domainPrefix = domainPrefix;
            this.time = time;
            this.timeEpoch = timeEpoch;
            this.http = http;
            this.authorizer = authorizer;
            this.requestId = requestId;
        }

        @NonNull
        public static RequestContextBuilder builder() {
            return new RequestContextBuilder();
        }

        @Nullable
        public String getRouteKey() {
            return this.routeKey;
        }

        @Nullable
        public String getAccountId() {
            return this.accountId;
        }

        @Nullable
        public String getStage() {
            return this.stage;
        }

        @Nullable
        public String getApiId() {
            return this.apiId;
        }

        @Nullable
        public String getDomainName() {
            return this.domainName;
        }

        @Nullable
        public String getDomainPrefix() {
            return this.domainPrefix;
        }

        @Nullable
        public String getTime() {
            return this.time;
        }

        @Nullable
        public long getTimeEpoch() {
            return this.timeEpoch;
        }

        @Nullable
        public Http getHttp() {
            return this.http;
        }

        @Nullable
        public Authorizer getAuthorizer() {
            return this.authorizer;
        }

        @Nullable
        public String getRequestId() {
            return this.requestId;
        }

        public void setRouteKey(@Nullable String routeKey) {
            this.routeKey = routeKey;
        }

        public void setAccountId(@Nullable String accountId) {
            this.accountId = accountId;
        }

        public void setStage(@Nullable String stage) {
            this.stage = stage;
        }

        public void setApiId(@Nullable String apiId) {
            this.apiId = apiId;
        }

        public void setDomainName(@Nullable String domainName) {
            this.domainName = domainName;
        }

        public void setDomainPrefix(String domainPrefix) {
            this.domainPrefix = domainPrefix;
        }

        public void setTime(@Nullable String time) {
            this.time = time;
        }

        public void setTimeEpoch(long timeEpoch) {
            this.timeEpoch = timeEpoch;
        }

        public void setHttp(@Nullable Http http) {
            this.http = http;
        }

        public void setAuthorizer(@Nullable Authorizer authorizer) {
            this.authorizer = authorizer;
        }

        public void setRequestId(@Nullable String requestId) {
            this.requestId = requestId;
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

            if (timeEpoch != that.timeEpoch) {
                return false;
            }
            if (!Objects.equals(routeKey, that.routeKey)) {
                return false;
            }
            if (!Objects.equals(accountId, that.accountId)) {
                return false;
            }
            if (!Objects.equals(stage, that.stage)) {
                return false;
            }
            if (!Objects.equals(apiId, that.apiId)) {
                return false;
            }
            if (!Objects.equals(domainName, that.domainName)) {
                return false;
            }
            if (!Objects.equals(domainPrefix, that.domainPrefix)) {
                return false;
            }
            if (!Objects.equals(time, that.time)) {
                return false;
            }
            if (!Objects.equals(http, that.http)) {
                return false;
            }
            if (!Objects.equals(authorizer, that.authorizer)) {
                return false;
            }
            return Objects.equals(requestId, that.requestId);
        }

        @Override
        public int hashCode() {
            int result = routeKey != null ? routeKey.hashCode() : 0;
            result = 31 * result + (accountId != null ? accountId.hashCode() : 0);
            result = 31 * result + (stage != null ? stage.hashCode() : 0);
            result = 31 * result + (apiId != null ? apiId.hashCode() : 0);
            result = 31 * result + (domainName != null ? domainName.hashCode() : 0);
            result = 31 * result + (domainPrefix != null ? domainPrefix.hashCode() : 0);
            result = 31 * result + (time != null ? time.hashCode() : 0);
            result = 31 * result + (int) (timeEpoch ^ (timeEpoch >>> 32));
            result = 31 * result + (http != null ? http.hashCode() : 0);
            result = 31 * result + (authorizer != null ? authorizer.hashCode() : 0);
            result = 31 * result + (requestId != null ? requestId.hashCode() : 0);
            return result;
        }

        protected boolean canEqual(Object other) {
            return other instanceof RequestContext;
        }

        @NonNull
        public String toString() {
            return "APIGatewayV2HTTPEvent.RequestContext(routeKey=" + this.getRouteKey() + ", accountId=" + this.getAccountId() + ", stage=" + this.getStage() + ", apiId=" + this.getApiId() + ", domainName=" + this.getDomainName() + ", domainPrefix=" + this.getDomainPrefix() + ", time=" + this.getTime() + ", timeEpoch=" + this.getTimeEpoch() + ", http=" + this.getHttp() + ", authorizer=" + this.getAuthorizer() + ", requestId=" + this.getRequestId() + ")";
        }

        public static class RequestContextBuilder {
            @Nullable
            private String routeKey;

            @Nullable
            private String accountId;

            @Nullable
            private String stage;

            @Nullable
            private String apiId;

            @Nullable
            private String domainName;

            @Nullable
            private String domainPrefix;

            @Nullable
            private String time;

            private long timeEpoch;

            @Nullable
            private Http http;

            @Nullable
            private Authorizer authorizer;

            @Nullable
            private String requestId;

            @NonNull
            public RequestContextBuilder withRouteKey(@Nullable String routeKey) {
                this.routeKey = routeKey;
                return this;
            }

            @NonNull
            public RequestContextBuilder withAccountId(@Nullable String accountId) {
                this.accountId = accountId;
                return this;
            }

            @NonNull
            public RequestContextBuilder withStage(@Nullable String stage) {
                this.stage = stage;
                return this;
            }

            @NonNull
            public RequestContextBuilder withApiId(@Nullable String apiId) {
                this.apiId = apiId;
                return this;
            }

            @NonNull
            public RequestContextBuilder withDomainName(@Nullable String domainName) {
                this.domainName = domainName;
                return this;
            }

            @NonNull
            public RequestContextBuilder withDomainPrefix(@Nullable String domainPrefix) {
                this.domainPrefix = domainPrefix;
                return this;
            }

            @NonNull
            public RequestContextBuilder withTime(@Nullable String time) {
                this.time = time;
                return this;
            }

            @NonNull
            public RequestContextBuilder withTimeEpoch(@Nullable long timeEpoch) {
                this.timeEpoch = timeEpoch;
                return this;
            }

            @NonNull
            public RequestContextBuilder withHttp(@Nullable Http http) {
                this.http = http;
                return this;
            }

            @NonNull
            public RequestContextBuilder withAuthorizer(@Nullable Authorizer authorizer) {
                this.authorizer = authorizer;
                return this;
            }

            @NonNull
            public RequestContextBuilder withRequestId(String requestId) {
                this.requestId = requestId;
                return this;
            }

            @NonNull
            public RequestContext build() {
                return new RequestContext(this.routeKey, this.accountId, this.stage, this.apiId, this.domainName, this.domainPrefix, this.time, this.timeEpoch, this.http, this.authorizer, this.requestId);
            }

            @NonNull
            public String toString() {
                return "APIGatewayV2HTTPEvent.RequestContext.RequestContextBuilder(routeKey=" + this.routeKey + ", accountId=" + this.accountId + ", stage=" + this.stage + ", apiId=" + this.apiId + ", domainName=" + this.domainName + ", domainPrefix=" + this.domainPrefix + ", time=" + this.time + ", timeEpoch=" + this.timeEpoch + ", http=" + this.http + ", authorizer=" + this.authorizer + ", requestId=" + this.requestId + ")";
            }
        }

        @Serdeable
        public static class CognitoIdentity {
            @Nullable
            private List<String> amr;

            @Nullable
            private String identityId;

            @Nullable
            private String identityPoolId;

            public CognitoIdentity() {
            }

            @Creator
            public CognitoIdentity(@Nullable List<String> amr,
                                   @Nullable String identityId,
                                   @Nullable String identityPoolId) {
                this.amr = amr;
                this.identityId = identityId;
                this.identityPoolId = identityPoolId;
            }

            @NonNull
            public static CognitoIdentityBuilder builder() {
                return new CognitoIdentityBuilder();
            }

            @Nullable public List<String> getAmr() {
                return this.amr;
            }

            @Nullable
            public String getIdentityId() {
                return this.identityId;
            }

            @Nullable
            public String getIdentityPoolId() {
                return this.identityPoolId;
            }

            public void setAmr(@Nullable List<String> amr) {
                this.amr = amr;
            }

            public void setIdentityId(@Nullable String identityId) {
                this.identityId = identityId;
            }

            public void setIdentityPoolId(@Nullable String identityPoolId) {
                this.identityPoolId = identityPoolId;
            }

            @Override
            public boolean equals(Object o) {
                if (this == o) {
                    return true;
                }
                if (o == null || getClass() != o.getClass()) {
                    return false;
                }

                CognitoIdentity that = (CognitoIdentity) o;

                if (!Objects.equals(amr, that.amr)) {
                    return false;
                }
                if (!Objects.equals(identityId, that.identityId)) {
                    return false;
                }
                return Objects.equals(identityPoolId, that.identityPoolId);
            }

            @Override
            public int hashCode() {
                int result = amr != null ? amr.hashCode() : 0;
                result = 31 * result + (identityId != null ? identityId.hashCode() : 0);
                result = 31 * result + (identityPoolId != null ? identityPoolId.hashCode() : 0);
                return result;
            }

            protected boolean canEqual(Object other) {
                return other instanceof CognitoIdentity;
            }

            @NonNull
            public String toString() {
                return "APIGatewayV2HTTPEvent.RequestContext.CognitoIdentity(amr=" + this.getAmr() + ", identityId=" + this.getIdentityId() + ", identityPoolId=" + this.getIdentityPoolId() + ")";
            }

            public static class CognitoIdentityBuilder {
                @Nullable
                private List<String> amr;

                @Nullable
                private String identityId;

                @Nullable
                private String identityPoolId;

                @NonNull
                public CognitoIdentityBuilder withAmr(@Nullable List<String> amr) {
                    this.amr = amr;
                    return this;
                }

                @NonNull
                public CognitoIdentityBuilder withIdentityId(@Nullable String identityId) {
                    this.identityId = identityId;
                    return this;
                }

                @NonNull
                public CognitoIdentityBuilder withIdentityPoolId(@Nullable String identityPoolId) {
                    this.identityPoolId = identityPoolId;
                    return this;
                }

                @NonNull
                public CognitoIdentity build() {
                    return new CognitoIdentity(this.amr, this.identityId, this.identityPoolId);
                }

                @NonNull
                public String toString() {
                    return "APIGatewayV2HTTPEvent.RequestContext.CognitoIdentity.CognitoIdentityBuilder(amr=" + this.amr + ", identityId=" + this.identityId + ", identityPoolId=" + this.identityPoolId + ")";
                }
            }
        }

        @Serdeable
        public static class IAM {
            @Nullable
            private String accessKey;

            @Nullable
            private String accountId;

            @Nullable
            private String callerId;

            @Nullable
            private CognitoIdentity cognitoIdentity;

            @Nullable
            private String principalOrgId;

            @Nullable
            private String userArn;

            @Nullable
            private String userId;

            public IAM() {
            }

            @Creator
            public IAM(@Nullable String accessKey,
                       @Nullable String accountId,
                       @Nullable String callerId,
                       @Nullable CognitoIdentity cognitoIdentity,
                       @Nullable String principalOrgId,
                       @Nullable String userArn,
                       @Nullable String userId) {
                this.accessKey = accessKey;
                this.accountId = accountId;
                this.callerId = callerId;
                this.cognitoIdentity = cognitoIdentity;
                this.principalOrgId = principalOrgId;
                this.userArn = userArn;
                this.userId = userId;
            }

            @NonNull
            public static IAMBuilder builder() {
                return new IAMBuilder();
            }

            @Nullable
            public String getAccessKey() {
                return this.accessKey;
            }

            @Nullable
            public String getAccountId() {
                return this.accountId;
            }

            @Nullable
            public String getCallerId() {
                return this.callerId;
            }

            @Nullable
            public CognitoIdentity getCognitoIdentity() {
                return this.cognitoIdentity;
            }

            @Nullable
            public String getPrincipalOrgId() {
                return this.principalOrgId;
            }

            @Nullable
            public String getUserArn() {
                return this.userArn;
            }

            @Nullable
            public String getUserId() {
                return this.userId;
            }

            public void setAccessKey(@Nullable String accessKey) {
                this.accessKey = accessKey;
            }

            public void setAccountId(@Nullable String accountId) {
                this.accountId = accountId;
            }

            public void setCallerId(@Nullable String callerId) {
                this.callerId = callerId;
            }

            public void setCognitoIdentity(@Nullable CognitoIdentity cognitoIdentity) {
                this.cognitoIdentity = cognitoIdentity;
            }

            public void setPrincipalOrgId(@Nullable String principalOrgId) {
                this.principalOrgId = principalOrgId;
            }

            public void setUserArn(@Nullable String userArn) {
                this.userArn = userArn;
            }

            public void setUserId(@Nullable String userId) {
                this.userId = userId;
            }

            protected boolean canEqual(Object other) {
                return other instanceof IAM;
            }

            @NonNull
            public String toString() {
                return "APIGatewayV2HTTPEvent.RequestContext.IAM(accessKey=" + this.getAccessKey() + ", accountId=" + this.getAccountId() + ", callerId=" + this.getCallerId() + ", cognitoIdentity=" + this.getCognitoIdentity() + ", principalOrgId=" + this.getPrincipalOrgId() + ", userArn=" + this.getUserArn() + ", userId=" + this.getUserId() + ")";
            }

            public static class IAMBuilder {

                @Nullable
                private String accessKey;

                @Nullable
                private String accountId;

                @Nullable
                private String callerId;

                @Nullable
                private CognitoIdentity cognitoIdentity;

                @Nullable
                private String principalOrgId;

                @Nullable
                private String userArn;

                @Nullable
                private String userId;

                @NonNull
                public IAMBuilder withAccessKey(@Nullable String accessKey) {
                    this.accessKey = accessKey;
                    return this;
                }

                @NonNull public IAMBuilder withAccountId(@Nullable String accountId) {
                    this.accountId = accountId;
                    return this;
                }

                @NonNull
                public IAMBuilder withCallerId(@Nullable String callerId) {
                    this.callerId = callerId;
                    return this;
                }

                @NonNull
                public IAMBuilder withCognitoIdentity(@Nullable CognitoIdentity cognitoIdentity) {
                    this.cognitoIdentity = cognitoIdentity;
                    return this;
                }

                @NonNull
                public IAMBuilder withPrincipalOrgId(@Nullable String principalOrgId) {
                    this.principalOrgId = principalOrgId;
                    return this;
                }

                @NonNull
                public IAMBuilder withUserArn(@Nullable String userArn) {
                    this.userArn = userArn;
                    return this;
                }

                @NonNull
                public IAMBuilder withUserId(@Nullable String userId) {
                    this.userId = userId;
                    return this;
                }

                @NonNull
                public IAM build() {
                    return new IAM(this.accessKey, this.accountId, this.callerId, this.cognitoIdentity, this.principalOrgId, this.userArn, this.userId);
                }

                @NonNull
                public String toString() {
                    return "APIGatewayV2HTTPEvent.RequestContext.IAM.IAMBuilder(accessKey=" + this.accessKey + ", accountId=" + this.accountId + ", callerId=" + this.callerId + ", cognitoIdentity=" + this.cognitoIdentity + ", principalOrgId=" + this.principalOrgId + ", userArn=" + this.userArn + ", userId=" + this.userId + ")";
                }
            }
        }

        @Serdeable
        public static class Http {

            @Nullable
            private String method;

            @Nullable
            private String path;

            @Nullable
            private String protocol;

            @Nullable
            private String sourceIp;

            @Nullable
            private String userAgent;

            public Http() {
            }

            @Creator
            public Http(@Nullable String method,
                        @Nullable String path,
                        @Nullable String protocol,
                        @Nullable String sourceIp,
                        @Nullable String userAgent) {
                this.method = method;
                this.path = path;
                this.protocol = protocol;
                this.sourceIp = sourceIp;
                this.userAgent = userAgent;
            }

            @NonNull
            public static HttpBuilder builder() {
                return new HttpBuilder();
            }

            @NonNull
            public String getMethod() {
                return this.method;
            }

            @NonNull
            public String getPath() {
                return this.path;
            }

            @NonNull
            public String getProtocol() {
                return this.protocol;
            }

            @NonNull
            public String getSourceIp() {
                return this.sourceIp;
            }

            @NonNull
            public String getUserAgent() {
                return this.userAgent;
            }

            public void setMethod(@NonNull String method) {
                this.method = method;
            }

            public void setPath(@NonNull String path) {
                this.path = path;
            }

            public void setProtocol(@NonNull String protocol) {
                this.protocol = protocol;
            }

            public void setSourceIp(@NonNull String sourceIp) {
                this.sourceIp = sourceIp;
            }

            public void setUserAgent(@NonNull String userAgent) {
                this.userAgent = userAgent;
            }

            protected boolean canEqual(Object other) {
                return other instanceof Http;
            }

            @Override
            public boolean equals(Object o) {
                if (this == o) {
                    return true;
                }
                if (o == null || getClass() != o.getClass()) {
                    return false;
                }

                Http http = (Http) o;

                if (!Objects.equals(method, http.method)) {
                    return false;
                }
                if (!Objects.equals(path, http.path)) {
                    return false;
                }
                if (!Objects.equals(protocol, http.protocol)) {
                    return false;
                }
                if (!Objects.equals(sourceIp, http.sourceIp)) {
                    return false;
                }
                return Objects.equals(userAgent, http.userAgent);
            }

            @Override
            public int hashCode() {
                int result = method != null ? method.hashCode() : 0;
                result = 31 * result + (path != null ? path.hashCode() : 0);
                result = 31 * result + (protocol != null ? protocol.hashCode() : 0);
                result = 31 * result + (sourceIp != null ? sourceIp.hashCode() : 0);
                result = 31 * result + (userAgent != null ? userAgent.hashCode() : 0);
                return result;
            }

            @NonNull
            public String toString() {
                return "APIGatewayV2HTTPEvent.RequestContext.Http(method=" + this.getMethod() + ", path=" + this.getPath() + ", protocol=" + this.getProtocol() + ", sourceIp=" + this.getSourceIp() + ", userAgent=" + this.getUserAgent() + ")";
            }

            public static class HttpBuilder {
                @Nullable
                private String method;

                @Nullable
                private String path;

                @Nullable
                private String protocol;

                @Nullable
                private String sourceIp;

                @Nullable
                private String userAgent;

                @NonNull
                public HttpBuilder withMethod(@Nullable String method) {
                    this.method = method;
                    return this;
                }

                @NonNull
                public HttpBuilder withPath(@Nullable String path) {
                    this.path = path;
                    return this;
                }

                @NonNull
                public HttpBuilder withProtocol(@Nullable String protocol) {
                    this.protocol = protocol;
                    return this;
                }

                @NonNull
                public HttpBuilder withSourceIp(@Nullable String sourceIp) {
                    this.sourceIp = sourceIp;
                    return this;
                }

                @NonNull
                public HttpBuilder withUserAgent(@Nullable String userAgent) {
                    this.userAgent = userAgent;
                    return this;
                }

                @NonNull
                public Http build() {
                    return new Http(this.method, this.path, this.protocol, this.sourceIp, this.userAgent);
                }

                @NonNull
                public String toString() {
                    return "APIGatewayV2HTTPEvent.RequestContext.Http.HttpBuilder(method=" + this.method + ", path=" + this.path + ", protocol=" + this.protocol + ", sourceIp=" + this.sourceIp + ", userAgent=" + this.userAgent + ")";
                }
            }
        }

        @Serdeable
        public static class Authorizer {
            @Nullable
            private JWT jwt;
            @Nullable
            private Map<String, Object> lambda;
            @Nullable
            private IAM iam;

            public Authorizer() {
            }

            @Creator
            public Authorizer(@Nullable JWT jwt,
                              @Nullable Map<String, Object> lambda,
                              @Nullable IAM iam) {
                this.jwt = jwt;
                this.lambda = lambda;
                this.iam = iam;
            }

            @NonNull
            public static AuthorizerBuilder builder() {
                return new AuthorizerBuilder();
            }

            @Nullable
            public JWT getJwt() {
                return this.jwt;
            }

            @Nullable
            public Map<String, Object> getLambda() {
                return this.lambda;
            }

            @Nullable
            public IAM getIam() {
                return this.iam;
            }

            public void setJwt(@Nullable JWT jwt) {
                this.jwt = jwt;
            }

            public void setLambda(@Nullable Map<String, Object> lambda) {
                this.lambda = lambda;
            }

            public void setIam(@Nullable IAM iam) {
                this.iam = iam;
            }

            @Override
            public boolean equals(Object o) {
                if (this == o) {
                    return true;
                }
                if (o == null || getClass() != o.getClass()) {
                    return false;
                }

                Authorizer that = (Authorizer) o;

                if (!Objects.equals(jwt, that.jwt)) {
                    return false;
                }
                if (!Objects.equals(lambda, that.lambda)) {
                    return false;
                }
                return Objects.equals(iam, that.iam);
            }

            @Override
            public int hashCode() {
                int result = jwt != null ? jwt.hashCode() : 0;
                result = 31 * result + (lambda != null ? lambda.hashCode() : 0);
                result = 31 * result + (iam != null ? iam.hashCode() : 0);
                return result;
            }

            protected boolean canEqual(Object other) {
                return other instanceof Authorizer;
            }

            @NonNull
            public String toString() {
                return "APIGatewayV2HTTPEvent.RequestContext.Authorizer(jwt=" + this.getJwt() + ", lambda=" + this.getLambda() + ", iam=" + this.getIam() + ")";
            }

            public static class AuthorizerBuilder {
                @Nullable
                private JWT jwt;

                @Nullable
                private Map<String, Object> lambda;

                @Nullable
                private IAM iam;

                @NonNull
                public AuthorizerBuilder withJwt(@Nullable JWT jwt) {
                    this.jwt = jwt;
                    return this;
                }

                @NonNull
                public AuthorizerBuilder withLambda(@Nullable Map<String, Object> lambda) {
                    this.lambda = lambda;
                    return this;
                }

                @NonNull
                public AuthorizerBuilder withIam(@Nullable IAM iam) {
                    this.iam = iam;
                    return this;
                }

                @NonNull
                public Authorizer build() {
                    return new Authorizer(this.jwt, this.lambda, this.iam);
                }

                @NonNull
                public String toString() {
                    return "APIGatewayV2HTTPEvent.RequestContext.Authorizer.AuthorizerBuilder(jwt=" + this.jwt + ", lambda=" + this.lambda + ", iam=" + this.iam + ")";
                }
            }

            @Serdeable
            public static class JWT {
                @Nullable
                private Map<String, String> claims;

                @Nullable
                private List<String> scopes;

                @Creator
                public JWT(@Nullable Map<String, String> claims, @Nullable List<String> scopes) {
                    this.claims = claims;
                    this.scopes = scopes;
                }

                public JWT() {
                }

                @NonNull
                public static JWTBuilder builder() {
                    return new JWTBuilder();
                }

                @Nullable
                public Map<String, String> getClaims() {
                    return this.claims;
                }

                @Nullable
                public List<String> getScopes() {
                    return this.scopes;
                }

                public void setClaims(@Nullable Map<String, String> claims) {
                    this.claims = claims;
                }

                public void setScopes(@Nullable List<String> scopes) {
                    this.scopes = scopes;
                }

                protected boolean canEqual(Object other) {
                    return other instanceof JWT;
                }

                @Override
                public boolean equals(Object o) {
                    if (this == o) {
                        return true;
                    }
                    if (o == null || getClass() != o.getClass()) {
                        return false;
                    }

                    JWT jwt = (JWT) o;

                    if (!Objects.equals(claims, jwt.claims)) {
                        return false;
                    }
                    return Objects.equals(scopes, jwt.scopes);
                }

                @Override
                public int hashCode() {
                    int result = claims != null ? claims.hashCode() : 0;
                    result = 31 * result + (scopes != null ? scopes.hashCode() : 0);
                    return result;
                }

                @NonNull
                public String toString() {
                    return "APIGatewayV2HTTPEvent.RequestContext.Authorizer.JWT(claims=" + this.getClaims() + ", scopes=" + this.getScopes() + ")";
                }

                public static class JWTBuilder {
                    @Nullable
                    private Map<String, String> claims;

                    @Nullable
                    private List<String> scopes;

                    @NonNull
                    public JWTBuilder withClaims(@Nullable Map<String, String> claims) {
                        this.claims = claims;
                        return this;
                    }

                    @NonNull
                    public JWTBuilder withScopes(@Nullable List<String> scopes) {
                        this.scopes = scopes;
                        return this;
                    }

                    @NonNull
                    public JWT build() {
                        return new JWT(this.claims, this.scopes);
                    }

                    @NonNull
                    public String toString() {
                        return "APIGatewayV2HTTPEvent.RequestContext.Authorizer.JWT.JWTBuilder(claims=" + this.claims + ", scopes=" + this.scopes + ")";
                    }
                }
            }
        }
    }
}
