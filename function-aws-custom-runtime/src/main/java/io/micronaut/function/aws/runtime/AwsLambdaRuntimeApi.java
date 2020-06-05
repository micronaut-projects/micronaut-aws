/*
 * Copyright 2017-2020 original authors
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
package io.micronaut.function.aws.runtime;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.MutableHttpRequest;
import io.micronaut.http.uri.UriTemplate;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;

/**
 * @see <a href="https://docs.aws.amazon.com/lambda/latest/dg/runtimes-api.html">AWS Lambda Runtime Interface</a>
 * @author sdelamo
 * @since 2.0.0
 */
public interface AwsLambdaRuntimeApi {
    String PATH_REQUEST_ID = "requestId";

    UriTemplate INVOCATION_TEMPLATE = UriTemplate.of("/2018-06-01/runtime/invocation/{" + PATH_REQUEST_ID + "}/response");
    UriTemplate ERROR_TEMPLATE = UriTemplate.of("/2018-06-01/runtime/invocation/{" + PATH_REQUEST_ID + "}/error");
    String NEXT_INVOCATION_URI = "/2018-06-01/runtime/invocation/next";
    String INIT_ERROR_URI = "/2018-06-01/runtime/init/error";
    String LAMBDA_RUNTIME_FUNCTION_ERROR_TYPE = "Lambda-Runtime-Function-Error-Type";

    /**
     *
     * @param requestId AWS Lambda Request ID
     * @return invocation response path for given request
     */
    @Nonnull
    default String responseUri(@Nonnull String requestId) {
        return INVOCATION_TEMPLATE.expand(Collections.singletonMap(PATH_REQUEST_ID, requestId));
    }

    /**
     *
     * @param requestId AWS Lambda Request ID
     * @return invocation error path for given request
     */
    @Nonnull
    default String errorUri(@Nonnull String requestId) {
        return ERROR_TEMPLATE.expand(Collections.singletonMap(PATH_REQUEST_ID, requestId));
    }

    /**
     *
     * @param requestId AWS Lambda Request ID
     * @param body The body of the request
     * @return Invocation Response Request
     */
    default HttpRequest invocationResponseRequest(@Nonnull String requestId, Object body) {
        return HttpRequest.POST(responseUri(requestId), body);
    }

    /**
     *
     * @param requestId  Lambda Request Identifier
     * @param errorMessage Error Message
     * @param errorType Error Type
     * @param lambdaFunctionErrorType Lambda Function Error Type
     * @return A request to the invocation error path to inform in JSON format about the error which was thrown during the function execution.
     */
    default HttpRequest<AwsLambdaRuntimeApiError> invocationErrorRequest(@Nonnull String requestId,
                                                                        @Nullable String errorMessage,
                                                                        @Nullable String errorType,
                                                                        @Nullable String lambdaFunctionErrorType) {
        AwsLambdaRuntimeApiError error = new AwsLambdaRuntimeApiError(errorMessage, errorType);
        MutableHttpRequest<AwsLambdaRuntimeApiError> request = HttpRequest.POST(errorUri(requestId), error);
        if (lambdaFunctionErrorType != null) {
            return request.header(LAMBDA_RUNTIME_FUNCTION_ERROR_TYPE, lambdaFunctionErrorType);
        }
        return request;
    }

    /**
     *
     * @param errorMessage Error Message
     * @param errorType Error Type
     * @param lambdaFunctionErrorType Lambda Function Error Type
     * @return A post request which should be send if the runtime encounters an error during initialization to post an error message to the initialization error path.
     */
    default HttpRequest<AwsLambdaRuntimeApiError> initializationErrorRequest(@Nullable String errorMessage,
                                                                            @Nullable String errorType,
                                                                            @Nullable String lambdaFunctionErrorType) {
        AwsLambdaRuntimeApiError error = new AwsLambdaRuntimeApiError(errorMessage, errorType);
        MutableHttpRequest<AwsLambdaRuntimeApiError> request = HttpRequest.POST(INIT_ERROR_URI, error);
        if (lambdaFunctionErrorType != null) {
            return request.header(LAMBDA_RUNTIME_FUNCTION_ERROR_TYPE, lambdaFunctionErrorType);
        }
        return request;
    }
}
