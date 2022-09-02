/*
 * Copyright 2017-2022 original authors
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
package io.micronaut.aws.cloudwatch.logging;

import ch.qos.logback.contrib.json.JsonFormatter;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micronaut.core.annotation.Internal;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;

/**
 * CloudWatch implementation of the {@link JsonFormatter}.
 *
 * @author Nemanja Mikic
 * @since 3.8.0
 */
@Internal
public final class CloudWatchJsonFormatter implements JsonFormatter {
    public static final int BUFFER_SIZE = 512;

    private ObjectMapper objectMapper;

    public CloudWatchJsonFormatter() {
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public String toJsonString(Map m) throws IOException {
        StringWriter writer = new StringWriter(BUFFER_SIZE);
        JsonGenerator generator = this.objectMapper.getFactory().createGenerator(writer);

        this.objectMapper.writeValue(generator, m);

        writer.flush();

        return writer.toString();
    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

}
