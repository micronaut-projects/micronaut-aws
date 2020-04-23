package io.micronaut.function.aws.proxy

import com.amazonaws.serverless.proxy.internal.testutils.AwsProxyRequestBuilder
import com.amazonaws.serverless.proxy.internal.testutils.MockLambdaContext
import com.amazonaws.serverless.proxy.model.AwsProxyResponse
import com.amazonaws.services.lambda.runtime.Context
import com.fasterxml.jackson.databind.ObjectMapper
import groovy.transform.Canonical
import io.micronaut.context.ApplicationContext
import io.micronaut.core.io.Writable
import io.micronaut.http.HttpHeaders
import io.micronaut.http.HttpMethod
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpStatus
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.Status
import io.micronaut.security.annotation.Secured
import io.micronaut.security.rules.SecurityRule
import io.reactivex.Single
import spock.lang.AutoCleanup
import spock.lang.Shared
import spock.lang.Specification

import javax.annotation.Nullable
import java.nio.charset.Charset
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream


class BodySpec extends Specification {

    @Shared @AutoCleanup MicronautLambdaContainerHandler handler = new MicronautLambdaContainerHandler(
                ApplicationContext.build()
    )
    @Shared Context lambdaContext = new MockLambdaContext()

    void "test writable"() {
        given:
        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder('/response-body/writable', HttpMethod.GET.toString())

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == 201
        response.isBase64Encoded()
        response.getMultiValueHeaders().getFirst(HttpHeaders.CONTENT_TYPE) == 'application/zip'
        // should be base64
        isZip(Base64.getMimeDecoder().decode(response.body))

    }

    void "test custom body POJO"() {
        given:
        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder('/response-body/pojo', HttpMethod.POST.toString())
        builder.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
        builder.body('{"x":10,"y":20}')

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == 201
        response.body == '{"x":10,"y":20}'

    }

    void "test readFor"() {
        given:
        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder('/response-body/pojo', HttpMethod.POST.toString())
        builder.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
        builder.body('{"x":10,"y":20}')
        def objectMapper = handler.getApplicationContext().getBean(ObjectMapper)
        def bytes = objectMapper.writeValueAsBytes(builder.build())
        def output = new ByteArrayOutputStream()


        when:
        handler.proxyStream(new ByteArrayInputStream(bytes), output, lambdaContext)
        def response = objectMapper.readValue(output.toByteArray(), AwsProxyResponse)
        then:
        response.statusCode == 201
        response.body == '{"x":10,"y":20}'

    }


    void "test custom body POJO - default to JSON"() {
        given:
        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder('/response-body/pojo', HttpMethod.POST.toString())
        builder.body('{"x":10,"y":20}')

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == 201
        response.body == '{"x":10,"y":20}'

    }

    void "test custom body POJO with whole request"() {
        given:
        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder('/response-body/pojo-and-request', HttpMethod.POST.toString())
        builder.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
        builder.body('{"x":10,"y":20}')

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == 201
        response.body == '{"x":10,"y":20}'

    }

    void "test custom body POJO - reactive types"() {
        given:
        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder('/response-body/pojo-reactive', HttpMethod.POST.toString())
        builder.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
        builder.body('{"x":10,"y":20}')

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == 201
        response.body == '{"x":10,"y":20}'
    }

    @Secured(SecurityRule.IS_ANONYMOUS)
    @Controller('/response-body')
    static class BodyController {

        @Post(uri = "/pojo")
        @Status(HttpStatus.CREATED)
        Point post(@Body Point data) {
            return data
        }

        @Post(uri = "/pojo-and-request")
        @Status(HttpStatus.CREATED)
        Point postRequest(HttpRequest<Point> request) {
            return request.body.orElse(null)
        }


        @Post(uri = "/pojo-reactive")
        @Status(HttpStatus.CREATED)
        Single<Point> post(@Body Single<Point> data) {
            return data
        }
        @Get(uri = "/writable", produces = "application/zip")
        @Status(HttpStatus.CREATED)
        Writable writable() {
            new Writable() {
                @Override
                void writeTo(OutputStream outputStream, @Nullable Charset charset) throws IOException {
                    ZipOutputStream zipOut = new ZipOutputStream(outputStream)
                    def entry = new ZipEntry("test")
                    zipOut.putNextEntry(entry);
                    outputStream.write("test 1".bytes)
                    zipOut.closeEntry();
                    outputStream.flush()
                }

                @Override
                void writeTo(Writer out) throws IOException {
                    // no-op
                }
            }
        }
    }

    @Canonical
    static class Point {
        Integer x,y
    }

    /**
     * Are the given bytes a zip file.
     * @param bytes The bytes
     * @return True if they are
     */
    static boolean isZip(byte[] bytes) {
        if (bytes != null) {
            return new ZipInputStream(new ByteArrayInputStream(bytes)).withCloseable { zipInputStream ->
                return zipInputStream.getNextEntry() != null
            }
        }
        return false;
    }
}
