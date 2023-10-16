package io.micronaut.aws.lambda.events.tests;

import com.amazonaws.services.lambda.runtime.CustomPojoSerializer;
import com.amazonaws.services.lambda.runtime.serialization.PojoSerializer;
import com.amazonaws.services.lambda.runtime.serialization.events.LambdaEventSerializers;

import java.util.Iterator;
import java.util.ServiceLoader;

public final class CustomPojoSerializerUtils {

    private CustomPojoSerializerUtils() {
    }

    public static CustomPojoSerializer loadSerializer()  {

        ServiceLoader<CustomPojoSerializer> loader = ServiceLoader.load(CustomPojoSerializer.class);
        Iterator<CustomPojoSerializer> serializers = loader.iterator();

        if (!serializers.hasNext()) {
            return null;
        }

        return serializers.next();
    }

    public static <T> PojoSerializer<T> loadSerializer(Class<T> eventClass) {
        return LambdaEventSerializers.serializerFor(eventClass, CustomPojoSerializerUtils.class.getClassLoader());
    }

    public static <T> T serializeFromJson(String input, Class<T> eventClass) {
        CustomPojoSerializer customPojoSerializer = loadSerializer();
        if (customPojoSerializer != null) {
            return customPojoSerializer.fromJson(input, eventClass);
        }

        PojoSerializer<T> pojoSerializer = loadSerializer(eventClass);
        return pojoSerializer.fromJson(input);
    }
}
