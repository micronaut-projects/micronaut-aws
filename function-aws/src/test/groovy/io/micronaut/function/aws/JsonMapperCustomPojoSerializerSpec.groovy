package io.micronaut.function.aws

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent
import com.amazonaws.services.lambda.runtime.CustomPojoSerializer
import io.micronaut.serde.annotation.Serdeable
import spock.lang.Specification

import java.nio.charset.StandardCharsets

class JsonMapperCustomPojoSerializerSpec extends Specification {

    void "via SPI you can load JsonMapperCustomPojoSerializer as CustomPojoSerializer"() {
        given:
        File f = new File("src/test/resources/api-gateway-proxy.json")

        expect:
        f.exists()

        when:
        ServiceLoader<CustomPojoSerializer> loader = ServiceLoader.load(CustomPojoSerializer.class);
        Iterator<CustomPojoSerializer> iterator = loader.iterator();

        then:
        iterator.hasNext()

        when:
        CustomPojoSerializer customPojoSerializer = iterator.next()

        then:
        customPojoSerializer instanceof JsonMapperCustomPojoSerializer

        when:
        APIGatewayProxyRequestEvent event = customPojoSerializer.fromJson(f.newInputStream(), APIGatewayProxyRequestEvent.class)

        then:
        assertApiGatewayProxyRequestEvent(event)

        when:
        event = customPojoSerializer.fromJson(f.text, APIGatewayProxyRequestEvent.class)

        then:
        assertApiGatewayProxyRequestEvent(event)

        when:
        ByteArrayOutputStream baos = new ByteArrayOutputStream()
        customPojoSerializer.toJson(new Book(title: "Building Microservices"), baos, Book.class)

        then:
        '{"title":"Building Microservices"}' == new String(baos.toByteArray(), StandardCharsets.UTF_8)
    }

    @Serdeable
    static class Book {
        String title
    }

    void assertApiGatewayProxyRequestEvent(APIGatewayProxyRequestEvent event) {
        assert "eyJ0ZXN0IjoiYm9keSJ9" == event.body
        assert "/{proxy+}" == event.resource
        assert "/path/to/resource" == event.path
        assert "POST" == event.httpMethod
        assert event.isBase64Encoded
        assert [foo: "bar"] == event.queryStringParameters
        assert [foo: ["bar"]] == event.multiValueQueryStringParameters
        assert [proxy: "/path/to/resource"] == event.pathParameters
        assert [baz: "qux"] == event.stageVariables
        assert [
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
        assert ["text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8"] == event.multiValueHeaders.get("Accept")
        assert ["gzip, deflate, sdch"] == event.multiValueHeaders.get("Accept-Encoding")
        assert ["en-US,en;q=0.8"] == event.multiValueHeaders.get("Accept-Language")
        assert ["max-age=0"] == event.multiValueHeaders.get("Cache-Control")
        assert ["https"] == event.multiValueHeaders.get("CloudFront-Forwarded-Proto")
        assert ["true"] == event.multiValueHeaders.get("CloudFront-Is-Desktop-Viewer")
        assert ["false"] == event.multiValueHeaders.get("CloudFront-Is-Mobile-Viewer")
        assert ["false"] == event.multiValueHeaders.get("CloudFront-Is-SmartTV-Viewer")
        assert ["false"] == event.multiValueHeaders.get("CloudFront-Is-Tablet-Viewer")
        assert ["US"] == event.multiValueHeaders.get("CloudFront-Viewer-Country")
        assert ["0123456789.execute-api.us-east-1.amazonaws.com"] == event.multiValueHeaders.get("Host")
        assert ["1"] == event.multiValueHeaders.get("Upgrade-Insecure-Requests")
        assert ["Custom User Agent String"] == event.multiValueHeaders.get("User-Agent")
        assert ["1.1 08f323deadbeefa7af34d5feb414ce27.cloudfront.net (CloudFront)"] == event.multiValueHeaders.get("Via")
        assert ["cDehVQoZnx43VYQb9j2-nvCh-9z396Uhbp027Y2JvkCPNLmGJHqlaA=="] == event.multiValueHeaders.get("X-Amz-Cf-Id")
        assert ["127.0.0.1, 127.0.0.2"] == event.multiValueHeaders.get("X-Forwarded-For")
        assert ["443"] == event.multiValueHeaders.get("X-Forwarded-Port")
        assert ["https"] == event.multiValueHeaders.get("X-Forwarded-Proto")
        assert "123456789012" == event.requestContext.accountId
        assert "123456" == event.requestContext.resourceId
        assert "prod" == event.requestContext.stage
        assert "c6af9ac6-7b61-11e6-9a41-93e8deadbeef" == event.requestContext.requestId
        //assert "09/Apr/2015:12:34:56 +0000" == event.requestContext.requestTime
        //assert 1428582896000 == event.requestContext.requestTimeEpoch
        assert null == event.requestContext.identity.cognitoIdentityPoolId
        assert null == event.requestContext.identity.accountId
        assert null == event.requestContext.identity.cognitoIdentityId
        assert null == event.requestContext.identity.caller
        assert null == event.requestContext.identity.accessKey
        assert "127.0.0.1" == event.requestContext.identity.sourceIp
        assert null == event.requestContext.identity.cognitoAuthenticationType
        assert null == event.requestContext.identity.cognitoAuthenticationProvider
        assert null == event.requestContext.identity.userArn
        assert "Custom User Agent String" == event.requestContext.identity.userAgent
        assert null == event.requestContext.identity.user
        assert "/prod/path/to/resource" == event.requestContext.path
        assert "/{proxy+}" == event.requestContext.resourcePath
        assert "POST" == event.requestContext.httpMethod
        assert "1234567890" == event.requestContext.apiId
        //assert "HTTP/1.1" == event.requestContext.protocol
    }
}
