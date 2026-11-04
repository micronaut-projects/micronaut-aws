package io.micronaut.aws.dev;

import io.micronaut.context.ApplicationContext;
import io.micronaut.dev.tck.ReloadHarness;
import io.micronaut.dev.tck.ReloadTck;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;
import software.amazon.awssdk.http.SdkHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Runs an application using the {@link SqsClient} of micronaut-aws through the development runtime, against
 * LocalStack, and edits it. The client, which no longer holds the environment through its credentials and region
 * providers, is the same instance after a restart; a change under {@code aws} releases it, while the SDK HTTP client,
 * whose own configuration did not change, is kept for the new client. Nothing of a retired generation stays
 * reachable.
 */
class SqsClientReloadTest {

    private static final String PROBE = """
        package example;

        import jakarta.inject.Singleton;
        import software.amazon.awssdk.services.sqs.SqsClient;
        import software.amazon.awssdk.services.sqs.model.CreateQueueRequest;

        @Singleton
        public class Probe {
            private final SqsClient client;

            public Probe(SqsClient client) {
                this.client = client;
            }

            public String createQueue(String name) {
                return "%s " + client.createQueue(CreateQueueRequest.builder().queueName(name).build()).queueUrl();
            }
        }
        """;

    private static LocalStackContainer localStack;

    @TempDir
    Path project;

    @AfterAll
    static void stopLocalStack() {
        if (localStack != null) {
            localStack.stop();
        }
    }

    @Test
    void aRestartKeepsTheClientAndAChangeUnderAwsReleasesIt() throws Exception {
        LocalStackContainer localStack = localStack();
        Map<String, String> properties = new LinkedHashMap<>();
        properties.put("aws.region", localStack.getRegion());
        properties.put("aws.access-key-id", localStack.getAccessKey());
        properties.put("aws.secret-key", localStack.getSecretKey());
        properties.put("aws.services.sqs.endpoint-override", localStack.getEndpoint().toString());
        try (ReloadHarness harness = ReloadHarness.inDirectory(project)) {
            properties.forEach(harness::property);
            harness.source("example.Probe", PROBE.formatted("first"));
            harness.start();
            assertTrue(createQueue(harness.context(), "dev-reload-one").startsWith("first "));
            SqsClient client = harness.context().getBean(SqsClient.class);
            SdkHttpClient httpClient = harness.context().getBean(SdkHttpClient.class);

            // a class changed: the application restarts, and the client is adopted by the new generation
            harness.source("example.Probe", PROBE.formatted("second"));
            harness.reload();
            assertEquals(2, harness.generation());
            ReloadTck.assertRetained(harness, client);
            assertSame(client, harness.context().getBean(SqsClient.class));
            assertTrue(createQueue(harness.context(), "dev-reload-two").startsWith("second "));

            // the region changes, together with a class: the client is made again, from the new region, on the
            // HTTP client that was kept
            StringBuilder changed = new StringBuilder();
            properties.forEach((key, value) -> {
                if (!key.equals("aws.region")) {
                    changed.append(key).append('=').append(value).append('\n');
                }
            });
            changed.append("aws.region=eu-west-1\n");
            harness.resource("application.properties", changed.toString());
            harness.source("example.Probe", PROBE.formatted("third"));
            harness.reload();
            assertEquals(3, harness.generation());
            SqsClient released = harness.context().getBean(SqsClient.class);
            assertNotSame(client, released, "a change under aws releases the client");
            assertEquals(Region.EU_WEST_1, released.serviceClientConfiguration().region());
            ReloadTck.assertRetained(harness, httpClient);
            assertTrue(createQueue(harness.context(), "dev-reload-three").startsWith("third "));
            client = null;
            released = null;
            httpClient = null;

            ReloadTck.assertRetiredGenerationsCollected(harness);
        }
    }

    private static String createQueue(ApplicationContext context, String name) {
        try {
            Class<?> type = Class.forName("example.Probe", true, context.getClassLoader());
            return (String) type.getMethod("createQueue", String.class).invoke(context.getBean(type), name);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError("Cannot call the probe of the application", e);
        }
    }

    private static synchronized LocalStackContainer localStack() {
        if (localStack == null) {
            localStack = new LocalStackContainer(DockerImageName.parse("localstack/localstack:4.14.0"))
                .withServices(LocalStackContainer.Service.SQS)
                .waitingFor(Wait.forHttp("/_localstack/health").forStatusCode(200));
            // SQS needs no Docker socket, which LocalStack mounts for other services and which Podman cannot mount
            localStack.setBinds(new ArrayList<>());
            localStack.start();
        }
        return localStack;
    }
}
