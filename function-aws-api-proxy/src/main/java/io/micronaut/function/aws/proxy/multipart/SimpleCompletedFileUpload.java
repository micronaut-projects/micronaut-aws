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

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.MediaType;
import io.micronaut.http.multipart.CompletedFileUpload;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Optional;

/**
 * A simple implementation of {@link CompletedFileUpload} for use with API Gateway requests.
 */
class SimpleCompletedFileUpload implements CompletedFileUpload {
    private final String name;
    private final String filename;
    private final MediaType contentType;
    private final byte[] content;
    private final long size;

    /**
     * Constructs a new SimpleCompletedFileUpload.
     *
     * @param name The name of the file field
     * @param filename The client-side filename
     * @param contentType The content type as a string
     * @param content The file content as bytes
     */
    public SimpleCompletedFileUpload(
            @NonNull String name,
            @NonNull String filename,
            @Nullable String contentType,
            @NonNull byte[] content) {
        this.name = name;
        this.filename = filename;
        this.contentType = contentType != null ? MediaType.of(contentType) : MediaType.APPLICATION_OCTET_STREAM_TYPE;
        this.content = content;
        this.size = content.length;
    }

    @Override
    @NonNull
    public String getName() {
        return name;
    }

    @Override
    @NonNull
    public String getFilename() {
        return filename;
    }

    @Override
    @NonNull
    public Optional<MediaType> getContentType() {
        return Optional.of(contentType);
    }

    @Override
    @NonNull
    public ByteBuffer getByteBuffer() throws IOException {
        return ByteBuffer.wrap(content);
    }

    @Override
    @NonNull
    public InputStream getInputStream() throws IOException {
        return new ByteArrayInputStream(content);
    }

    @Override
    public long getSize() {
        return size;
    }

    @Override
    public long getDefinedSize() {
        return size;
    }

    @Override
    @NonNull
    public byte[] getBytes() throws IOException {
        return content;
    }

    @Override
    public boolean isComplete() {
        return true;
    }

    @Override
    public void discard() {
        // No resources to release - data is held in memory
    }
}
