package io.micronaut.function.aws.proxy.encoding;

import jakarta.inject.Singleton;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;

@Singleton
public class GZipContentEncoder implements ContentEncoder {

    @Override
    public String getName() {
        return "gzip";
    }

    @Override
    public byte[] encodeContent(byte[] body) {
        if (body == null || body.length == 0) {
            return new byte[0];
        }
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             GZIPOutputStream gzip = new GZIPOutputStream(baos)) {
            gzip.write(body);
            gzip.finish();
            return baos.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to gzip content", e);
        }
    }
}
