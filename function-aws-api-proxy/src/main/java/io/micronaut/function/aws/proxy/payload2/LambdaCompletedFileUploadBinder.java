/*
 * Copyright 2017-2026 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micronaut.function.aws.proxy.payload2;

import io.micronaut.context.annotation.Value;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.bind.ArgumentBinder;
import io.micronaut.core.bind.annotation.Bindable;
import io.micronaut.core.convert.ArgumentConversionContext;
import io.micronaut.core.io.buffer.ReadBufferFactory;
import io.micronaut.core.type.Argument;
import io.micronaut.http.HttpHeaders;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.bind.binders.TypedRequestArgumentBinder;
import io.micronaut.http.exceptions.HttpStatusException;
import io.micronaut.http.multipart.CompletedFileUpload;
import io.micronaut.http.multipart.FormFieldMetadata;
import io.micronaut.http.server.HttpServerConfiguration;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Binds multipart file parts from API Gateway V2 events to {@link CompletedFileUpload} arguments.
 *
 * <p>A Lambda event carries the complete request body, so there is no HTTP server involved that could parse it.
 * This binder parses the {@code multipart/form-data} framing (RFC 7578 / RFC 2046) directly from the decoded body
 * bytes, so it depends on neither a particular HTTP server nor any additional library. Requests that did not
 * originate from a Lambda event are passed to the original binder, when one was available.</p>
 *
 * <p>The configured {@code micronaut.server.multipart.max-file-size} is enforced against the selected part before it
 * is copied. This is not a streaming request memory limit: the Lambda event already holds the whole body in memory,
 * and a successful upload copies the selected part once more.</p>
 *
 * @since 5.2.0
 */
@Internal
@Singleton
public class LambdaCompletedFileUploadBinder implements TypedRequestArgumentBinder<CompletedFileUpload> {

    private static final String FORM_DATA = "form-data";
    private static final String NAME = "name";
    private static final String FILENAME = "filename";
    private static final byte[] CRLF = {'\r', '\n'};
    private static final byte[] HEADERS_END = {'\r', '\n', '\r', '\n'};
    private static final byte[] CLOSE = {'-', '-'};
    private static final int MAX_BOUNDARY_LENGTH = 70;

    private final long maxFileSize;
    private final ArgumentBinder<CompletedFileUpload, HttpRequest<?>> delegate;

    /**
     * @param maxFileSize The maximum size of an uploaded file, in bytes
     */
    @Inject
    public LambdaCompletedFileUploadBinder(
        @Value("${micronaut.server.multipart.max-file-size:"
            + HttpServerConfiguration.MultipartConfiguration.DEFAULT_MAX_FILE_SIZE + "}") long maxFileSize
    ) {
        this(maxFileSize, (context, request) -> BindingResult.unsatisfied());
    }

    /**
     * @param maxFileSize The maximum size of an uploaded file, in bytes
     * @param delegate    The binder for requests that are not Lambda events
     */
    public LambdaCompletedFileUploadBinder(long maxFileSize, ArgumentBinder<CompletedFileUpload, HttpRequest<?>> delegate) {
        this.maxFileSize = maxFileSize;
        this.delegate = delegate;
    }

    @Override
    public Argument<CompletedFileUpload> argumentType() {
        return Argument.of(CompletedFileUpload.class);
    }

