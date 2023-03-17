package io.micronaut.aws.function.apigatewayproxy;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.ApplicationContextProvider;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.type.Argument;
import io.micronaut.function.executor.FunctionInitializer;
import io.micronaut.http.codec.MediaTypeCodecRegistry;
import io.micronaut.http.cookie.Cookies;
import io.micronaut.servlet.http.ServletHttpHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ApiGatewayProxyEventFunction extends FunctionInitializer implements
    RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent>,
    ApplicationContextProvider,
    Closeable {

    private static final Logger LOG = LoggerFactory.getLogger(ApiGatewayProxyEventFunction.class);

    private final ServletHttpHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> httpHandler;
    private final ConversionService conversionService;
    private final MediaTypeCodecRegistry codecRegistry;

    public ApiGatewayProxyEventFunction() {
        httpHandler = initializeHandler();
        this.conversionService = applicationContext.getBean(ConversionService.class);
        this.codecRegistry = applicationContext.getBean(MediaTypeCodecRegistry.class);
    }

    public ApiGatewayProxyEventFunction(ApplicationContext ctx) {
        super(ctx);
        httpHandler = initializeHandler();
        this.conversionService = applicationContext.getBean(ConversionService.class);
        this.codecRegistry = applicationContext.getBean(MediaTypeCodecRegistry.class);
    }

    private ServletHttpHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> initializeHandler() {
        ApiGatewayProxyEventHandler apiGatewayProxyEventHandler = new ApiGatewayProxyEventHandler(applicationContext, codecRegistry, conversionService);

        Runtime.getRuntime().addShutdownHook(
            new Thread(apiGatewayProxyEventHandler::close)
        );
        return apiGatewayProxyEventHandler;
    }

    public ApiGatewayProxyServletResponse handleRequest(io.micronaut.http.HttpRequest<?> request) {
        ApiGatewayProxyServletResponse<Object> response = new ApiGatewayProxyServletResponse<>(conversionService);
        return (ApiGatewayProxyServletResponse) httpHandler.exchange(new ApiGatewayProxyServletRequest<>(toRequest(request), response, codecRegistry, conversionService)).getResponse();
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {
        return httpHandler.exchange(input, new APIGatewayProxyResponseEvent()).getResponse().getNativeResponse();
    }

    private APIGatewayProxyRequestEvent toRequest(io.micronaut.http.HttpRequest<?> request) {

        Map<String, List<String>> headers = new LinkedHashMap<>();
        Map<String, List<String>> parameters = new LinkedHashMap<>();
        request.getHeaders().forEach(headers::put);
        request.getParameters().forEach(parameters::put);
        Object body = request.getBody().orElse(null);

        try {
            Cookies cookies = request.getCookies();
            cookies.forEach((s, cookie) -> {
            });
        } catch (UnsupportedOperationException e) {
            //not all request types support retrieving cookies
        }
        return new APIGatewayProxyRequestEvent() {

            @Override
            public Map<String, String> getHeaders() {
                return request.getHeaders().asMap(String.class, String.class);
            }

            @Override
            public String getPath() {
                return request.getPath();
            }

            @Override
            public String getHttpMethod() {
                return request.getMethodName();
            }

            @Override
            public String getBody() {
                return request.getBody(Argument.of(String.class)).orElse(null);
            }
        };
    }
}
