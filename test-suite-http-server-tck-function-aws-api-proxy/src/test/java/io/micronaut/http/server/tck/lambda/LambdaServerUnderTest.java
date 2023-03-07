package io.micronaut.http.server.tck.lambda;

import com.amazonaws.serverless.exceptions.ContainerInitializationException;
import com.amazonaws.serverless.proxy.internal.testutils.MockLambdaContext;
import com.amazonaws.serverless.proxy.model.ApiGatewayRequestIdentity;
import com.amazonaws.serverless.proxy.model.AwsProxyRequest;
import com.amazonaws.serverless.proxy.model.AwsProxyRequestContext;
import com.amazonaws.serverless.proxy.model.AwsProxyResponse;
import com.amazonaws.serverless.proxy.model.MultiValuedTreeMap;
import com.amazonaws.services.lambda.runtime.Context;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.ApplicationContextBuilder;
import io.micronaut.core.type.Argument;
import io.micronaut.function.aws.LambdaApplicationContextBuilder;
import io.micronaut.function.aws.proxy.MicronautLambdaHandler;
import io.micronaut.http.HttpHeaders;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.MutableHttpParameters;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.http.client.netty.NettyClientHttpRequestFactory;
import io.micronaut.http.server.tck.ServerUnderTest;
import io.micronaut.http.simple.SimpleHttpResponseFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class LambdaServerUnderTest implements ServerUnderTest {
    private MicronautLambdaHandler handler;
    private Context lambdaContext;

    public LambdaServerUnderTest(Map<String, Object> properties) {
        try {
            ApplicationContextBuilder contextBuilder = new LambdaApplicationContextBuilder();
            contextBuilder.properties(properties);
            this.handler = new MicronautLambdaHandler(contextBuilder);
            this.lambdaContext = new MockLambdaContext();
        } catch (ContainerInitializationException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public <I, O> HttpResponse<O> exchange(HttpRequest<I> request, Argument<O> bodyType) {
        try {
            AwsProxyRequest input = adaptRequest(request);
            AwsProxyResponse awsProxyResponse = handler.handleRequest(input, lambdaContext);
            return adaptReponse(awsProxyResponse);
        } catch (UnsupportedEncodingException e) {
            return new SimpleHttpResponseFactory().status(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private <I> AwsProxyRequest adaptRequest(HttpRequest<I> request) throws UnsupportedEncodingException {
        AwsProxyRequest input = new AwsProxyRequest();
        input.setHttpMethod(request.getMethodName());
        input.setRequestContext(new AwsProxyRequestContext() {
            @Override
            public ApiGatewayRequestIdentity getIdentity() {
                return new ApiGatewayRequestIdentity() {
                    @Override
                    public String getSourceIp() {
                        return "127.0.0.1";
                    }
                };
            }
        });
        input.setPath(request.getPath());
        for (String headerName : request.getHeaders().names()) {
            input.getMultiValueHeaders().put(headerName, request.getHeaders().getAll(headerName));
        }
        MutableHttpParameters parameters = NettyClientHttpRequestFactory.INSTANCE.create(request.getMethod(), request.getUri().toString()).getParameters();
        Map<String, List<String>> paramsMap = parameters.asMap();
        MultiValuedTreeMap<String, String> multiValueQueryStringParameters = new MultiValuedTreeMap<>();
        for (String paramName : paramsMap.keySet()) {
            multiValueQueryStringParameters.addAll(paramName, paramsMap.get(paramName));
        }
        input.setMultiValueQueryStringParameters(multiValueQueryStringParameters);

        Map<String, String> queryStringParameters = new HashMap<>();
        for (String paramName : parameters.names()) {
            queryStringParameters.put(paramName, request.getParameters().get(paramName));
        }
        input.setQueryStringParameters(queryStringParameters);

        if (request.getContentType().isPresent() && request.getContentType().get().equals(MediaType.APPLICATION_FORM_URLENCODED_TYPE)) {
            Optional<Map> body = request.getBody(Map.class);
            if (body.isPresent()) {
                input.setBody(getDataString(body.get()));
                input.setIsBase64Encoded(false);
            }
            Optional<String> bodyString = request.getBody(String.class);
            if (bodyString.isPresent()) {
                input.setBody(bodyString.get());
                input.setIsBase64Encoded(false);
            }
        } else {
            Optional<String> body = request.getBody(String.class);
            if (body.isPresent()) {
                input.setBody(body.get());
                input.setIsBase64Encoded(false);
            }
        }
        return input;
    }

    private <O> HttpResponse<O> adaptReponse(AwsProxyResponse awsProxyResponse) {
        MutableHttpResponse<O> response = new SimpleHttpResponseFactory().status(HttpStatus.valueOf(awsProxyResponse.getStatusCode()));
        if (awsProxyResponse.getMultiValueHeaders() != null) {
            HttpHeaders headers = new MultiValueHeadersAdapter(awsProxyResponse.getMultiValueHeaders());
            for (String headerName : headers.names()) {
                for (String value : headers.getAll(headerName)) {
                    response.header(headerName, value);
                }
            }
        }
        if (awsProxyResponse.isBase64Encoded()) {
            response.body(Base64.getMimeDecoder().decode(awsProxyResponse.getBody()));
        } else {
            response.body(awsProxyResponse.getBody());
        }

        if (response.getStatus().getCode() >= 400) {
            throw new HttpClientResponseException("error", response);
        }
        return response;
    }

    private String getDataString(Map params) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for(Object k : params.keySet()) {
            if (first) {
                first = false;
            } else {
                result.append("&");
            }
            result.append(URLEncoder.encode(k.toString(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(params.get(k).toString(), "UTF-8"));
        }
        return result.toString();
    }

    @Override
    public ApplicationContext getApplicationContext() {
        return handler.getApplicationContext();
    }

    @Override
    public Optional<Integer> getPort() {
        // Need a port for the CORS tests
        return Optional.of(1234);
    }

    @Override
    public void close() throws IOException {
        this.handler.close();
    }
}
