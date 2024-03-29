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
import io.micronaut.serde.ObjectMapper;
import io.micronaut.core.annotation.Internal;

import java.io.IOException;
import java.util.Map;

/**
 * CloudWatch's implementation of the {@link JsonFormatter}.
 *
 * @author Nemanja Mikic
 * @since 3.9.0
 */
@Internal
public final class CloudWatchJsonFormatter implements JsonFormatter {
    private ObjectMapper objectMapper;

    @Override
    public String toJsonString(Map m) throws IOException {
        if (objectMapper == null) {
            objectMapper = ObjectMapper.getDefault();
        }
        return objectMapper.writeValueAsString(m);
    }

}
