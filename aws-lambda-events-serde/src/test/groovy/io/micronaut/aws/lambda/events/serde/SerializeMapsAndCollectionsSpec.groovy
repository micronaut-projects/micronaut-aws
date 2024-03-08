package io.micronaut.aws.lambda.events.serde

import io.micronaut.json.JsonMapper
import spock.lang.Specification

class SerializeMapsAndCollectionsSpec extends Specification {

    JsonMapper objectMapper = CustomPojoSerializerUtils.getJsonMapper()

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
