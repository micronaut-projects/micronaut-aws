package io.micronaut.function.aws.test;

import io.micronaut.context.annotation.Property;
import io.micronaut.context.annotation.Requires;
import io.micronaut.function.aws.test.annotation.MicronautLambdaTest;
import io.micronaut.test.annotation.MockBean;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@MicronautLambdaTest
@Property(name = "spec.name", value = "MicronautLambdaExtensionMockBeanTest")
class MicronautLambdaExtensionMockBeanTest {

    @Inject
    FriendlySingleton friendlySingleton;

    @Inject
    TestHandler testHandler;

    @MockBean(FriendlySingleton.class)
    FriendlySingleton mockFriendlySingleton() {
        return mock(FriendlySingleton.class);
    }

    @Test
    void testSingletonIsSingle() throws Exception {
        when(friendlySingleton.hello("world")).thenReturn("hello world");
        assertEquals("hello world", testHandler.execute("world"));
        verify(friendlySingleton).hello("world");
    }

    @Singleton
    @Requires(property = "spec.name", value = "MicronautLambdaExtensionMockBeanTest")
    static class TestHandler {

        private final FriendlySingleton singleton;

        TestHandler(FriendlySingleton singleton) {
            this.singleton = singleton;
        }

        public String execute(String input) {
            return singleton.hello(input);
        }
    }

    interface FriendlySingleton {
        String hello(String name);
    }
}
