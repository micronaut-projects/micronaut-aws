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
package io.micronaut.function.aws.proxy;

import com.amazonaws.serverless.proxy.model.AwsProxyRequest;
import com.amazonaws.serverless.proxy.model.AwsProxyRequestContext;
import io.micronaut.http.*;
import io.micronaut.http.annotation.*;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;

import javax.ws.rs.core.Response;
import java.security.Principal;
import java.util.List;
import java.util.Random;
import java.util.Set;

import static io.micronaut.function.aws.proxy.MicronautAwsProxyTest.SERVLET_RESP_HEADER_KEY;


@Controller("/echo")
@Secured(SecurityRule.IS_ANONYMOUS)
public class EchoMicronautController {
    @Get("/encoded-param")
    public SingleValueModel echoEncodedParam(@QueryValue("param") String param) {
        SingleValueModel model = new SingleValueModel();
        model.setValue(param);
        return model;
    }

    @Get("/query-string")
    public MapResponseModel echoQueryString(HttpRequest<?> request) {
        MapResponseModel queryStrings = new MapResponseModel();
        final HttpParameters parameters = request.getParameters();
        for (String key : parameters.names()) {
            queryStrings.addValue(key, parameters.get(key));
        }

        return queryStrings;
    }
    @Get("/encoded-path/{resource}")
    public Response encodedPathParam(@PathVariable("resource") String resource) {
        SingleValueModel sv = new SingleValueModel();
        sv.setValue(resource);
        return Response.ok(sv).build();
    }

    @Get("/decoded-param")
    @Produces(MediaType.APPLICATION_JSON)
    public SingleValueModel echoDecodedParam(@QueryValue("param") String param) {
        SingleValueModel model = new SingleValueModel();
        model.setValue(param);
        return model;
    }

    @Get("/list-query-string")
    @Produces(MediaType.APPLICATION_JSON)
    public SingleValueModel echoQueryStringLength(@QueryValue("list") List<String> param) {
        System.out.println("param: " + param + " = " + param.size());
        SingleValueModel model = new SingleValueModel();
        model.setValue(param.size() + "");
        return model;
    }

    @Get("/scheme")
    @Produces(MediaType.APPLICATION_JSON)
    public SingleValueModel echoRequestScheme(HttpRequest<?> request) {
        SingleValueModel model = new SingleValueModel();
        System.out.println("RequestUri: " + request.getUri().toString());
            model.setValue(request.getUri().getScheme());
        return model;


    }

    @Get("/referer-header")
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<SingleValueModel> referer(@Header("Referer") String referer) {
        System.out.println("Received referer: " + referer);
        SingleValueModel sv = new SingleValueModel();
        sv.setValue(referer);
        return HttpResponse.ok(sv);
    }

    @Get("/servlet-headers")
    @Produces(MediaType.APPLICATION_JSON)
    public MapResponseModel echoServerHeaders(HttpRequest<?> request) {
        MapResponseModel headers = new MapResponseModel();
        final HttpHeaders requestHeaders = request.getHeaders();
        final Set<String> names = requestHeaders.names();
        for (String name : names) {
            headers.addValue(name, requestHeaders.get(name));
        }
        return headers;
    }

    @Get("/status-code")
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<SingleValueModel> echoCustomStatusCode(@QueryValue("status") int statusCode ) {
        SingleValueModel output = new SingleValueModel();
        output.setValue("" + statusCode);

        return HttpResponse.<SingleValueModel>status(HttpStatus.valueOf(statusCode)).body(output);
    }

    @Get("/binary")
    @Produces("application/octet-stream")
    public HttpResponse<byte[]> echoBinaryData() {
        byte[] b = new byte[128];
        new Random().nextBytes(b);

        return HttpResponse.ok(b);
    }

    @Get("/servlet-response")
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<SingleValueModel> echoCustomStatusCode() {
        SingleValueModel output = new SingleValueModel();
        output.setValue("Custom header in resp");
        return HttpResponse.ok(output)
                .header(SERVLET_RESP_HEADER_KEY, "1");
    }

    @Get("/headers")
    @Produces(MediaType.APPLICATION_JSON)
    public MapResponseModel echoHeaders(HttpRequest<?> context) {
        MapResponseModel headers = new MapResponseModel();
        final HttpHeaders httpHeaders = context.getHeaders();
        for (String key : httpHeaders.names()) {
            final List<String> values = httpHeaders.getAll(key);
            for (String value : values) {
                headers.addValue(key, value);
            }
        }

        return headers;
    }

    @Put(uri = "/empty-stream/{paramId}/test/{param2}", processes = MediaType.APPLICATION_JSON)
    public HttpResponse<SingleValueModel> emptyStream(@PathVariable("paramId") String paramId, @PathVariable("param2") String param2) {
        SingleValueModel sv = new SingleValueModel();
        sv.setValue(paramId);
        return HttpResponse.ok(sv);
    }

    @Get("/authorizer-principal")
    @Produces(MediaType.APPLICATION_JSON)
    public SingleValueModel echoAuthorizerPrincipal(AwsProxyRequest proxyRequest) {
        SingleValueModel valueModel = new SingleValueModel();
        valueModel.setValue(proxyRequest.getRequestContext().getAuthorizer().getPrincipalId());

        return valueModel;
    }

    @Post(uri = "/json-body", processes = MediaType.APPLICATION_JSON)
    public SingleValueModel echoJsonValue(@Body final SingleValueModel requestValue) {
        SingleValueModel output = new SingleValueModel();
        output.setValue(requestValue.getValue());

        return output;
    }

    @Get("/security-context")
    @Produces(MediaType.APPLICATION_JSON)
    public SingleValueModel getPrincipal(Principal principal) {
        SingleValueModel output = new SingleValueModel();
        output.setValue(principal.getName());
        return output;
    }

    @Get("/authorizer-context")
    public SingleValueModel echoAuthorizerContext(AwsProxyRequestContext awsProxyRequestContext, @QueryValue("key") String key) {
        SingleValueModel valueModel = new SingleValueModel();
        valueModel.setValue(awsProxyRequestContext.getAuthorizer().getContextValue(key));

        return valueModel;
    }
}
