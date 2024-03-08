package io.micronaut.aws.lambda.events.serde

import com.amazonaws.services.lambda.runtime.CustomPojoSerializer
import io.micronaut.serde.ObjectMapper

import javax.naming.ConfigurationException

class CustomPojoSerializerUtils {

    private static List<CustomPojoSerializer> customPojoSerializers() {
        List<CustomPojoSerializer> services = new ArrayList<>()
        ServiceLoader<CustomPojoSerializer> loader = ServiceLoader.load(CustomPojoSerializer.class)
        loader.forEach(services::add)
        return services
    }

    static CustomPojoSerializer customPojoSerializer() {
        return customPojoSerializers()
                .stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No CustomPojoSerializer found"))
    }

    static ObjectMapper getObjectMapper() {
        CustomPojoSerializer pojoSerializer = CustomPojoSerializerUtils.customPojoSerializer()
        if (pojoSerializer instanceof SerdeCustomPojoSerializer) {
            return  ((SerdeCustomPojoSerializer) pojoSerializer).getJsonMapper()
        }
        throw new ConfigurationException("CustomPojoSerializer no type of SerdeCustomPojoSerializer")
    }
}
