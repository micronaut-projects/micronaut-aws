package io.micronaut.function.aws.proxy.test;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.sun.net.httpserver.HttpExchange;

@FunctionalInterface
public interface HttpExchangeToAwsProxyRequestAdapter {
    APIGatewayV2HTTPEvent createAwsProxyRequest(HttpExchange httpExchange);
}
