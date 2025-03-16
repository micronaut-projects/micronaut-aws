/*
 * Copyright 2017-2025 original authors
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
package io.micronaut.function.aws.proxy.multipart;

import io.micronaut.core.annotation.Internal;
import io.micronaut.http.HttpHeaders;
import io.micronaut.http.multipart.CompletedFileUpload;

import java.nio.charset.Charset;
import java.util.*;

/**
 * Decodes multipart/form-data content from an HTTP request body into parameters (plaintext fields)
 * and uploads (binary data fields).
 */
@Internal
public final class MultipartDataDecoder {
    private static final String BOUNDARY_KEYWORD = "boundary=";
    private static final String CONTENT_DISPOSITION_HEADER = "content-disposition:";
    private static final String CONTENT_TYPE_HEADER = "content-type:";
    private static final String FILENAME_HEADER = "filename=";
    private static final String NAME_HEADER = "name=";
    private static final String CRLF = "\r\n";
    private static final String HEADER_END_MARKER = CRLF + CRLF;
    private final byte[] bodyBytes;
    private final String boundary;
    private final Charset charset;
    private Map<String, List<String>> params;
    private Map<String, CompletedFileUpload> fileUploads;

    /**
     * Creates a new decoder for multipart/form-data content.
     *
     * @param bodyBytes The raw request body as a byte array
     * @param httpHeaders The HTTP headers of the request.
     * @param charset The charset to use for text content
     */
    public MultipartDataDecoder(byte[] bodyBytes, HttpHeaders httpHeaders, Charset charset) {
        this.bodyBytes = bodyBytes;
        this.charset = charset;
        String contentType = httpHeaders.getContentType().orElse(null);

        this.boundary = contentType != null ? MultipartDataDecoder.extractBoundary(contentType) : "";
    }

    /**
     * Extract the boundary from a Content-Type header.
     * @param contentType The Content-Type header value
     * @return The boundary string or null if not found
     */
    private static String extractBoundary(String contentType) {
        int boundaryIndex = contentType.indexOf(BOUNDARY_KEYWORD);
        if (boundaryIndex > 0) {
            String boundary = contentType.substring(boundaryIndex + BOUNDARY_KEYWORD.length());
            // Handle quoted boundaries
            if (boundary.startsWith("\"") && boundary.endsWith("\"")) {
                boundary = boundary.substring(1, boundary.length() - 1);
            }
            // Handle additional parameters after boundary
            int paramSeparator = boundary.indexOf(';');
            if (paramSeparator > 0) {
                boundary = boundary.substring(0, paramSeparator);
            }
            return boundary;
        }
        return null;
    }

    /**
     * Returns the decoded form parameters from the multipart content.
     * @return A map of parameter names to values
     */
    public Map<String, List<String>> parameters() {
        if (params == null) {
            parseMultipartData();
        }
        return params;
    }

    /**
     * Returns the decoded file uploads from the multipart content.
     * @return A map of field names to file uploads
     */
    public Map<String, CompletedFileUpload> fileUploads() {
        if (fileUploads == null) {
            parseMultipartData();
        }
        return fileUploads;
    }

    /**
     * Extract the Content-Type from the headers of a multipart part.
     * @param headers The headers section of a multipart part
     * @return The content type or null if not found
     */
    private static String extractContentType(String headers) {
        String lowerHeaders = headers.toLowerCase();
        int ctIndex = lowerHeaders.indexOf(CONTENT_TYPE_HEADER);
        if (ctIndex < 0) {
            return null;
        }

        // Find the end of the header line
        int lineEnd = headers.indexOf(CRLF, ctIndex);
        if (lineEnd < 0) {
            lineEnd = headers.length();
        }

        // Extract the value part
        return headers.substring(ctIndex + CONTENT_TYPE_HEADER.length(), lineEnd).trim();
    }

