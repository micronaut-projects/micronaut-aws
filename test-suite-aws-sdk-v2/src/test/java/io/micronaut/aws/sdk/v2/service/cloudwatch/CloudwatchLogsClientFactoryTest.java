package io.micronaut.aws.sdk.v2.service.cloudwatch;

import io.micronaut.aws.sdk.v2.service.cloudwatchlogs.CloudwatchLogsClientFactory;
import io.micronaut.context.BeanContext;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;

@MicronautTest(startApplication = false)
class CloudwatchLogsClientFactoryTest {
    @Inject
    BeanContext beanContext;
    @Test
    void beanOfTypeCloudwatchLogsClientFactoryDoesNotExists() {
        assertFalse(beanContext.containsBean(CloudwatchLogsClientFactory.class));
    }
}
