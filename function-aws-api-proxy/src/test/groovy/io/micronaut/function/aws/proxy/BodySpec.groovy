package io.micronaut.function.aws.proxy

import com.amazonaws.serverless.proxy.internal.testutils.AwsProxyRequestBuilder
import com.amazonaws.serverless.proxy.internal.testutils.MockLambdaContext
import com.amazonaws.serverless.proxy.model.AwsProxyResponse
import com.amazonaws.services.lambda.runtime.Context
import com.fasterxml.jackson.databind.ObjectMapper
import delight.fileupload.FileUpload
import groovy.transform.Canonical
import io.micronaut.context.ApplicationContext
import io.micronaut.context.annotation.Requires
import io.micronaut.core.async.annotation.SingleResult
import io.micronaut.core.io.Writable
import io.micronaut.http.HttpHeaders
import io.micronaut.http.HttpMethod
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpStatus
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Header
import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.Status
import org.apache.commons.fileupload.FileItem
import org.reactivestreams.Publisher
import spock.lang.AutoCleanup
import spock.lang.Shared
import spock.lang.Specification

import io.micronaut.core.annotation.Nullable;
import java.nio.charset.Charset
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

class BodySpec extends Specification {

    @Shared @AutoCleanup MicronautLambdaContainerHandler handler = new MicronautLambdaContainerHandler(
            ApplicationContext.builder().properties([
                    'micronaut.security.enabled': false,
                    'spec.name': 'BodySpec'
            ])
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

    void "test plain text as binary"() {
        given:
        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder('/response-body/bytes', HttpMethod.POST.toString())
        builder.header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN)
        builder.binaryBody(new ByteArrayInputStream('Hello'.bytes))

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == 201
        response.body == 'Hello'
    }

    void "test plain text uplaod"() {
        given:
        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder('/response-body/multipart', HttpMethod.POST.toString())
        builder.formFieldPart('name', 'Vlad')
        builder.formFilePart('file', 'greetings.txt', 'Hello'.bytes)

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == 201
        response.body == 'Hello Vlad from greetings.txt'
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

    void "test singleValuesHeaders"() {

        given:
        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder().fromJsonString(getSingleValueRequestJson())

        def objectMapper = handler.getApplicationContext().getBean(ObjectMapper)
        def bytes = objectMapper.writeValueAsBytes(builder.build())
        def output = new ByteArrayOutputStream()

        when:
        handler.proxyStream(new ByteArrayInputStream(bytes), output, lambdaContext)
        def response = objectMapper.readValue(output.toByteArray(), AwsProxyResponse)
        then:
        response.statusCode == 201
        response.getBody() == "Root=1-62e22402-3a5f246225e45edd7735c182"
    }

    private String getSingleValueRequestJson() {
        return """{
        "requestContext": {
            "elb": {
                "targetGroupArn": "arn:aws:elasticloadbalancing:us-east-2:123456789012:targetgroup/prod-example-function/e77803ebb6d2c24"
            }
        },
        "httpMethod": "GET",
        "path": "/response-body/singeHeaders",
        "queryStringParameters": {},
        "headers": {
            "accept": "*",
            "content-length": "17",
            "content-type": "application/json",
            "host": "stackoverflow.name",
            "user-agent": "curl/7.77.0",
            "x-amzn-trace-id": "Root=1-62e22402-3a5f246225e45edd7735c182",
            "x-forwarded-for": "24.14.13.186",
            "x-forwarded-port": "443",
            "x-forwarded-proto": "https",
            "x-jersey-tracing-accept": "true"
        },
        "body": null,
        "isBase64Encoded": false
}
"""
    }

    @Controller('/response-body')
    @Requires(property = 'spec.name', value = 'BodySpec')
    static class BodyController {

        @Post(uri = "/pojo")
        @Status(HttpStatus.CREATED)
        Point post(@Body Point data) {
            return data
        }

        @Post(uri = "/bytes", consumes = MediaType.TEXT_PLAIN)
        @Status(HttpStatus.CREATED)
        String postBytes(@Body byte[] bytes) {
            return new String(bytes)
        }

        @Get(uri = "/singeHeaders")
        @Status(HttpStatus.CREATED)
        String singeHeaders(@Header String xAmznTraceId) {
            return xAmznTraceId
        }


        @Post(uri = "/multipart", consumes = MediaType.MULTIPART_FORM_DATA)
        @Status(HttpStatus.CREATED)
        String postMultipart(@Body byte[] bytes, @Header String contentType) {
            List<FileItem> items = FileUpload.parse(bytes, contentType)
            String name = null
            String text = null
            String fileName = null

            for (FileItem item : items) {
                switch (item.fieldName) {
                    case 'name':
                        name = item.string
                        break;
                    case 'file':
                        text = item.inputStream.text
                        fileName = item.name
                        break;
                }
            }

            return "$text $name from $fileName"
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
