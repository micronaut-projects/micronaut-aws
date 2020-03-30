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

import com.amazonaws.services.lambda.runtime.ClientContext;
import com.amazonaws.services.lambda.runtime.CognitoIdentity;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import io.micronaut.http.HttpHeaders;

import java.util.Calendar;

/**
 * Implementation of Lambda execution {@link Context} for runtime environments.
 *
 * @author sdelamo
 * @since 1.4
 */
public class RuntimeContext implements Context {
    private final HttpHeaders headers;

    /**
     *
     * @param headers HTTP Headers
     */
    public RuntimeContext(HttpHeaders headers) {
        this.headers = headers;
    }

    @Override
    public String getAwsRequestId() {
        return headers.get(LambdaRuntimeInvocationResponseHeaders.LAMBDA_RUNTIME_AWS_REQUEST_ID);
    }

    @Override
    public String getLogGroupName() {
        return getEnv(ReservedRuntimeEnvironmentVariables.AWS_LAMBDA_LOG_GROUP_NAME);
    }

    @Override
    public String getLogStreamName() {
        return getEnv(ReservedRuntimeEnvironmentVariables.AWS_LAMBDA_LOG_STREAM_NAME);
    }

    @Override
    public String getFunctionName() {
        return getEnv(ReservedRuntimeEnvironmentVariables.AWS_LAMBDA_FUNCTION_NAME);
    }

    @Override
    public String getFunctionVersion() {
        return getEnv(ReservedRuntimeEnvironmentVariables.AWS_LAMBDA_FUNCTION_VERSION);
    }

    @Override
    public String getInvokedFunctionArn() {
        return headers.get(LambdaRuntimeInvocationResponseHeaders.LAMBDA_RUNTIME_INVOKED_FUNCTION_ARN);
    }

    @Override
    public CognitoIdentity getIdentity() {
        // TODO Use LambdaRuntimeInvocationResponseHeaders.LAMBDA_RUNTIME_CLIENT_CONTEXT to build this
        return null;
    }

    @Override
    public ClientContext getClientContext() {
        // TODO Use LambdaRuntimeInvocationResponseHeaders.LAMBDA_RUNTIME_CLIENT_CONTEXT to build this
        return null;
    }

    @Override
    public int getRemainingTimeInMillis() {
        String millis = headers.get(LambdaRuntimeInvocationResponseHeaders.LAMBDA_RUNTIME_DEADLINE_MS);
        try {
            if (millis != null) {
                long deadlineepoch = Long.parseLong(millis);
                long currentepoch = currentTime();
                Long remainingTime = deadlineepoch - currentepoch;
                return remainingTime.intValue();
            }
        } catch (NumberFormatException e) {
        }
        return 0;
    }

    @Override
    public int getMemoryLimitInMB() {
        String memory = getEnv(ReservedRuntimeEnvironmentVariables.AWS_LAMBDA_FUNCTION_MEMORY_SIZE);
        if (memory != null) {
            try {
                return Integer.parseInt(memory);
            } catch (NumberFormatException e) {
            }
        }
        return 0;
    }

    @Override
    public LambdaLogger getLogger() {
        return null;
    }


    /**
     * @param name the name of the environment variable
     * @return the string value of the variable, or {@code null} if the variable is not defined
     */
    protected String getEnv(String name) {
        return System.getenv(name);
    }

    /**
     * @return the number of milliseconds since January 1, 1970, 00:00:00 GMT until the current date
     */
    protected long currentTime() {
        return Calendar.getInstance().getTimeInMillis();
    }
}
