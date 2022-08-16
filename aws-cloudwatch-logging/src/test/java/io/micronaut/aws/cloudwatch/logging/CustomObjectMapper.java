package io.micronaut.aws.cloudwatch.logging;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.TimeZone;

public class CustomObjectMapper extends ObjectMapper {

    public CustomObjectMapper() {
        super();
        setTimeZone(TimeZone.getDefault());
    }

}
