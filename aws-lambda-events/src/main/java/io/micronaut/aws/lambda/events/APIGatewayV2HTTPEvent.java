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
import io.micronaut.core.annotation.Nullable;
import io.micronaut.serde.annotation.Serdeable;

import java.util.List;
import java.util.Map;

@Serdeable
public class APIGatewayV2HTTPEvent {
    private String version;
    private String routeKey;
    private String rawPath;
    private String rawQueryString;

    @Nullable
    private List<String> cookies;
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
    private RequestContext requestContext;

    public static APIGatewayV2HTTPEventBuilder builder() {
        return new APIGatewayV2HTTPEventBuilder();
    }

    @Creator
    public APIGatewayV2HTTPEvent(String version, String routeKey, String rawPath, String rawQueryString, @Nullable List<String> cookies, Map<String, String> headers, @Nullable Map<String, String> queryStringParameters, @Nullable Map<String, String> pathParameters, @Nullable Map<String, String> stageVariables, @Nullable String body, boolean isBase64Encoded, RequestContext requestContext) {
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

    public String getVersion() {
        return this.version;
    }

    public String getRouteKey() {
        return this.routeKey;
    }

    public String getRawPath() {
        return this.rawPath;
    }

    public String getRawQueryString() {
        return this.rawQueryString;
    }

    @Nullable
    public List<String> getCookies() {
        return this.cookies;
    }

    public Map<String, String> getHeaders() {
        return this.headers;
    }

    public Map<String, String> getQueryStringParameters() {
        return this.queryStringParameters;
    }

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

    public RequestContext getRequestContext() {
        return this.requestContext;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public void setRouteKey(String routeKey) {
        this.routeKey = routeKey;
    }

    public void setRawPath(String rawPath) {
        this.rawPath = rawPath;
    }

    public void setRawQueryString(String rawQueryString) {
        this.rawQueryString = rawQueryString;
    }

    public void setCookies(@Nullable List<String> cookies) {
        this.cookies = cookies;
    }

    public void setHeaders(Map<String, String> headers) {
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

    public void setRequestContext(RequestContext requestContext) {
        this.requestContext = requestContext;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (!(o instanceof APIGatewayV2HTTPEvent)) {
            return false;
        } else {
            APIGatewayV2HTTPEvent other = (APIGatewayV2HTTPEvent)o;
            if (!other.canEqual(this)) {
                return false;
            } else if (this.getIsBase64Encoded() != other.getIsBase64Encoded()) {
                return false;
            } else {
                label145: {
                    Object this$version = this.getVersion();
                    Object other$version = other.getVersion();
                    if (this$version == null) {
                        if (other$version == null) {
                            break label145;
                        }
                    } else if (this$version.equals(other$version)) {
                        break label145;
                    }

                    return false;
                }

                Object this$routeKey = this.getRouteKey();
                Object other$routeKey = other.getRouteKey();
                if (this$routeKey == null) {
                    if (other$routeKey != null) {
                        return false;
                    }
                } else if (!this$routeKey.equals(other$routeKey)) {
                    return false;
                }

                Object this$rawPath = this.getRawPath();
                Object other$rawPath = other.getRawPath();
                if (this$rawPath == null) {
                    if (other$rawPath != null) {
                        return false;
                    }
                } else if (!this$rawPath.equals(other$rawPath)) {
                    return false;
                }

                label124: {
                    Object this$rawQueryString = this.getRawQueryString();
                    Object other$rawQueryString = other.getRawQueryString();
                    if (this$rawQueryString == null) {
                        if (other$rawQueryString == null) {
                            break label124;
                        }
                    } else if (this$rawQueryString.equals(other$rawQueryString)) {
                        break label124;
                    }

                    return false;
                }

                Object this$cookies = this.getCookies();
                Object other$cookies = other.getCookies();
                if (this$cookies == null) {
                    if (other$cookies != null) {
                        return false;
                    }
                } else if (!this$cookies.equals(other$cookies)) {
                    return false;
                }

                Object this$headers = this.getHeaders();
                Object other$headers = other.getHeaders();
                if (this$headers == null) {
                    if (other$headers != null) {
                        return false;
                    }
                } else if (!this$headers.equals(other$headers)) {
                    return false;
                }

                label103: {
                    Object this$queryStringParameters = this.getQueryStringParameters();
                    Object other$queryStringParameters = other.getQueryStringParameters();
                    if (this$queryStringParameters == null) {
                        if (other$queryStringParameters == null) {
                            break label103;
                        }
                    } else if (this$queryStringParameters.equals(other$queryStringParameters)) {
                        break label103;
                    }

                    return false;
                }

                Object this$pathParameters = this.getPathParameters();
                Object other$pathParameters = other.getPathParameters();
                if (this$pathParameters == null) {
                    if (other$pathParameters != null) {
                        return false;
                    }
                } else if (!this$pathParameters.equals(other$pathParameters)) {
                    return false;
                }

                label89: {
                    Object this$stageVariables = this.getStageVariables();
                    Object other$stageVariables = other.getStageVariables();
                    if (this$stageVariables == null) {
                        if (other$stageVariables == null) {
                            break label89;
                        }
                    } else if (this$stageVariables.equals(other$stageVariables)) {
                        break label89;
                    }

                    return false;
                }

                Object this$body = this.getBody();
                Object other$body = other.getBody();
                if (this$body == null) {
                    if (other$body != null) {
                        return false;
                    }
                } else if (!this$body.equals(other$body)) {
                    return false;
                }

                Object this$requestContext = this.getRequestContext();
                Object other$requestContext = other.getRequestContext();
                if (this$requestContext == null) {
                    if (other$requestContext == null) {
                        return true;
                    }
                } else if (this$requestContext.equals(other$requestContext)) {
                    return true;
                }

                return false;
            }
        }
    }

    protected boolean canEqual(Object other) {
        return other instanceof APIGatewayV2HTTPEvent;
    }

    public int hashCode() {
        int result = 1;
        result = result * 59 + (this.getIsBase64Encoded() ? 79 : 97);
        Object $version = this.getVersion();
        result = result * 59 + ($version == null ? 43 : $version.hashCode());
        Object $routeKey = this.getRouteKey();
        result = result * 59 + ($routeKey == null ? 43 : $routeKey.hashCode());
        Object $rawPath = this.getRawPath();
        result = result * 59 + ($rawPath == null ? 43 : $rawPath.hashCode());
        Object $rawQueryString = this.getRawQueryString();
        result = result * 59 + ($rawQueryString == null ? 43 : $rawQueryString.hashCode());
        Object $cookies = this.getCookies();
        result = result * 59 + ($cookies == null ? 43 : $cookies.hashCode());
        Object $headers = this.getHeaders();
        result = result * 59 + ($headers == null ? 43 : $headers.hashCode());
        Object $queryStringParameters = this.getQueryStringParameters();
        result = result * 59 + ($queryStringParameters == null ? 43 : $queryStringParameters.hashCode());
        Object $pathParameters = this.getPathParameters();
        result = result * 59 + ($pathParameters == null ? 43 : $pathParameters.hashCode());
        Object $stageVariables = this.getStageVariables();
        result = result * 59 + ($stageVariables == null ? 43 : $stageVariables.hashCode());
        Object $body = this.getBody();
        result = result * 59 + ($body == null ? 43 : $body.hashCode());
        Object $requestContext = this.getRequestContext();
        result = result * 59 + ($requestContext == null ? 43 : $requestContext.hashCode());
        return result;
    }

    public String toString() {
        return "APIGatewayV2HTTPEvent(version=" + this.getVersion() + ", routeKey=" + this.getRouteKey() + ", rawPath=" + this.getRawPath() + ", rawQueryString=" + this.getRawQueryString() + ", cookies=" + this.getCookies() + ", headers=" + this.getHeaders() + ", queryStringParameters=" + this.getQueryStringParameters() + ", pathParameters=" + this.getPathParameters() + ", stageVariables=" + this.getStageVariables() + ", body=" + this.getBody() + ", isBase64Encoded=" + this.getIsBase64Encoded() + ", requestContext=" + this.getRequestContext() + ")";
    }

    public APIGatewayV2HTTPEvent() {
    }

    public static class APIGatewayV2HTTPEventBuilder {
        private String version;
        private String routeKey;
        private String rawPath;
        private String rawQueryString;
        private List<String> cookies;
        private Map<String, String> headers;
        private Map<String, String> queryStringParameters;

        @Nullable
        private Map<String, String> pathParameters;
        private Map<String, String> stageVariables;
        private String body;
        private boolean isBase64Encoded;
        private RequestContext requestContext;

        APIGatewayV2HTTPEventBuilder() {
        }

        public APIGatewayV2HTTPEventBuilder withVersion(String version) {
            this.version = version;
            return this;
        }

        public APIGatewayV2HTTPEventBuilder withRouteKey(String routeKey) {
            this.routeKey = routeKey;
            return this;
        }

        public APIGatewayV2HTTPEventBuilder withRawPath(String rawPath) {
            this.rawPath = rawPath;
            return this;
        }

        public APIGatewayV2HTTPEventBuilder withRawQueryString(String rawQueryString) {
            this.rawQueryString = rawQueryString;
            return this;
        }

        public APIGatewayV2HTTPEventBuilder withCookies(List<String> cookies) {
            this.cookies = cookies;
            return this;
        }

        public APIGatewayV2HTTPEventBuilder withHeaders(Map<String, String> headers) {
            this.headers = headers;
            return this;
        }

        public APIGatewayV2HTTPEventBuilder withQueryStringParameters(Map<String, String> queryStringParameters) {
            this.queryStringParameters = queryStringParameters;
            return this;
        }

        public APIGatewayV2HTTPEventBuilder withPathParameters(Map<String, String> pathParameters) {
            this.pathParameters = pathParameters;
            return this;
        }

        public APIGatewayV2HTTPEventBuilder withStageVariables(Map<String, String> stageVariables) {
            this.stageVariables = stageVariables;
            return this;
        }

        public APIGatewayV2HTTPEventBuilder withBody(String body) {
            this.body = body;
            return this;
        }

        public APIGatewayV2HTTPEventBuilder withIsBase64Encoded(boolean isBase64Encoded) {
            this.isBase64Encoded = isBase64Encoded;
            return this;
        }

        public APIGatewayV2HTTPEventBuilder withRequestContext(RequestContext requestContext) {
            this.requestContext = requestContext;
            return this;
        }

        public APIGatewayV2HTTPEvent build() {
            return new APIGatewayV2HTTPEvent(this.version, this.routeKey, this.rawPath, this.rawQueryString, this.cookies, this.headers, this.queryStringParameters, this.pathParameters, this.stageVariables, this.body, this.isBase64Encoded, this.requestContext);
        }

        public String toString() {
            return "APIGatewayV2HTTPEvent.APIGatewayV2HTTPEventBuilder(version=" + this.version + ", routeKey=" + this.routeKey + ", rawPath=" + this.rawPath + ", rawQueryString=" + this.rawQueryString + ", cookies=" + this.cookies + ", headers=" + this.headers + ", queryStringParameters=" + this.queryStringParameters + ", pathParameters=" + this.pathParameters + ", stageVariables=" + this.stageVariables + ", body=" + this.body + ", isBase64Encoded=" + this.isBase64Encoded + ", requestContext=" + this.requestContext + ")";
        }
    }

    @Serdeable
    public static class RequestContext {
        private String routeKey;
        private String accountId;
        private String stage;
        private String apiId;
        private String domainName;
        private String domainPrefix;
        private String time;
        private long timeEpoch;
        private Http http;

        @Nullable
        private Authorizer authorizer;
        private String requestId;

        public static RequestContextBuilder builder() {
            return new RequestContextBuilder();
        }

        @Creator
        public RequestContext(String routeKey, String accountId, String stage, String apiId, String domainName, String domainPrefix, String time, long timeEpoch, Http http, @Nullable Authorizer authorizer, String requestId) {
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

        public String getRouteKey() {
            return this.routeKey;
        }

        public String getAccountId() {
            return this.accountId;
        }

        public String getStage() {
            return this.stage;
        }

        public String getApiId() {
            return this.apiId;
        }

        public String getDomainName() {
            return this.domainName;
        }

        public String getDomainPrefix() {
            return this.domainPrefix;
        }

        public String getTime() {
            return this.time;
        }

        public long getTimeEpoch() {
            return this.timeEpoch;
        }

        public Http getHttp() {
            return this.http;
        }

        @Nullable
        public Authorizer getAuthorizer() {
            return this.authorizer;
        }

        public String getRequestId() {
            return this.requestId;
        }

        public void setRouteKey(String routeKey) {
            this.routeKey = routeKey;
        }

        public void setAccountId(String accountId) {
            this.accountId = accountId;
        }

        public void setStage(String stage) {
            this.stage = stage;
        }

        public void setApiId(String apiId) {
            this.apiId = apiId;
        }

        public void setDomainName(String domainName) {
            this.domainName = domainName;
        }

        public void setDomainPrefix(String domainPrefix) {
            this.domainPrefix = domainPrefix;
        }

        public void setTime(String time) {
            this.time = time;
        }

        public void setTimeEpoch(long timeEpoch) {
            this.timeEpoch = timeEpoch;
        }

        public void setHttp(Http http) {
            this.http = http;
        }

        public void setAuthorizer(@Nullable Authorizer authorizer) {
            this.authorizer = authorizer;
        }

        public void setRequestId(String requestId) {
            this.requestId = requestId;
        }

        public boolean equals(Object o) {
            if (o == this) {
                return true;
            } else if (!(o instanceof RequestContext)) {
                return false;
            } else {
                RequestContext other = (RequestContext)o;
                if (!other.canEqual(this)) {
                    return false;
                } else if (this.getTimeEpoch() != other.getTimeEpoch()) {
                    return false;
                } else {
                    Object this$routeKey = this.getRouteKey();
                    Object other$routeKey = other.getRouteKey();
                    if (this$routeKey == null) {
                        if (other$routeKey != null) {
                            return false;
                        }
                    } else if (!this$routeKey.equals(other$routeKey)) {
                        return false;
                    }

                    Object this$accountId = this.getAccountId();
                    Object other$accountId = other.getAccountId();
                    if (this$accountId == null) {
                        if (other$accountId != null) {
                            return false;
                        }
                    } else if (!this$accountId.equals(other$accountId)) {
                        return false;
                    }

                    label119: {
                        Object this$stage = this.getStage();
                        Object other$stage = other.getStage();
                        if (this$stage == null) {
                            if (other$stage == null) {
                                break label119;
                            }
                        } else if (this$stage.equals(other$stage)) {
                            break label119;
                        }

                        return false;
                    }

                    label112: {
                        Object this$apiId = this.getApiId();
                        Object other$apiId = other.getApiId();
                        if (this$apiId == null) {
                            if (other$apiId == null) {
                                break label112;
                            }
                        } else if (this$apiId.equals(other$apiId)) {
                            break label112;
                        }

                        return false;
                    }

                    Object this$domainName = this.getDomainName();
                    Object other$domainName = other.getDomainName();
                    if (this$domainName == null) {
                        if (other$domainName != null) {
                            return false;
                        }
                    } else if (!this$domainName.equals(other$domainName)) {
                        return false;
                    }

                    Object this$domainPrefix = this.getDomainPrefix();
                    Object other$domainPrefix = other.getDomainPrefix();
                    if (this$domainPrefix == null) {
                        if (other$domainPrefix != null) {
                            return false;
                        }
                    } else if (!this$domainPrefix.equals(other$domainPrefix)) {
                        return false;
                    }

                    label91: {
                        Object this$time = this.getTime();
                        Object other$time = other.getTime();
                        if (this$time == null) {
                            if (other$time == null) {
                                break label91;
                            }
                        } else if (this$time.equals(other$time)) {
                            break label91;
                        }

                        return false;
                    }

                    Object this$http = this.getHttp();
                    Object other$http = other.getHttp();
                    if (this$http == null) {
                        if (other$http != null) {
                            return false;
                        }
                    } else if (!this$http.equals(other$http)) {
                        return false;
                    }

                    Object this$authorizer = this.getAuthorizer();
                    Object other$authorizer = other.getAuthorizer();
                    if (this$authorizer == null) {
                        if (other$authorizer != null) {
                            return false;
                        }
                    } else if (!this$authorizer.equals(other$authorizer)) {
                        return false;
                    }

                    Object this$requestId = this.getRequestId();
                    Object other$requestId = other.getRequestId();
                    if (this$requestId == null) {
                        if (other$requestId != null) {
                            return false;
                        }
                    } else if (!this$requestId.equals(other$requestId)) {
                        return false;
                    }

                    return true;
                }
            }
        }

        protected boolean canEqual(Object other) {
            return other instanceof RequestContext;
        }

        public int hashCode() {
            int result = 1;
            long $timeEpoch = this.getTimeEpoch();
            result = result * 59 + (int)($timeEpoch >>> 32 ^ $timeEpoch);
            Object $routeKey = this.getRouteKey();
            result = result * 59 + ($routeKey == null ? 43 : $routeKey.hashCode());
            Object $accountId = this.getAccountId();
            result = result * 59 + ($accountId == null ? 43 : $accountId.hashCode());
            Object $stage = this.getStage();
            result = result * 59 + ($stage == null ? 43 : $stage.hashCode());
            Object $apiId = this.getApiId();
            result = result * 59 + ($apiId == null ? 43 : $apiId.hashCode());
            Object $domainName = this.getDomainName();
            result = result * 59 + ($domainName == null ? 43 : $domainName.hashCode());
            Object $domainPrefix = this.getDomainPrefix();
            result = result * 59 + ($domainPrefix == null ? 43 : $domainPrefix.hashCode());
            Object $time = this.getTime();
            result = result * 59 + ($time == null ? 43 : $time.hashCode());
            Object $http = this.getHttp();
            result = result * 59 + ($http == null ? 43 : $http.hashCode());
            Object $authorizer = this.getAuthorizer();
            result = result * 59 + ($authorizer == null ? 43 : $authorizer.hashCode());
            Object $requestId = this.getRequestId();
            result = result * 59 + ($requestId == null ? 43 : $requestId.hashCode());
            return result;
        }

        public String toString() {
            return "APIGatewayV2HTTPEvent.RequestContext(routeKey=" + this.getRouteKey() + ", accountId=" + this.getAccountId() + ", stage=" + this.getStage() + ", apiId=" + this.getApiId() + ", domainName=" + this.getDomainName() + ", domainPrefix=" + this.getDomainPrefix() + ", time=" + this.getTime() + ", timeEpoch=" + this.getTimeEpoch() + ", http=" + this.getHttp() + ", authorizer=" + this.getAuthorizer() + ", requestId=" + this.getRequestId() + ")";
        }

        public RequestContext() {
        }

        public static class RequestContextBuilder {
            private String routeKey;
            private String accountId;
            private String stage;
            private String apiId;
            private String domainName;
            private String domainPrefix;
            private String time;
            private long timeEpoch;
            private Http http;
            private Authorizer authorizer;
            private String requestId;

            RequestContextBuilder() {
            }

            public RequestContextBuilder withRouteKey(String routeKey) {
                this.routeKey = routeKey;
                return this;
            }

            public RequestContextBuilder withAccountId(String accountId) {
                this.accountId = accountId;
                return this;
            }

            public RequestContextBuilder withStage(String stage) {
                this.stage = stage;
                return this;
            }

            public RequestContextBuilder withApiId(String apiId) {
                this.apiId = apiId;
                return this;
            }

            public RequestContextBuilder withDomainName(String domainName) {
                this.domainName = domainName;
                return this;
            }

            public RequestContextBuilder withDomainPrefix(String domainPrefix) {
                this.domainPrefix = domainPrefix;
                return this;
            }

            public RequestContextBuilder withTime(String time) {
                this.time = time;
                return this;
            }

            public RequestContextBuilder withTimeEpoch(long timeEpoch) {
                this.timeEpoch = timeEpoch;
                return this;
            }

            public RequestContextBuilder withHttp(Http http) {
                this.http = http;
                return this;
            }

            public RequestContextBuilder withAuthorizer(Authorizer authorizer) {
                this.authorizer = authorizer;
                return this;
            }

            public RequestContextBuilder withRequestId(String requestId) {
                this.requestId = requestId;
                return this;
            }

            public RequestContext build() {
                return new RequestContext(this.routeKey, this.accountId, this.stage, this.apiId, this.domainName, this.domainPrefix, this.time, this.timeEpoch, this.http, this.authorizer, this.requestId);
            }

            public String toString() {
                return "APIGatewayV2HTTPEvent.RequestContext.RequestContextBuilder(routeKey=" + this.routeKey + ", accountId=" + this.accountId + ", stage=" + this.stage + ", apiId=" + this.apiId + ", domainName=" + this.domainName + ", domainPrefix=" + this.domainPrefix + ", time=" + this.time + ", timeEpoch=" + this.timeEpoch + ", http=" + this.http + ", authorizer=" + this.authorizer + ", requestId=" + this.requestId + ")";
            }
        }

        @Serdeable
        public static class CognitoIdentity {
            private List<String> amr;
            private String identityId;
            private String identityPoolId;

            public static CognitoIdentityBuilder builder() {
                return new CognitoIdentityBuilder();
            }

            @Creator
            public CognitoIdentity(List<String> amr, String identityId, String identityPoolId) {
                this.amr = amr;
                this.identityId = identityId;
                this.identityPoolId = identityPoolId;
            }

            public List<String> getAmr() {
                return this.amr;
            }

            public String getIdentityId() {
                return this.identityId;
            }

            public String getIdentityPoolId() {
                return this.identityPoolId;
            }

            public void setAmr(List<String> amr) {
                this.amr = amr;
            }

            public void setIdentityId(String identityId) {
                this.identityId = identityId;
            }

            public void setIdentityPoolId(String identityPoolId) {
                this.identityPoolId = identityPoolId;
            }

            public boolean equals(Object o) {
                if (o == this) {
                    return true;
                } else if (!(o instanceof CognitoIdentity)) {
                    return false;
                } else {
                    CognitoIdentity other = (CognitoIdentity)o;
                    if (!other.canEqual(this)) {
                        return false;
                    } else {
                        label47: {
                            Object this$amr = this.getAmr();
                            Object other$amr = other.getAmr();
                            if (this$amr == null) {
                                if (other$amr == null) {
                                    break label47;
                                }
                            } else if (this$amr.equals(other$amr)) {
                                break label47;
                            }

                            return false;
                        }

                        Object this$identityId = this.getIdentityId();
                        Object other$identityId = other.getIdentityId();
                        if (this$identityId == null) {
                            if (other$identityId != null) {
                                return false;
                            }
                        } else if (!this$identityId.equals(other$identityId)) {
                            return false;
                        }

                        Object this$identityPoolId = this.getIdentityPoolId();
                        Object other$identityPoolId = other.getIdentityPoolId();
                        if (this$identityPoolId == null) {
                            if (other$identityPoolId != null) {
                                return false;
                            }
                        } else if (!this$identityPoolId.equals(other$identityPoolId)) {
                            return false;
                        }

                        return true;
                    }
                }
            }

            protected boolean canEqual(Object other) {
                return other instanceof CognitoIdentity;
            }

            public int hashCode() {
                int result = 1;
                Object $amr = this.getAmr();
                result = result * 59 + ($amr == null ? 43 : $amr.hashCode());
                Object $identityId = this.getIdentityId();
                result = result * 59 + ($identityId == null ? 43 : $identityId.hashCode());
                Object $identityPoolId = this.getIdentityPoolId();
                result = result * 59 + ($identityPoolId == null ? 43 : $identityPoolId.hashCode());
                return result;
            }

            public String toString() {
                return "APIGatewayV2HTTPEvent.RequestContext.CognitoIdentity(amr=" + this.getAmr() + ", identityId=" + this.getIdentityId() + ", identityPoolId=" + this.getIdentityPoolId() + ")";
            }

            public CognitoIdentity() {
            }

            public static class CognitoIdentityBuilder {
                private List<String> amr;
                private String identityId;
                private String identityPoolId;

                CognitoIdentityBuilder() {
                }

                public CognitoIdentityBuilder withAmr(List<String> amr) {
                    this.amr = amr;
                    return this;
                }

                public CognitoIdentityBuilder withIdentityId(String identityId) {
                    this.identityId = identityId;
                    return this;
                }

                public CognitoIdentityBuilder withIdentityPoolId(String identityPoolId) {
                    this.identityPoolId = identityPoolId;
                    return this;
                }

                public CognitoIdentity build() {
                    return new CognitoIdentity(this.amr, this.identityId, this.identityPoolId);
                }

                public String toString() {
                    return "APIGatewayV2HTTPEvent.RequestContext.CognitoIdentity.CognitoIdentityBuilder(amr=" + this.amr + ", identityId=" + this.identityId + ", identityPoolId=" + this.identityPoolId + ")";
                }
            }
        }

        @Serdeable
        public static class IAM {
            private String accessKey;
            private String accountId;
            private String callerId;
            private CognitoIdentity cognitoIdentity;
            private String principalOrgId;
            private String userArn;
            private String userId;

            public static IAMBuilder builder() {
                return new IAMBuilder();
            }

            @Creator
            public IAM(String accessKey, String accountId, String callerId, CognitoIdentity cognitoIdentity, String principalOrgId, String userArn, String userId) {
                this.accessKey = accessKey;
                this.accountId = accountId;
                this.callerId = callerId;
                this.cognitoIdentity = cognitoIdentity;
                this.principalOrgId = principalOrgId;
                this.userArn = userArn;
                this.userId = userId;
            }

            public String getAccessKey() {
                return this.accessKey;
            }

            public String getAccountId() {
                return this.accountId;
            }

            public String getCallerId() {
                return this.callerId;
            }

            public CognitoIdentity getCognitoIdentity() {
                return this.cognitoIdentity;
            }

            public String getPrincipalOrgId() {
                return this.principalOrgId;
            }

            public String getUserArn() {
                return this.userArn;
            }

            public String getUserId() {
                return this.userId;
            }

            public void setAccessKey(String accessKey) {
                this.accessKey = accessKey;
            }

            public void setAccountId(String accountId) {
                this.accountId = accountId;
            }

            public void setCallerId(String callerId) {
                this.callerId = callerId;
            }

            public void setCognitoIdentity(CognitoIdentity cognitoIdentity) {
                this.cognitoIdentity = cognitoIdentity;
            }

            public void setPrincipalOrgId(String principalOrgId) {
                this.principalOrgId = principalOrgId;
            }

            public void setUserArn(String userArn) {
                this.userArn = userArn;
            }

            public void setUserId(String userId) {
                this.userId = userId;
            }

            public boolean equals(Object o) {
                if (o == this) {
                    return true;
                } else if (!(o instanceof IAM)) {
                    return false;
                } else {
                    IAM other = (IAM)o;
                    if (!other.canEqual(this)) {
                        return false;
                    } else {
                        label95: {
                            Object this$accessKey = this.getAccessKey();
                            Object other$accessKey = other.getAccessKey();
                            if (this$accessKey == null) {
                                if (other$accessKey == null) {
                                    break label95;
                                }
                            } else if (this$accessKey.equals(other$accessKey)) {
                                break label95;
                            }

                            return false;
                        }

                        Object this$accountId = this.getAccountId();
                        Object other$accountId = other.getAccountId();
                        if (this$accountId == null) {
                            if (other$accountId != null) {
                                return false;
                            }
                        } else if (!this$accountId.equals(other$accountId)) {
                            return false;
                        }

                        Object this$callerId = this.getCallerId();
                        Object other$callerId = other.getCallerId();
                        if (this$callerId == null) {
                            if (other$callerId != null) {
                                return false;
                            }
                        } else if (!this$callerId.equals(other$callerId)) {
                            return false;
                        }

                        label74: {
                            Object this$cognitoIdentity = this.getCognitoIdentity();
                            Object other$cognitoIdentity = other.getCognitoIdentity();
                            if (this$cognitoIdentity == null) {
                                if (other$cognitoIdentity == null) {
                                    break label74;
                                }
                            } else if (this$cognitoIdentity.equals(other$cognitoIdentity)) {
                                break label74;
                            }

                            return false;
                        }

                        label67: {
                            Object this$principalOrgId = this.getPrincipalOrgId();
                            Object other$principalOrgId = other.getPrincipalOrgId();
                            if (this$principalOrgId == null) {
                                if (other$principalOrgId == null) {
                                    break label67;
                                }
                            } else if (this$principalOrgId.equals(other$principalOrgId)) {
                                break label67;
                            }

                            return false;
                        }

                        Object this$userArn = this.getUserArn();
                        Object other$userArn = other.getUserArn();
                        if (this$userArn == null) {
                            if (other$userArn != null) {
                                return false;
                            }
                        } else if (!this$userArn.equals(other$userArn)) {
                            return false;
                        }

                        Object this$userId = this.getUserId();
                        Object other$userId = other.getUserId();
                        if (this$userId == null) {
                            if (other$userId != null) {
                                return false;
                            }
                        } else if (!this$userId.equals(other$userId)) {
                            return false;
                        }

                        return true;
                    }
                }
            }

            protected boolean canEqual(Object other) {
                return other instanceof IAM;
            }

            public int hashCode() {
                int result = 1;
                Object $accessKey = this.getAccessKey();
                result = result * 59 + ($accessKey == null ? 43 : $accessKey.hashCode());
                Object $accountId = this.getAccountId();
                result = result * 59 + ($accountId == null ? 43 : $accountId.hashCode());
                Object $callerId = this.getCallerId();
                result = result * 59 + ($callerId == null ? 43 : $callerId.hashCode());
                Object $cognitoIdentity = this.getCognitoIdentity();
                result = result * 59 + ($cognitoIdentity == null ? 43 : $cognitoIdentity.hashCode());
                Object $principalOrgId = this.getPrincipalOrgId();
                result = result * 59 + ($principalOrgId == null ? 43 : $principalOrgId.hashCode());
                Object $userArn = this.getUserArn();
                result = result * 59 + ($userArn == null ? 43 : $userArn.hashCode());
                Object $userId = this.getUserId();
                result = result * 59 + ($userId == null ? 43 : $userId.hashCode());
                return result;
            }

            public String toString() {
                return "APIGatewayV2HTTPEvent.RequestContext.IAM(accessKey=" + this.getAccessKey() + ", accountId=" + this.getAccountId() + ", callerId=" + this.getCallerId() + ", cognitoIdentity=" + this.getCognitoIdentity() + ", principalOrgId=" + this.getPrincipalOrgId() + ", userArn=" + this.getUserArn() + ", userId=" + this.getUserId() + ")";
            }

            public IAM() {
            }

            public static class IAMBuilder {
                private String accessKey;
                private String accountId;
                private String callerId;
                private CognitoIdentity cognitoIdentity;
                private String principalOrgId;
                private String userArn;
                private String userId;

                IAMBuilder() {
                }

                public IAMBuilder withAccessKey(String accessKey) {
                    this.accessKey = accessKey;
                    return this;
                }

                public IAMBuilder withAccountId(String accountId) {
                    this.accountId = accountId;
                    return this;
                }

                public IAMBuilder withCallerId(String callerId) {
                    this.callerId = callerId;
                    return this;
                }

                public IAMBuilder withCognitoIdentity(CognitoIdentity cognitoIdentity) {
                    this.cognitoIdentity = cognitoIdentity;
                    return this;
                }

                public IAMBuilder withPrincipalOrgId(String principalOrgId) {
                    this.principalOrgId = principalOrgId;
                    return this;
                }

                public IAMBuilder withUserArn(String userArn) {
                    this.userArn = userArn;
                    return this;
                }

                public IAMBuilder withUserId(String userId) {
                    this.userId = userId;
                    return this;
                }

                public IAM build() {
                    return new IAM(this.accessKey, this.accountId, this.callerId, this.cognitoIdentity, this.principalOrgId, this.userArn, this.userId);
                }

                public String toString() {
                    return "APIGatewayV2HTTPEvent.RequestContext.IAM.IAMBuilder(accessKey=" + this.accessKey + ", accountId=" + this.accountId + ", callerId=" + this.callerId + ", cognitoIdentity=" + this.cognitoIdentity + ", principalOrgId=" + this.principalOrgId + ", userArn=" + this.userArn + ", userId=" + this.userId + ")";
                }
            }
        }

        @Serdeable
        public static class Http {
            private String method;
            private String path;
            private String protocol;
            private String sourceIp;
            private String userAgent;

            public static HttpBuilder builder() {
                return new HttpBuilder();
            }

            @Creator
            public Http(String method, String path, String protocol, String sourceIp, String userAgent) {
                this.method = method;
                this.path = path;
                this.protocol = protocol;
                this.sourceIp = sourceIp;
                this.userAgent = userAgent;
            }

            public String getMethod() {
                return this.method;
            }

            public String getPath() {
                return this.path;
            }

            public String getProtocol() {
                return this.protocol;
            }

            public String getSourceIp() {
                return this.sourceIp;
            }

            public String getUserAgent() {
                return this.userAgent;
            }

            public void setMethod(String method) {
                this.method = method;
            }

            public void setPath(String path) {
                this.path = path;
            }

            public void setProtocol(String protocol) {
                this.protocol = protocol;
            }

            public void setSourceIp(String sourceIp) {
                this.sourceIp = sourceIp;
            }

            public void setUserAgent(String userAgent) {
                this.userAgent = userAgent;
            }

            public boolean equals(Object o) {
                if (o == this) {
                    return true;
                } else if (!(o instanceof Http)) {
                    return false;
                } else {
                    Http other = (Http)o;
                    if (!other.canEqual(this)) {
                        return false;
                    } else {
                        label71: {
                            Object this$method = this.getMethod();
                            Object other$method = other.getMethod();
                            if (this$method == null) {
                                if (other$method == null) {
                                    break label71;
                                }
                            } else if (this$method.equals(other$method)) {
                                break label71;
                            }

                            return false;
                        }

                        Object this$path = this.getPath();
                        Object other$path = other.getPath();
                        if (this$path == null) {
                            if (other$path != null) {
                                return false;
                            }
                        } else if (!this$path.equals(other$path)) {
                            return false;
                        }

                        label57: {
                            Object this$protocol = this.getProtocol();
                            Object other$protocol = other.getProtocol();
                            if (this$protocol == null) {
                                if (other$protocol == null) {
                                    break label57;
                                }
                            } else if (this$protocol.equals(other$protocol)) {
                                break label57;
                            }

                            return false;
                        }

                        Object this$sourceIp = this.getSourceIp();
                        Object other$sourceIp = other.getSourceIp();
                        if (this$sourceIp == null) {
                            if (other$sourceIp != null) {
                                return false;
                            }
                        } else if (!this$sourceIp.equals(other$sourceIp)) {
                            return false;
                        }

                        Object this$userAgent = this.getUserAgent();
                        Object other$userAgent = other.getUserAgent();
                        if (this$userAgent == null) {
                            if (other$userAgent == null) {
                                return true;
                            }
                        } else if (this$userAgent.equals(other$userAgent)) {
                            return true;
                        }

                        return false;
                    }
                }
            }

            protected boolean canEqual(Object other) {
                return other instanceof Http;
            }

            public int hashCode() {
                int result = 1;
                Object $method = this.getMethod();
                result = result * 59 + ($method == null ? 43 : $method.hashCode());
                Object $path = this.getPath();
                result = result * 59 + ($path == null ? 43 : $path.hashCode());
                Object $protocol = this.getProtocol();
                result = result * 59 + ($protocol == null ? 43 : $protocol.hashCode());
                Object $sourceIp = this.getSourceIp();
                result = result * 59 + ($sourceIp == null ? 43 : $sourceIp.hashCode());
                Object $userAgent = this.getUserAgent();
                result = result * 59 + ($userAgent == null ? 43 : $userAgent.hashCode());
                return result;
            }

            public String toString() {
                return "APIGatewayV2HTTPEvent.RequestContext.Http(method=" + this.getMethod() + ", path=" + this.getPath() + ", protocol=" + this.getProtocol() + ", sourceIp=" + this.getSourceIp() + ", userAgent=" + this.getUserAgent() + ")";
            }

            public Http() {
            }

            public static class HttpBuilder {
                private String method;
                private String path;
                private String protocol;
                private String sourceIp;
                private String userAgent;

                HttpBuilder() {
                }

                public HttpBuilder withMethod(String method) {
                    this.method = method;
                    return this;
                }

                public HttpBuilder withPath(String path) {
                    this.path = path;
                    return this;
                }

                public HttpBuilder withProtocol(String protocol) {
                    this.protocol = protocol;
                    return this;
                }

                public HttpBuilder withSourceIp(String sourceIp) {
                    this.sourceIp = sourceIp;
                    return this;
                }

                public HttpBuilder withUserAgent(String userAgent) {
                    this.userAgent = userAgent;
                    return this;
                }

                public Http build() {
                    return new Http(this.method, this.path, this.protocol, this.sourceIp, this.userAgent);
                }

                public String toString() {
                    return "APIGatewayV2HTTPEvent.RequestContext.Http.HttpBuilder(method=" + this.method + ", path=" + this.path + ", protocol=" + this.protocol + ", sourceIp=" + this.sourceIp + ", userAgent=" + this.userAgent + ")";
                }
            }
        }

        @Serdeable
        public static class Authorizer {
            private JWT jwt;
            private Map<String, Object> lambda;
            private IAM iam;

            public static AuthorizerBuilder builder() {
                return new AuthorizerBuilder();
            }

            @Creator
            public Authorizer(JWT jwt, Map<String, Object> lambda, IAM iam) {
                this.jwt = jwt;
                this.lambda = lambda;
                this.iam = iam;
            }

            public JWT getJwt() {
                return this.jwt;
            }

            public Map<String, Object> getLambda() {
                return this.lambda;
            }

            public IAM getIam() {
                return this.iam;
            }

            public void setJwt(JWT jwt) {
                this.jwt = jwt;
            }

            public void setLambda(Map<String, Object> lambda) {
                this.lambda = lambda;
            }

            public void setIam(IAM iam) {
                this.iam = iam;
            }

            public boolean equals(Object o) {
                if (o == this) {
                    return true;
                } else if (!(o instanceof Authorizer)) {
                    return false;
                } else {
                    Authorizer other = (Authorizer)o;
                    if (!other.canEqual(this)) {
                        return false;
                    } else {
                        label47: {
                            Object this$jwt = this.getJwt();
                            Object other$jwt = other.getJwt();
                            if (this$jwt == null) {
                                if (other$jwt == null) {
                                    break label47;
                                }
                            } else if (this$jwt.equals(other$jwt)) {
                                break label47;
                            }

                            return false;
                        }

                        Object this$lambda = this.getLambda();
                        Object other$lambda = other.getLambda();
                        if (this$lambda == null) {
                            if (other$lambda != null) {
                                return false;
                            }
                        } else if (!this$lambda.equals(other$lambda)) {
                            return false;
                        }

                        Object this$iam = this.getIam();
                        Object other$iam = other.getIam();
                        if (this$iam == null) {
                            if (other$iam != null) {
                                return false;
                            }
                        } else if (!this$iam.equals(other$iam)) {
                            return false;
                        }

                        return true;
                    }
                }
            }

            protected boolean canEqual(Object other) {
                return other instanceof Authorizer;
            }

            public int hashCode() {
                int result = 1;
                Object $jwt = this.getJwt();
                result = result * 59 + ($jwt == null ? 43 : $jwt.hashCode());
                Object $lambda = this.getLambda();
                result = result * 59 + ($lambda == null ? 43 : $lambda.hashCode());
                Object $iam = this.getIam();
                result = result * 59 + ($iam == null ? 43 : $iam.hashCode());
                return result;
            }

            public String toString() {
                return "APIGatewayV2HTTPEvent.RequestContext.Authorizer(jwt=" + this.getJwt() + ", lambda=" + this.getLambda() + ", iam=" + this.getIam() + ")";
            }

            public Authorizer() {
            }

            public static class AuthorizerBuilder {
                private JWT jwt;
                private Map<String, Object> lambda;
                private IAM iam;

                AuthorizerBuilder() {
                }

                public AuthorizerBuilder withJwt(JWT jwt) {
                    this.jwt = jwt;
                    return this;
                }

                public AuthorizerBuilder withLambda(Map<String, Object> lambda) {
                    this.lambda = lambda;
                    return this;
                }

                public AuthorizerBuilder withIam(IAM iam) {
                    this.iam = iam;
                    return this;
                }

                public Authorizer build() {
                    return new Authorizer(this.jwt, this.lambda, this.iam);
                }

                public String toString() {
                    return "APIGatewayV2HTTPEvent.RequestContext.Authorizer.AuthorizerBuilder(jwt=" + this.jwt + ", lambda=" + this.lambda + ", iam=" + this.iam + ")";
                }
            }

            @Serdeable
            public static class JWT {
                private Map<String, String> claims;
                private List<String> scopes;

                public static JWTBuilder builder() {
                    return new JWTBuilder();
                }

                @Creator
                public JWT(Map<String, String> claims, List<String> scopes) {
                    this.claims = claims;
                    this.scopes = scopes;
                }

                public Map<String, String> getClaims() {
                    return this.claims;
                }

                public List<String> getScopes() {
                    return this.scopes;
                }

                public void setClaims(Map<String, String> claims) {
                    this.claims = claims;
                }

                public void setScopes(List<String> scopes) {
                    this.scopes = scopes;
                }

                public boolean equals(Object o) {
                    if (o == this) {
                        return true;
                    } else if (!(o instanceof JWT)) {
                        return false;
                    } else {
                        JWT other = (JWT)o;
                        if (!other.canEqual(this)) {
                            return false;
                        } else {
                            Object this$claims = this.getClaims();
                            Object other$claims = other.getClaims();
                            if (this$claims == null) {
                                if (other$claims != null) {
                                    return false;
                                }
                            } else if (!this$claims.equals(other$claims)) {
                                return false;
                            }

                            Object this$scopes = this.getScopes();
                            Object other$scopes = other.getScopes();
                            if (this$scopes == null) {
                                if (other$scopes != null) {
                                    return false;
                                }
                            } else if (!this$scopes.equals(other$scopes)) {
                                return false;
                            }

                            return true;
                        }
                    }
                }

                protected boolean canEqual(Object other) {
                    return other instanceof JWT;
                }

                public int hashCode() {
                    int result = 1;
                    Object $claims = this.getClaims();
                    result = result * 59 + ($claims == null ? 43 : $claims.hashCode());
                    Object $scopes = this.getScopes();
                    result = result * 59 + ($scopes == null ? 43 : $scopes.hashCode());
                    return result;
                }

                public String toString() {
                    return "APIGatewayV2HTTPEvent.RequestContext.Authorizer.JWT(claims=" + this.getClaims() + ", scopes=" + this.getScopes() + ")";
                }

                public JWT() {
                }

                public static class JWTBuilder {
                    private Map<String, String> claims;
                    private List<String> scopes;

                    JWTBuilder() {
                    }

                    public JWTBuilder withClaims(Map<String, String> claims) {
                        this.claims = claims;
                        return this;
                    }

                    public JWTBuilder withScopes(List<String> scopes) {
                        this.scopes = scopes;
                        return this;
                    }

                    public JWT build() {
                        return new JWT(this.claims, this.scopes);
                    }

                    public String toString() {
                        return "APIGatewayV2HTTPEvent.RequestContext.Authorizer.JWT.JWTBuilder(claims=" + this.claims + ", scopes=" + this.scopes + ")";
                    }
                }
            }
        }
    }
}
