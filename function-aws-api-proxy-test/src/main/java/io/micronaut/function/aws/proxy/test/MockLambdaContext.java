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
package io.micronaut.function.aws.proxy.test;

import com.amazonaws.services.lambda.runtime.ClientContext;
import com.amazonaws.services.lambda.runtime.CognitoIdentity;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import io.micronaut.core.annotation.Internal;

/**
 * Mock Lambda context.
 */
@Internal
public class MockLambdaContext implements Context {

    //-------------------------------------------------------------
    // Variables - Private - Static
    //-------------------------------------------------------------

    private static LambdaLogger logger = new MockLambdaConsoleLogger();


    //-------------------------------------------------------------
    // Implementation - Context
    //-------------------------------------------------------------


    @Override
    public String getAwsRequestId() {
        return null;
    }


    @Override
    public String getLogGroupName() {
        return null;
    }


    @Override
    public String getLogStreamName() {
        return null;
    }


    @Override
    public String getFunctionName() {
        return null;
    }


    @Override
    public String getFunctionVersion() {
        return null;
    }


    @Override
    public String getInvokedFunctionArn() {
        return null;
    }


    @Override
    public CognitoIdentity getIdentity() {
        return null;
    }


    @Override
    public ClientContext getClientContext() {
        return null;
    }


    @Override
    public int getRemainingTimeInMillis() {
        return 0;
    }


    @Override
    public int getMemoryLimitInMB() {
        return 0;
    }


    @Override
    public LambdaLogger getLogger() {
        return logger;
    }
}
