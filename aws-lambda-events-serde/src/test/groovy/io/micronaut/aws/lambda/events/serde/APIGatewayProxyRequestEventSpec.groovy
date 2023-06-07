package io.micronaut.aws.lambda.events.serde

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent
import io.micronaut.context.BeanContext
import io.micronaut.serde.ObjectMapper
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import spock.lang.Specification

@MicronautTest(startApplication = false)
class APIGatewayProxyRequestEventSpec extends Specification {
    @Inject
    ObjectMapper objectMapper

    @Inject
    BeanContext beanContext

    void "APIGatewayProxyRequestEvent can be serialized"() {
        given:
        File f = new File("src/test/resources/api-gateway-proxy.json")

        expect:
        f.exists()

        when:
        String json = f.text
        APIGatewayProxyRequestEvent event = objectMapper.readValue(json, APIGatewayProxyRequestEvent)

        then:
        event
        "eyJ0ZXN0IjoiYm9keSJ9" == event.body
        "/{proxy+}" == event.resource
        "/path/to/resource" == event.path
        "POST" == event.httpMethod
        event.isBase64Encoded
        [foo: "bar"] == event.queryStringParameters
        [foo: ["bar"]] == event.multiValueQueryStringParameters
        [proxy: "/path/to/resource"] == event.pathParameters
        [baz: "qux"] == event.stageVariables
        [
                "Accept"                      : "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8",
                "Accept-Encoding"             : "gzip, deflate, sdch",
                "Accept-Language"             : "en-US,en;q=0.8",
                "Cache-Control"               : "max-age=0",
                "CloudFront-Forwarded-Proto"  : "https",
                "CloudFront-Is-Desktop-Viewer": "true",
                "CloudFront-Is-Mobile-Viewer" : "false",
                "CloudFront-Is-SmartTV-Viewer": "false",
                "CloudFront-Is-Tablet-Viewer" : "false",
                "CloudFront-Viewer-Country"   : "US",
                "Host"                        : "1234567890.execute-api.us-east-1.amazonaws.com",
                "Upgrade-Insecure-Requests"   : "1",
                "User-Agent"                  : "Custom User Agent String",
                "Via"                         : "1.1 08f323deadbeefa7af34d5feb414ce27.cloudfront.net (CloudFront)",
                "X-Amz-Cf-Id"                 : "cDehVQoZnx43VYQb9j2-nvCh-9z396Uhbp027Y2JvkCPNLmGJHqlaA==",
                "X-Forwarded-For"             : "127.0.0.1, 127.0.0.2",
                "X-Forwarded-Port"            : "443",
                "X-Forwarded-Proto"           : "https"
        ] == event.headers
        ["text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8"] == event.multiValueHeaders.get("Accept")
        ["gzip, deflate, sdch"] == event.multiValueHeaders.get("Accept-Encoding")
        ["en-US,en;q=0.8"] == event.multiValueHeaders.get("Accept-Language")
        ["max-age=0"] == event.multiValueHeaders.get("Cache-Control")
        ["https"] == event.multiValueHeaders.get("CloudFront-Forwarded-Proto")
        ["true"] == event.multiValueHeaders.get("CloudFront-Is-Desktop-Viewer")
        ["false"] == event.multiValueHeaders.get("CloudFront-Is-Mobile-Viewer")
        ["false"] == event.multiValueHeaders.get("CloudFront-Is-SmartTV-Viewer")
        ["false"] == event.multiValueHeaders.get("CloudFront-Is-Tablet-Viewer")
        ["US"] == event.multiValueHeaders.get("CloudFront-Viewer-Country")
        ["0123456789.execute-api.us-east-1.amazonaws.com"] == event.multiValueHeaders.get("Host")
        ["1"] == event.multiValueHeaders.get("Upgrade-Insecure-Requests")
        ["Custom User Agent String"] == event.multiValueHeaders.get("User-Agent")
        ["1.1 08f323deadbeefa7af34d5feb414ce27.cloudfront.net (CloudFront)"] == event.multiValueHeaders.get("Via")
        ["cDehVQoZnx43VYQb9j2-nvCh-9z396Uhbp027Y2JvkCPNLmGJHqlaA=="] == event.multiValueHeaders.get("X-Amz-Cf-Id")
        ["127.0.0.1, 127.0.0.2"] == event.multiValueHeaders.get("X-Forwarded-For")
        ["443"] == event.multiValueHeaders.get("X-Forwarded-Port")
        ["https"] == event.multiValueHeaders.get("X-Forwarded-Proto")
        "123456789012" == event.requestContext.accountId
        "123456" == event.requestContext.resourceId
        "prod" == event.requestContext.stage
        "c6af9ac6-7b61-11e6-9a41-93e8deadbeef" == event.requestContext.requestId
        //"09/Apr/2015:12:34:56 +0000" == event.requestContext.requestTime
        //1428582896000 == event.requestContext.requestTimeEpoch
        null == event.requestContext.identity.cognitoIdentityPoolId
        null == event.requestContext.identity.accountId
        null == event.requestContext.identity.cognitoIdentityId
        null == event.requestContext.identity.caller
        null == event.requestContext.identity.accessKey
        "127.0.0.1" == event.requestContext.identity.sourceIp
        null == event.requestContext.identity.cognitoAuthenticationType
        null == event.requestContext.identity.cognitoAuthenticationProvider
        null == event.requestContext.identity.userArn
        "Custom User Agent String" == event.requestContext.identity.userAgent
        null == event.requestContext.identity.user
        "/prod/path/to/resource" == event.requestContext.path
        "/{proxy+}" == event.requestContext.resourcePath
        "POST" == event.requestContext.httpMethod
        "1234567890" == event.requestContext.apiId
        //"HTTP/1.1" == event.requestContext.protocol
    }
}
