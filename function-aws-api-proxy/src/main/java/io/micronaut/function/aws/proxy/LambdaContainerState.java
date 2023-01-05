package io.micronaut.function.aws.proxy;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micronaut.context.ApplicationContext;
import io.micronaut.jackson.codec.JsonMediaTypeCodec;
import io.micronaut.web.router.Router;

/**
 * Holds state for the running container.
 */
class LambdaContainerState implements MicronautLambdaContainerContext {
    private Router router;
    private ApplicationContext applicationContext;
    private JsonMediaTypeCodec jsonCodec;
    private ObjectMapper objectMapper;

    @Override
    public Router getRouter() {
        return router;
    }

    @Override
    public JsonMediaTypeCodec getJsonCodec() {
        return jsonCodec;
    }

    @Override
    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    @Override
    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    void setJsonCodec(JsonMediaTypeCodec jsonCodec) {
        this.jsonCodec = jsonCodec;
    }

    void setRouter(Router router) {
        this.router = router;
    }

    void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }
}