    /**
     * Extract the filename from the Content-Disposition header.
     * @param multipartHeaders The headers section of a multipart part
     * @return The filename or null if not found
     */
    private static String extractFilename(String multipartHeaders) {
        String lowerHeaders = multipartHeaders.toLowerCase();
        // We can just skip over the content disposition header here; it's mainly for browsers
        int cdIndex = lowerHeaders.indexOf(CONTENT_DISPOSITION_HEADER);
        if (cdIndex < 0) {
            return null;
        }

        int filenameIndex = lowerHeaders.indexOf(FILENAME_HEADER, cdIndex);
        if (filenameIndex < 0) {
            return null;
        }

        // Extract the value (handling quotes)
        filenameIndex += FILENAME_HEADER.length();
        char quote = multipartHeaders.charAt(filenameIndex);
        if (quote == '"' || quote == '\'') {
            int endQuoteIndex = multipartHeaders.indexOf(quote, filenameIndex + 1);
            if (endQuoteIndex > 0) {
                return multipartHeaders.substring(filenameIndex + 1, endQuoteIndex);
            }
        } else {
            // Unquoted filename - ends at next space or semicolon
            int endIndex = -1;
            for (int i = filenameIndex; i < multipartHeaders.length(); i++) {
                char c = multipartHeaders.charAt(i);
                if (c == ' ' || c == ';' || c == '\r' || c == '\n') {
                    endIndex = i;
                    break;
                }
            }
            if (endIndex > 0) {
                return multipartHeaders.substring(filenameIndex, endIndex);
            }
        }

        return null;
    }

    /**
     * Extract the field name from the headers of a multipart part.
     * @param multipartHeaders The headers section of a multipart part
     * @return The field name or null if not found
     */
    private static String extractFieldName(String multipartHeaders) {
        int cdIndex = -1;
        String lowerHeaders = multipartHeaders.toLowerCase();
        int contentDispIndex = lowerHeaders.indexOf(CONTENT_DISPOSITION_HEADER);
        if (contentDispIndex >= 0) {
            cdIndex = contentDispIndex;
        }

        if (cdIndex < 0) {
            return null;
        }

        // Find the name parameter (case-insensitive)
        int nameIndex = lowerHeaders.indexOf(NAME_HEADER, cdIndex);
        if (nameIndex < 0) {
            return null;
        }

        // Extract the value (handling quotes)
        nameIndex += NAME_HEADER.length();
        char quote = multipartHeaders.charAt(nameIndex);
        if (quote == '"' || quote == '\'') {
            int endQuoteIndex = multipartHeaders.indexOf(quote, nameIndex + 1);
            if (endQuoteIndex > 0) {
                return multipartHeaders.substring(nameIndex + 1, endQuoteIndex);
            }
        } else {
            // Unquoted name - ends at next space or semicolon
            int endIndex = -1;
            for (int i = nameIndex; i < multipartHeaders.length(); i++) {
                char c = multipartHeaders.charAt(i);
                if (c == ' ' || c == ';' || c == '\r' || c == '\n') {
                    endIndex = i;
                    break;
                }
            }
            if (endIndex > 0) {
                return multipartHeaders.substring(nameIndex, endIndex);
            }
        }

        return null;
    }

