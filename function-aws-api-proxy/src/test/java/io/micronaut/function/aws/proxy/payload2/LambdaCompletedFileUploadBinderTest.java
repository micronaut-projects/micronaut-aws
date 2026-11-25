package io.micronaut.function.aws.proxy.payload2;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.annotation.Requires;
import io.micronaut.core.util.StringUtils;
import io.micronaut.function.aws.proxy.MockLambdaContext;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Consumes;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Put;
import io.micronaut.http.multipart.CompletedFileUpload;
import io.micronaut.json.JsonMapper;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LambdaCompletedFileUploadBinderTest {

    private static final String SPEC_NAME = "LambdaCompletedFileUploadBinderTest";
    private static final String PATH = "/upload";

    // A 1x1 PNG, including bytes that are not valid UTF-8 and a CRLF sequence
    private static final byte[] PNG = {
        (byte) 0x89, 'P', 'N', 'G', '\r', '\n', 0x1a, '\n', 0, 0, 0, 0x0d, 'I', 'H', 'D', 'R',
        0, 0, 0, 1, 0, 0, 0, 1, 8, 6, 0, 0, 0, 0x1f, 0x15, (byte) 0xc4, (byte) 0x89, (byte) 0xff, (byte) 0xfe
    };

    @Test
    void bindsBinaryFilePart() throws IOException {
        byte[] body = multipart("test-boundary",
            "--test-boundary\r\nContent-Disposition: form-data; name=\"file\"; filename=\"avatar.png\"\r\n"
                + "Content-Type: image/png\r\n\r\n", PNG, "\r\n--test-boundary--\r\n");
        APIGatewayV2HTTPResponse response = execute("multipart/form-data; boundary=test-boundary", body, Map.of());
        assertEquals(200, response.getStatusCode(), response.getBody());
        assertEquals("avatar.png:image/png:" + Base64.getEncoder().encodeToString(PNG), response.getBody());
    }

    @Test
    void bindsWithQuotedBoundaryOtherPartsAndLookalikeDelimiter() throws IOException {
        byte[] content = "before\r\n--test-boundary-not-a-delimiter\r\nafter".getBytes(StandardCharsets.US_ASCII);
        byte[] body = multipart("test-boundary",
            "preamble\r\n--test-boundary\r\nContent-Disposition: form-data; name=\"other\"\r\n\r\nignored\r\n"
                + "--test-boundary\r\nContent-Disposition: form-data; name=\"file\"; filename=\"a;b.png\"\r\n"
                + "Content-Type: image/png\r\n\r\n", content, "\r\n--test-boundary--\r\nepilogue");
        APIGatewayV2HTTPResponse response = execute("multipart/form-data; boundary=\"test-boundary\"", body, Map.of());
        assertEquals(200, response.getStatusCode(), response.getBody());
        assertEquals("a;b.png:image/png:" + Base64.getEncoder().encodeToString(content), response.getBody());
    }

    @Test
    void bindsEmptyFile() throws IOException {
        byte[] body = multipart("b",
            "--b\r\nContent-Disposition: form-data; name=\"file\"; filename=\"empty.png\"\r\n\r\n", new byte[0], "\r\n--b--\r\n");
        APIGatewayV2HTTPResponse response = execute("multipart/form-data; boundary=b", body, Map.of());
        assertEquals(200, response.getStatusCode(), response.getBody());
        assertEquals("empty.png:null:", response.getBody());
    }

    @Test
    void rejectsTruncatedBody() throws IOException {
        byte[] body = "--b\r\nContent-Disposition: form-data; name=\"file\"; filename=\"a.png\"\r\n\r\ndata"
            .getBytes(StandardCharsets.US_ASCII);
        assertEquals(400, execute("multipart/form-data; boundary=b", body, Map.of()).getStatusCode());
    }

    @Test
    void rejectsMissingBoundary() throws IOException {
        byte[] body = multipart("b",
            "--b\r\nContent-Disposition: form-data; name=\"file\"; filename=\"a.png\"\r\n\r\n", PNG, "\r\n--b--\r\n");
        assertEquals(400, execute("multipart/form-data", body, Map.of()).getStatusCode());
    }

    @Test
    void rejectsMissingFilePartAndFormFieldInsteadOfFile() throws IOException {
        byte[] other = multipart("b",
            "--b\r\nContent-Disposition: form-data; name=\"other\"; filename=\"a.png\"\r\n\r\n", PNG, "\r\n--b--\r\n");
        assertEquals(400, execute("multipart/form-data; boundary=b", other, Map.of()).getStatusCode());
        byte[] field = multipart("b",
            "--b\r\nContent-Disposition: form-data; name=\"file\"\r\n\r\n", PNG, "\r\n--b--\r\n");
        assertEquals(400, execute("multipart/form-data; boundary=b", field, Map.of()).getStatusCode());
    }

    @Test
    void rejectsOversizedFile() throws IOException {
        byte[] body = multipart("b",
            "--b\r\nContent-Disposition: form-data; name=\"file\"; filename=\"a.png\"\r\n\r\n", PNG, "\r\n--b--\r\n");
        Map<String, Object> config = Map.of("micronaut.server.multipart.max-file-size", PNG.length - 1);
        assertEquals(413, execute("multipart/form-data; boundary=b", body, config).getStatusCode());
        Map<String, Object> exact = Map.of("micronaut.server.multipart.max-file-size", PNG.length);
        assertEquals(200, execute("multipart/form-data; boundary=b", body, exact).getStatusCode());
    }

    private static byte[] multipart(String boundary, String head, byte[] content, String tail) {
        byte[] h = head.getBytes(StandardCharsets.UTF_8);
        byte[] t = tail.getBytes(StandardCharsets.UTF_8);
        byte[] result = Arrays.copyOf(h, h.length + content.length + t.length);
        System.arraycopy(content, 0, result, h.length, content.length);
        System.arraycopy(t, 0, result, h.length + content.length, t.length);
        return result;
    }

    private static APIGatewayV2HTTPResponse execute(String contentType, byte[] body, Map<String, Object> extraConfig) throws IOException {
        Map<String, Object> config = new HashMap<>(extraConfig);
        config.put("micronaut.security.enabled", StringUtils.FALSE);
        config.put("spec.name", SPEC_NAME);
        try (ApplicationContext ctx = ApplicationContext.builder().properties(config).build();
             APIGatewayV2HTTPEventFunction handler = new APIGatewayV2HTTPEventFunction(ctx)) {
            String json = """
                {"version":"2.0","rawPath":"%1$s","rawQueryString":"",
                 "headers":{"content-type":"%2$s"},
                 "requestContext":{"http":{"method":"PUT","path":"%1$s"}},
                 "body":"%3$s","isBase64Encoded":true}"""
                .formatted(PATH, contentType.replace("\"", "\\\""), Base64.getEncoder().encodeToString(body));
            APIGatewayV2HTTPEvent event = ctx.getBean(JsonMapper.class).readValue(json, APIGatewayV2HTTPEvent.class);
            return handler.handleRequest(event, new MockLambdaContext());
        }
    }

    @Requires(property = "spec.name", value = SPEC_NAME)
    @Controller
    static class UploadController {

        @Put(PATH)
        @Consumes(MediaType.MULTIPART_FORM_DATA)
        String upload(CompletedFileUpload file) throws IOException {
            return file.getFilename() + ":" + file.getContentType().map(MediaType::toString).orElse(null)
                + ":" + Base64.getEncoder().encodeToString(file.getBytes());
        }
    }
}