    @Override
    public BindingResult<CompletedFileUpload> bind(ArgumentConversionContext<CompletedFileUpload> context, HttpRequest<?> request) {
        if (!(request instanceof APIGatewayV2HTTPEventServletRequest<?> lambdaRequest)) {
            return delegate.bind(context, request);
        }
        if (request.getContentType().filter(type -> type.equals(MediaType.MULTIPART_FORM_DATA_TYPE)).isEmpty()) {
            return BindingResult.unsatisfied();
        }
        String name = context.getArgument().getAnnotationMetadata().stringValue(Bindable.class)
            .orElse(context.getArgument().getName());
        byte[] body;
        try {
            body = lambdaRequest.getBodyBytes();
        } catch (IOException e) {
            return BindingResult.empty();
        }
        String boundary = parseParameters(request.getHeaders().get(HttpHeaders.CONTENT_TYPE)).get("boundary");
        if (boundary == null || boundary.isEmpty() || boundary.length() > MAX_BOUNDARY_LENGTH) {
            throw invalidMultipart();
        }
        Part part = findPart(body, ("--" + boundary).getBytes(StandardCharsets.ISO_8859_1), name);
        if (part == null || part.filename == null) {
            return BindingResult.empty();
        }
        long length = (long) part.end - part.start;
        if (length > maxFileSize) {
            throw new HttpStatusException(HttpStatus.REQUEST_ENTITY_TOO_LARGE, "File exceeds maximum size");
        }
        MediaType mediaType = null;
        if (part.contentType != null) {
            try {
                mediaType = MediaType.of(part.contentType);
            } catch (IllegalArgumentException e) {
                // an unusable part content type is treated as absent
            }
        }
        CompletedFileUpload result = CompletedFileUpload.ofMemory(
            new FormFieldMetadata(name, part.filename, mediaType),
            ReadBufferFactory.getJdkFactory().copyOf(ByteBuffer.wrap(body, part.start, part.end - part.start)));
        return () -> Optional.of(result);
    }

    /**
     * Scans the whole body for the first part named {@code name}. The entire body is validated, including the closing
     * delimiter, so truncated requests are rejected.
     *
     * @return The first matching part, or {@code null} if there is none
     */
    private static Part findPart(byte[] body, byte[] delimiter, String name) {
        Part selected = null;
        int delimiterStart = indexOfDelimiter(body, delimiter, 0);
        if (delimiterStart < 0) {
            throw invalidMultipart();
        }
        while (true) {
            int pos = delimiterStart + delimiter.length;
            if (startsWith(body, pos, CLOSE)) {
                return selected;
            }
            // Skip transport padding after the delimiter
            while (pos < body.length && (body[pos] == ' ' || body[pos] == '\t')) {
                pos++;
            }
            if (!startsWith(body, pos, CRLF)) {
                throw invalidMultipart();
            }
            pos += CRLF.length;
            String headerBlock;
            int contentStart;
            if (startsWith(body, pos, CRLF)) {
                headerBlock = "";
                contentStart = pos + CRLF.length;
            } else {
                int headersEnd = indexOf(body, HEADERS_END, pos);
                if (headersEnd < 0) {
                    throw invalidMultipart();
                }
                headerBlock = new String(body, pos, headersEnd - pos, StandardCharsets.UTF_8);
                contentStart = headersEnd + HEADERS_END.length;
            }
            int nextDelimiter = indexOfDelimiter(body, delimiter, contentStart);
            if (nextDelimiter < 0) {
                throw invalidMultipart();
            }
            if (selected == null) {
                // The CRLF preceding the delimiter belongs to the delimiter, not the content
                selected = toPartIfNamed(headerBlock, name, contentStart, Math.max(contentStart, nextDelimiter - CRLF.length));
            }
            delimiterStart = nextDelimiter;
        }
    }

    private static Part toPartIfNamed(String headerBlock, String name, int start, int end) {
        String disposition = null;
        String contentType = null;
        for (String line : headerBlock.split("\r\n")) {
            int colon = line.indexOf(':');
            if (colon <= 0) {
                continue;
            }
            String headerName = line.substring(0, colon).trim();
            String value = line.substring(colon + 1).trim();
            if (disposition == null && headerName.equalsIgnoreCase(HttpHeaders.CONTENT_DISPOSITION)) {
                disposition = value;
            } else if (contentType == null && headerName.equalsIgnoreCase(HttpHeaders.CONTENT_TYPE)) {
                contentType = value;
            }
        }
        if (disposition == null) {
            return null;
        }
        int semicolon = disposition.indexOf(';');
        String type = (semicolon < 0 ? disposition : disposition.substring(0, semicolon)).trim();
        Map<String, String> parameters = parseParameters(disposition);
        if (!FORM_DATA.equalsIgnoreCase(type) || !name.equals(parameters.get(NAME))) {
            return null;
        }
        return new Part(parameters.get(FILENAME), contentType, start, end);
    }

