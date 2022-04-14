/*
 * Copyright 2022 original authors
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

package io.micronaut.aws.lambda.events.serde

import io.micronaut.context.BeanContext
import io.micronaut.serde.ObjectMapper
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import spock.lang.Specification

@MicronautTest(startApplication = false)
class SerializeMapsAndCollectionsSpec extends Specification {

    @Inject
    ObjectMapper objectMapper

    @Inject
    BeanContext beanContext

    void "Serialize map with values that are of type ArrayList"() {
        given:
        ArrayList list = new ArrayList()
        list.add("bar")
        Map<String, List<String>> map = Collections.singletonMap("foo", list)

        when:
        String serializedMap = objectMapper.writeValueAsString(map)

        then:
        Map deserializedMap = objectMapper.readValue(serializedMap, Map)
        "bar" == deserializedMap.get("foo").get(0);
    }

}
