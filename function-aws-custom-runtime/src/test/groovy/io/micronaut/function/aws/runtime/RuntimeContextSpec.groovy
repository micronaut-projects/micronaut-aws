package io.micronaut.function.aws.runtime

import com.amazonaws.services.lambda.runtime.Context
import groovy.transform.InheritConstructors
import io.micronaut.core.convert.ConversionService
import io.micronaut.http.simple.SimpleHttpHeaders
import spock.lang.Specification

class RuntimeContextSpec extends Specification {

    void "Runtime context is built with HTTP Headers and environment variables"() {
        given:
        Map<String, String> headers = [
                "Content-Type": "application/json",
                "Lambda-Runtime-Aws-Request-Id": requestId,
                "Lambda-Runtime-Deadline-Ms": "1585319307614",
                "Lambda-Runtime-Invoked-Function-Arn": arn,
                "Lambda-Runtime-Trace-Id": "Root=1-5e7e0d88-3035367cad0be9af410af341Parent=257c305f5ab43516Sampled=1",
                "Date":  "Fri, 27 Mar 2020 14:28:25 GMT",
                "Content-Length": "1618",
                "Connection":  "close"
        ] as Map<String, String>
        SimpleHttpHeaders simpleHttpHeaders = new SimpleHttpHeaders(headers, ConversionService.SHARED)

        Context context = new CustomRuntimeContext(simpleHttpHeaders)

        expect:
        context.getAwsRequestId() == requestId
        context.getLogGroupName() == '/aws/lambda/customruntimetest'
        context.getLogStreamName() == '2020/03/27/[$LATEST]6a827d2d24914b88a2f4acbcbc86f768'
        context.getFunctionName() == 'customruntimetest'
        context.getFunctionVersion() == '$LATEST'
        context.getInvokedFunctionArn() == arn
        !context.getIdentity()
        !context.getClientContext()
        context.getRemainingTimeInMillis() == 5000
        context.getMemoryLimitInMB() == 512
        context.getLogger()

        where:
        requestId = "777e7e60-e583-483b-ae3a-2ed7c387823d"
        arn = '"arn:aws:lambda:eu-west-1:020181215351:function:customruntimetest"'
    }

    @InheritConstructors
    static class CustomRuntimeContext extends RuntimeContext {

        static final Map<String, Object> m = [
                'HANDLER': 'example.micronaut.Handler::handleRequest',
                'AWS_REGION': 'eu-west-1',
                'AWS_LAMBDA_FUNCTION_NAME': 'customruntimetest',
                'AWS_LAMBDA_FUNCTION_MEMORY_SIZE': 512,
                'AWS_LAMBDA_FUNCTION_VERSION': '$LATEST',
                'AWS_LAMBDA_LOG_GROUP_NAME': '/aws/lambda/customruntimetest',
                'AWS_LAMBDA_LOG_STREAM_NAME': '2020/03/27/[$LATEST]6a827d2d24914b88a2f4acbcbc86f768',
                'AWS_ACCESS_KEY_ID': 'XXXXXXXXXXXXXXXXXXXX',
                'AWS_SECRET_ACCESS_KEY': 'YYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYY',
                'AWS_SESSION_TOKEN': 'xxxxxxxxxxxxxxxxxx///////////xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx//////////xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx',
                'AWS_LAMBDA_RUNTIME_API': '127.0.0.1:9001',
                'LAMBDA_TASK_ROOT': '/var/task',
                'LAMBDA_RUNTIME_DIR': '/var/runtime',
                'TZ': ':UTC',
        ]

        @Override
        long currentTime() {
            1585319302614L
        }

        @Override
        String getEnv(String name) {
            return m[name]
        }
    }
}
