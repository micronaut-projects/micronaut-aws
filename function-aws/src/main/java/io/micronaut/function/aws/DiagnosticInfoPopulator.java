package io.micronaut.function.aws;

import com.amazonaws.services.lambda.runtime.ClientContext;
import com.amazonaws.services.lambda.runtime.CognitoIdentity;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import io.micronaut.context.ApplicationContext;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.util.StringUtils;
import org.slf4j.MDC;

import java.util.Optional;

@Introspected
public class DiagnosticInfoPopulator {

    public static final String ENV_X_AMZN_TRACE_ID = "_X_AMZN_TRACE_ID";

    // See: https://github.com/aws/aws-xray-sdk-java/issues/251
    public static final String LAMBDA_TRACE_HEADER_PROP = "com.amazonaws.xray.traceHeader";

    public static final String MDC_DEFAULT_AWS_REQUEST_ID = "AWSRequestId";
    public static final String MDC_DEFAULT_FUNCTION_NAME = "AWSFunctionName";
    public static final String MDC_DEFAULT_FUNCTION_VERSION = "AWSFunctionVersion";
    public static final String MDC_DEFAULT_FUNCTION_ARN = "AWSFunctionArn";
    public static final String MDC_DEFAULT_FUNCTION_MEMORY_SIZE = "AWSFunctionMemoryLimit";
    public static final String MDC_DEFAULT_FUNCTION_REMAINING_TIME = "AWSFunctionRemainingTime";
    public static final String MDC_DEFAULT_XRAY_TRACE_ID = "AWS-XRAY-TRACE-ID";


    /**
     * Register the beans in the application.
     *
     * @param context context
     * @param applicationContext application context
     */
    static void registerContextBeans(com.amazonaws.services.lambda.runtime.Context context, ApplicationContext applicationContext) {
        applicationContext.registerSingleton(context);
        LambdaLogger logger = context.getLogger();
        if (logger != null) {
            applicationContext.registerSingleton(logger);
        }
        ClientContext clientContext = context.getClientContext();
        if (clientContext != null) {
            applicationContext.registerSingleton(clientContext);
        }
        CognitoIdentity identity = context.getIdentity();
        if (identity != null) {
            applicationContext.registerSingleton(identity);
        }
    }

    /**
     * @see <a href="https://docs.aws.amazon.com/lambda/latest/dg/java-logging.html">AWS Lambda function logging in Java</a>
     * @param context The Lambda execution environment context object.
     */
    public void populateMappingDiagnosticContextValues(@NonNull com.amazonaws.services.lambda.runtime.Context context) {
        if (context.getAwsRequestId() != null) {
            mdcput(MDC_DEFAULT_AWS_REQUEST_ID, context.getAwsRequestId());
        }
        if (context.getFunctionName() != null) {
            mdcput(MDC_DEFAULT_FUNCTION_NAME, context.getFunctionName());
        }
        if (context.getFunctionVersion() != null) {
            mdcput(MDC_DEFAULT_FUNCTION_VERSION, context.getFunctionVersion());
        }
        if (context.getInvokedFunctionArn() != null) {
            mdcput(MDC_DEFAULT_FUNCTION_ARN, context.getInvokedFunctionArn());
        }
        mdcput(MDC_DEFAULT_FUNCTION_MEMORY_SIZE, String.valueOf(context.getMemoryLimitInMB()));
        mdcput(MDC_DEFAULT_FUNCTION_REMAINING_TIME, String.valueOf(context.getRemainingTimeInMillis()));
    }

    /**
     * Put a diagnostic context value.
     * @param key non-null key
     * @param val value to put in the map
     * @throws IllegalArgumentException in case the "key" parameter is null
     */
    protected void mdcput(@NonNull String key, @NonNull String val) throws IllegalArgumentException {
        MDC.put(key, val);
    }

    /**
     * Populate MDC with XRay Trace ID if is able to parse it.
     */
    public void populateMappingDiagnosticContextWithXrayTraceId() {
        parseXrayTraceId().ifPresent(xrayTraceId -> mdcput(MDC_DEFAULT_XRAY_TRACE_ID, xrayTraceId));
    }

    /**
     * Parses XRay Trace ID from _X_AMZN_TRACE_ID environment variable.
     * @see <a href="https://docs.aws.amazon.com/xray/latest/devguide/xray-sdk-java-configuration.html">Trace ID injection into logs</a>
     * @return Trace id or empty if not found
     */
    @NonNull
    protected static Optional<String> parseXrayTraceId() {
        String lambdaTraceHeaderKey = System.getenv(ENV_X_AMZN_TRACE_ID);
        lambdaTraceHeaderKey = StringUtils.isNotEmpty(lambdaTraceHeaderKey) ? lambdaTraceHeaderKey
                : System.getProperty(LAMBDA_TRACE_HEADER_PROP);
        if (lambdaTraceHeaderKey != null) {
            String[] arr = lambdaTraceHeaderKey.split(";");
            if (arr.length >= 1) {
                return Optional.of(arr[0].replace("Root=", ""));
            }
        }
        return Optional.empty();
    }
}
