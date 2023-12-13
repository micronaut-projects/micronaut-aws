package io.micronaut.function.aws

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent
import io.micronaut.context.ApplicationContext
import io.micronaut.context.env.Environment
import io.micronaut.function.FunctionBean
import io.micronaut.http.HttpMethod
import io.micronaut.json.JsonMapper
import jakarta.inject.Inject
import jakarta.inject.Singleton
import spock.lang.Specification

import java.util.function.Function

class MicronautRequestStreamHandlerStartsContextSpec extends Specification {

    void "MicronautRequestStreamHandler starts application context"() throws IOException {
        given:
        String expectation = '{"message":"Hello World"}'
        FunctionRequestHandler handler = new FunctionRequestHandler()

        when:
        JsonMapper jsonMapper = handler.getApplicationContext().getBean(JsonMapper.class)
        APIGatewayProxyRequestEvent request = createRequest(HttpMethod.GET, "/")
        APIGatewayProxyResponseEvent response = execute(handler, jsonMapper, request)

        then: 'application context is started'
        200 == response.getStatusCode().intValue()
        expectation == response.body
        and: "bean injection works in class extending MicronautRequestStreamHandler"
        "HELLO WORLD" == handler.getUppercaser().uppercase("Hello World")

        when:
        handler.close()
        handler = new FunctionRequestHandler(ApplicationContext.builder().build().stop())
        jsonMapper = handler.getApplicationContext().getBean(JsonMapper.class)
        request = createRequest(HttpMethod.GET, "/")
        response = execute(handler, jsonMapper, request)

        then: 'application context is started'
        200 == response.getStatusCode().intValue()
        expectation == response.body

        and: "bean injection works in class extending MicronautRequestStreamHandler"
        "HELLO WORLD" == handler.getUppercaser().uppercase("Hello World")

        cleanup:
        handler.close()
    }

    private static APIGatewayProxyRequestEvent createRequest(HttpMethod method, String path) {
        APIGatewayProxyRequestEvent request = new APIGatewayProxyRequestEvent();
        request.setHttpMethod(method.name());
        request.setPath(path);
        return request;
    }

    private static APIGatewayProxyResponseEvent execute(MicronautRequestStreamHandler handler,
                                                        JsonMapper jsonMapper,
                                                        APIGatewayProxyRequestEvent request) throws IOException {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(jsonMapper.writeValueAsBytes(request))
        ByteArrayOutputStream output = new ByteArrayOutputStream()
        handler.handleRequest(inputStream, output, ContextUtils.mock())
        return jsonMapper.readValue(output.toByteArray(), APIGatewayProxyResponseEvent.class)
    }

    static class FunctionRequestHandler extends MicronautRequestStreamHandler {
        public static final String FUNCTION_NAME = "apiHandler"

        @Inject
        UpperCaseString uppercaser

        FunctionRequestHandler() {
        }

        FunctionRequestHandler(ApplicationContext applicationContext) {
            super(applicationContext)
        }

        UpperCaseString getUppercaser() {
            return uppercaser
        }

        @Override
        protected String resolveFunctionName(Environment env) {
            return FUNCTION_NAME
        }
    }

    @FunctionBean(FunctionRequestHandler.FUNCTION_NAME)
    static class ApiHandler implements Function<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
        @Inject
        JsonMapper jsonMapper

        @Override
        APIGatewayProxyResponseEvent apply(APIGatewayProxyRequestEvent apiGatewayProxyRequestEvent) {
            APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent()
            try {
                String json = new String(jsonMapper.writeValueAsBytes([message: "Hello World"]))
                response.setStatusCode(200)
                response.setBody(json)
            } catch (IOException e) {
                response.setStatusCode(500)
            }
            return response;
        }
    }

    @Singleton
    static class UpperCaseString {
        String uppercase(String str) {
            str.toUpperCase()
        }
    }

}
