package io.micronaut.function.aws.proxy.encoding;

import jakarta.inject.Singleton;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.DeflaterOutputStream;

@Singleton
public class DeflateContentEncoder implements ContentEncoder {

    @Override
    public String getName() {
        return "deflate";
    }

    @Override
    public byte[] encodeContent(byte[] body) {
        if (body == null || body.length == 0) {
            return new byte[0];
        }
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             DeflaterOutputStream deflate = new DeflaterOutputStream(baos)) {
            deflate.write(body);
            deflate.finish();
            return baos.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to deflate content", e);
        }
    }
}
