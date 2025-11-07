package io.micronaut.function.aws.proxy.encoding;

public class PassThroughContentEncoder implements ContentEncoder{
    @Override
    public String getName() {
        return "passthrough";
    }

    @Override
    public byte[] encodeContent(byte[] body) {
        return body;
    }
}
