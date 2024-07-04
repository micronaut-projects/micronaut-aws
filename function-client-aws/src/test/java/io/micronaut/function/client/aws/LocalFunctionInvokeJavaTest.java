package io.micronaut.function.client.aws;

import io.micronaut.context.ApplicationContext;
import static org.junit.Assert.assertEquals;
import io.micronaut.runtime.server.EmbeddedServer;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

class LocalFunctionInvokeJavaTest {

    @Test
    void testInvokingALocalFunction() {
        Suma sum = new Suma();
        sum.setA(5);
        sum.setB(10);

        EmbeddedServer server = ApplicationContext.run(EmbeddedServer.class);
        MathClient mathClient = server.getApplicationContext().getBean(MathClient.class);

        assertEquals(Long.valueOf(Integer.MAX_VALUE), mathClient.max());
        assertEquals(2, mathClient.rnd(1.6f));
        assertEquals(15, mathClient.sum(sum));

        server.close();
    }

    @Test
    void testInvokingALocalFunctionRX() {
        Suma sum = new Suma();
        sum.setA(5);
        sum.setB(10);

        EmbeddedServer server = ApplicationContext.run(EmbeddedServer.class);
        ReactiveMathClient mathClient = server.getApplicationContext().getBean(ReactiveMathClient.class);

        assertEquals(Long.valueOf(Integer.MAX_VALUE), Mono.from(mathClient.max()).block());
        assertEquals(2, Mono.from(mathClient.rnd(1.6f)).block().longValue());
        assertEquals(15, Mono.from(mathClient.sum(sum)).block().longValue());

        server.close();
    }
}
