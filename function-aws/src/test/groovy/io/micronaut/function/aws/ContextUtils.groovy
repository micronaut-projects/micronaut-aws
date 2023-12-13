package io.micronaut.function.aws

import com.amazonaws.services.lambda.runtime.ClientContext
import com.amazonaws.services.lambda.runtime.CognitoIdentity
import com.amazonaws.services.lambda.runtime.LambdaLogger
import com.amazonaws.services.lambda.runtime.Context

final class ContextUtils {
    private ContextUtils() {
    }

    static Context mock() {
        new Context() {
            @Override
            String getAwsRequestId() {
                null
            }

            @Override
            String getLogGroupName() {
                null
            }

            @Override
            String getLogStreamName() {
                null
            }

            @Override
            String getFunctionName() {
                return "foo";
            }

            @Override
            String getFunctionVersion() {
                null
            }

            @Override
            String getInvokedFunctionArn() {
                null
            }

            @Override
            CognitoIdentity getIdentity() {
                null
            }

            @Override
            ClientContext getClientContext() {
                null
            }

            @Override
            int getRemainingTimeInMillis() {
                return 0;
            }

            @Override
            int getMemoryLimitInMB() {
                return 0;
            }

            @Override
            LambdaLogger getLogger() {
                null
            }
        };
    }
}
