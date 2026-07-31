package io.micronaut.function.aws.proxy.encoding;

import io.micronaut.core.annotation.Indexed;

@Indexed(ContentEncoder.class)
public interface ContentEncoder {
    String getName();

    byte[] encodeContent(byte[] body);
}