    /**
     * Parses the {@code ;name=value} parameters of a header value. Values may be quoted, in which case they may
     * contain semicolons and backslash-escaped quotes. Names are lower-cased; the first occurrence wins.
     */
    private static Map<String, String> parseParameters(String header) {
        Map<String, String> parameters = new HashMap<>();
        if (header == null) {
            return parameters;
        }
        int length = header.length();
        int i = header.indexOf(';');
        while (i >= 0 && i < length) {
            i++;
            int end = i;
            while (end < length && header.charAt(end) != '=' && header.charAt(end) != ';') {
                end++;
            }
            String key = header.substring(i, end).trim().toLowerCase(Locale.ROOT);
            String value = "";
            if (end < length && header.charAt(end) == '=') {
                end++;
                while (end < length && Character.isWhitespace(header.charAt(end))) {
                    end++;
                }
                if (end < length && header.charAt(end) == '"') {
                    StringBuilder quoted = new StringBuilder();
                    end++;
                    while (end < length && header.charAt(end) != '"') {
                        char c = header.charAt(end);
                        if (c == '\\' && end + 1 < length && (header.charAt(end + 1) == '"' || header.charAt(end + 1) == '\\')) {
                            c = header.charAt(++end);
                        }
                        quoted.append(c);
                        end++;
                    }
                    value = quoted.toString();
                    while (end < length && header.charAt(end) != ';') {
                        end++;
                    }
                } else {
                    int valueStart = end;
                    while (end < length && header.charAt(end) != ';') {
                        end++;
                    }
                    value = header.substring(valueStart, end).trim();
                }
            }
            if (!key.isEmpty()) {
                parameters.putIfAbsent(key, value);
            }
            i = end;
        }
        return parameters;
    }

    /**
     * Finds a delimiter line: the delimiter at the start of the body or after a CRLF, followed by either the close
     * marker or optional padding and a CRLF. Text that merely begins with the delimiter is not a delimiter.
     */
    private static int indexOfDelimiter(byte[] body, byte[] delimiter, int from) {
        int index = indexOf(body, delimiter, from);
        while (index >= 0) {
            boolean lineStart = index == 0 || index >= CRLF.length && startsWith(body, index - CRLF.length, CRLF);
            if (lineStart && isDelimiterEnd(body, index + delimiter.length)) {
                return index;
            }
            index = indexOf(body, delimiter, index + 1);
        }
        return -1;
    }

    private static boolean isDelimiterEnd(byte[] body, int pos) {
        if (startsWith(body, pos, CLOSE)) {
            return true;
        }
        while (pos < body.length && (body[pos] == ' ' || body[pos] == '\t')) {
            pos++;
        }
        return startsWith(body, pos, CRLF);
    }

    private static boolean startsWith(byte[] body, int pos, byte[] prefix) {
        if (pos < 0 || pos + prefix.length > body.length) {
            return false;
        }
        for (int i = 0; i < prefix.length; i++) {
            if (body[pos + i] != prefix[i]) {
                return false;
            }
        }
        return true;
    }

    private static int indexOf(byte[] body, byte[] target, int from) {
        int last = body.length - target.length;
        for (int i = Math.max(0, from); i <= last; i++) {
            if (startsWith(body, i, target)) {
                return i;
            }
        }
        return -1;
    }

    private static HttpStatusException invalidMultipart() {
        return new HttpStatusException(HttpStatus.BAD_REQUEST, "Invalid multipart request");
    }

    /**
     * A located part: the half-open range {@code [start, end)} of the body holding its content.
     *
     * @param filename    The {@code filename} of the part, or {@code null} for a plain form field
     * @param contentType The part's {@code Content-Type}, if any
     * @param start       The index of the first content byte
     * @param end         The index after the last content byte
     */
    private record Part(String filename, String contentType, int start, int end) {
    }
}
