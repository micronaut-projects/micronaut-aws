package io.micronaut.function.aws.test;

import io.micronaut.context.ApplicationContext;
import io.micronaut.function.aws.MicronautRequestHandler;
import io.micronaut.function.aws.test.annotation.MicronautLambdaTest;
import io.micronaut.test.annotation.MockBean;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@MicronautLambdaTest
class MicronautLambdaExtensionMockBeanTest {

    @Inject
    ApplicationContext context;

    @Inject
    EagerSingleton eagerSingleton;

    @Test
    void testSingletonIsSingle() throws Exception {
        try (TestHandler handler = new TestHandler(context)) {
            when(eagerSingleton.hello("world")).thenReturn("hello world");
            when(eagerSingleton.hello("world")).thenReturn("hello world");
            assertEquals("hello world", handler.execute("world"));
            verify(eagerSingleton).hello("world");
        }
    }

    @MockBean(EagerSingleton.class)
    EagerSingleton eagerSingleton() {
        return mock(EagerSingleton.class);
    }

    static class TestHandler extends MicronautRequestHandler<String, String> {

        @Inject
        EagerSingleton singleton;

        //used in AWS
        public TestHandler() {
        }

        //used in tests
        public TestHandler(ApplicationContext applicationContext) {
            super(applicationContext);
        }

        @Override
        public String execute(String input) {
            return singleton.hello(input);
        }
    }
}
