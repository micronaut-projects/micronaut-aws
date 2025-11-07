package io.micronaut.function.aws.proxy.encoding;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Singleton;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Singleton
public class EncodingService {
    private static final Logger LOG = Logger.getLogger(EncodingService.class.getName());
    private final List<ContentEncoder> encoders;
    private Map<String, ContentEncoder> encodersByName;

    public EncodingService(final List<ContentEncoder> encoders) {
        this.encoders = encoders;
    }

    @PostConstruct
    void afterPropertiesSet() {
        encodersByName = encoders.stream().collect(
            Collectors.toMap(ContentEncoder::getName, e -> e)
        );
    }

    public boolean supportsEncoding(String contentEncoding) {
        return encodersByName.containsKey(contentEncoding);
    }

    public ContentEncoder getContentEncoder(String encoding) {
        return encodersByName.get(encoding);
    }
}