    /**
     * Parse multipart/form-data content.
     */
    private void parseMultipartData() {
        Map<String, List<String>> parameters = new HashMap<>();
        Map<String, CompletedFileUpload> uploads = new LinkedHashMap<>();

        if (bodyBytes == null || bodyBytes.length == 0 || boundary.isEmpty()) {
            this.params = Collections.emptyMap();
            this.fileUploads = Collections.emptyMap();
            return;
        }

        // We need to find the boundary markers in the byte array directly
        // to preserve binary data integrity
        byte[] boundaryBytes = ("--" + boundary).getBytes(charset);

        // Find all boundary positions
        List<Integer> boundaryPositions = findByteSequence(bodyBytes, boundaryBytes);

        // Process each part
        for (int i = 0; i < boundaryPositions.size() - 1; i++) {
            int start = boundaryPositions.get(i) + boundaryBytes.length;
            int end = boundaryPositions.get(i + 1);

            // Skip empty parts
            if (end - start <= 4) {
                continue;
            }

            // Extract part content
            byte[] partBytes = new byte[end - start];
            System.arraycopy(bodyBytes, start, partBytes, 0, partBytes.length);

            // Find headers and content boundary
            int headerEnd = findFirstByteSequence(partBytes, HEADER_END_MARKER.getBytes(charset));
            if (headerEnd < 0) {
                continue;
            }

            // Extract headers as a string
            String multipartHeaders = new String(partBytes, 0, headerEnd, charset);

            // Extract the name from Content-Disposition header
            String name = extractFieldName(multipartHeaders);
            if (name == null) {
                continue;
            }

            // Extract content type from headers
            String contentType = extractContentType(multipartHeaders);

            // Skip past the header end marker
            int contentStart = headerEnd + HEADER_END_MARKER.length();
            int contentLength = partBytes.length - contentStart;

            // Skip empty content
            if (contentLength <= 0) {
                continue;
            }

            // Trim trailing CRLF if present
            if (contentLength >= 2 && partBytes[contentStart + contentLength - 2] == '\r' &&
                partBytes[contentStart + contentLength - 1] == '\n') {
                contentLength -= 2;
            }

            // Check if this is a file upload
            boolean isFile = multipartHeaders.contains(FILENAME_HEADER);

            if (isFile) {
                // Extract filename from headers
                String filename = extractFilename(multipartHeaders);
                if (filename != null) {
                    // Create byte array for file content
                    byte[] fileContent = new byte[contentLength];
                    System.arraycopy(partBytes, contentStart, fileContent, 0, contentLength);

                    // Create a file upload object and store it
                    SimpleCompletedFileUpload fileUpload = new SimpleCompletedFileUpload(name, filename, contentType, fileContent);
                    uploads.put(name, fileUpload);
                }
            } else {
                // For regular form fields, convert to string and add to parameters
                String content = new String(partBytes, contentStart, contentLength, charset);
                parameters.computeIfAbsent(name, k -> new ArrayList<>()).add(content);
            }
        }

        this.params = parameters;
        this.fileUploads = uploads;
    }

    /**
     * Find all occurrences of a byte sequence within a larger byte array.
     * @param data The data to search in
     * @param sequence The sequence to find
     * @return A list of starting positions for each occurrence
     */
    private static List<Integer> findByteSequence(byte[] data, byte[] sequence) {
        List<Integer> positions = new ArrayList<>();

        // Handle empty data or sequence
        if (data == null || sequence == null || data.length == 0 || sequence.length == 0 ||
            data.length < sequence.length) {
            return positions;
        }

        // Find all occurrences
        outer:
        for (int i = 0; i <= data.length - sequence.length; i++) {
            for (int j = 0; j < sequence.length; j++) {
                if (data[i + j] != sequence[j]) {
                    continue outer;
                }
            }
            positions.add(i);
        }

        return positions;
    }

    /**
     * Find the first occurrence of a byte sequence within a larger byte array.
     * @param data The data to search in
     * @param sequence The sequence to find
     * @return The starting position or -1 if not found
     */
    private static int findFirstByteSequence(byte[] data, byte[] sequence) {
        // Handle empty data or sequence
        if (data == null || sequence == null || data.length == 0 || sequence.length == 0 ||
            data.length < sequence.length) {
            return -1;
        }

        // Find first occurrence
        outer:
        for (int i = 0; i <= data.length - sequence.length; i++) {
            for (int j = 0; j < sequence.length; j++) {
                if (data[i + j] != sequence[j]) {
                    continue outer;
                }
            }
            return i;
        }

        return -1;
    }
}
