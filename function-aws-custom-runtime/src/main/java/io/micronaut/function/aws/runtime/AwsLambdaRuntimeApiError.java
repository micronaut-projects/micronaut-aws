/*
 * Copyright 2017-2019 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micronaut.function.aws.runtime;

import io.micronaut.core.annotation.Introspected;

import javax.annotation.Nullable;

/**
 * AWS Lambda Runtime Interface Error.
 * It is used to communicate errors thrown during function execution.
 *
 * @author sdelamo
 * @since 2.0.0
 */
@Introspected
public class AwsLambdaRuntimeApiError {
    @Nullable
    private String errorMessage;

    @Nullable
    private String errorType;

    /**
     * Constructor.
     */
    public AwsLambdaRuntimeApiError() {
    }

    /**
     *
     * @param errorMessage Error Message
     * @param errorType Error Type
     */
    public AwsLambdaRuntimeApiError(@Nullable String errorMessage,
                                    @Nullable String errorType) {
        this.errorMessage = errorMessage;
        this.errorType = errorType;
    }

    /**
     *
     * @return Error Message
     */
    @Nullable
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     *
     * @param errorMessage Error message
     */
    public void setErrorMessage(@Nullable String errorMessage) {
        this.errorMessage = errorMessage;
    }

    /**
     *
     * @return Error Type
     */
    @Nullable
    public String getErrorType() {
        return errorType;
    }

    /**
     *
     * @param errorType Error Type
     */
    public void setErrorType(@Nullable String errorType) {
        this.errorType = errorType;
    }

    @Override
    public String toString() {
        return "AwsLambdaRuntimeApiError{" +
                "errorMessage='" + errorMessage + '\'' +
                ", errorType='" + errorType + '\'' +
                '}';
    }
}
