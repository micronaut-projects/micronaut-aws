package example;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.Callable;

import static org.junit.jupiter.api.Assertions.assertTrue;

@MicronautTest
public class LoggingTest {

    public static final Logger log = LoggerFactory.getLogger("LoggingTest");

    @Inject
    CustomLogsClient customLogsClient;

    @Test
    void test() {
        log.info("test message");

        List<String> loggedMessages = customLogsClient.getLoggedMessages();

        waitUntil(() -> !loggedMessages.isEmpty(), 10, 1);

        String lastMessage = loggedMessages.get(loggedMessages.size() - 1);
        assertTrue(lastMessage.contains("\"message\":\"test message\""));
    }

    private void waitUntil(Callable<Boolean> conditionEvaluator, int timeoutSeconds, int sleepTimeSeconds) {
        Exception lastException = null;

        long end  = System.currentTimeMillis() + timeoutSeconds * 1000L;
        while (System.currentTimeMillis() < end) {
            try {
                Thread.sleep(sleepTimeSeconds * 1000L);
            } catch (InterruptedException e) {
                // continue
            }
            try {
                if (conditionEvaluator.call()) {
                    return;
                }
            } catch (Exception e) {
                lastException = e;
            }
        }
        String errorMessage = "Condition was not fulfilled within " + timeoutSeconds + " seconds";
        throw lastException == null ? new IllegalStateException(errorMessage) : new IllegalStateException(errorMessage, lastException);
    }
}
