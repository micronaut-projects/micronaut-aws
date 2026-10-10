package io.micronaut.aws.sdk.v2.dev

import io.micronaut.aws.sdk.v2.CredentialsAndRegionFactory
import io.micronaut.aws.sdk.v2.EnvironmentAwsCredentialsProvider
import io.micronaut.aws.sdk.v2.EnvironmentAwsRegionProvider
import io.micronaut.context.ApplicationContext
import io.micronaut.context.BeanLocator
import io.micronaut.context.BeanRegistration
import io.micronaut.context.env.DevelopmentMode
import io.micronaut.context.reload.BeanRetentionPolicy
import io.micronaut.core.value.PropertyResolver
import io.micronaut.inject.BeanDefinition
import io.micronaut.inject.BeanIdentifier
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.AwsCredentialsProviderChain
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.awscore.retry.AwsRetryStrategy
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration
import software.amazon.awssdk.core.interceptor.ExecutionInterceptor
import software.amazon.awssdk.core.retry.RetryPolicy
import software.amazon.awssdk.core.retry.backoff.BackoffStrategy
import software.amazon.awssdk.core.retry.conditions.RetryCondition
import software.amazon.awssdk.metrics.MetricPublisher
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.regions.providers.AwsRegionProviderChain
import software.amazon.awssdk.retries.api.RetryStrategy
import software.amazon.awssdk.services.sqs.SqsClient
import spock.lang.Specification
import spock.lang.Unroll

import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.ThreadFactory
import java.util.function.Predicate

class DevelopmentCredentialsAndRegionSpec extends Specification {

    void "in development mode the chains copy the aws configuration and hold neither the environment nor the context"() {
        given:
        ApplicationContext context = ApplicationContext.run((DevelopmentMode.PROPERTY): 'true')

        expect:
        [AwsCredentialsProviderChain, AwsRegionProviderChain].each { Class<?> type ->
            BeanDefinition<?> definition = context.getBeanDefinition(type)
            assert definition.declaringType.get() == DevelopmentCredentialsAndRegionFactory
            assert !definition.requiredComponents.any { PropertyResolver.isAssignableFrom(it) || BeanLocator.isAssignableFrom(it) }
        }
        context.containsBean(DevelopmentAwsRetentionPolicy)

        cleanup:
        context.close()
    }

    void "outside development mode the chains read the environment as before"() {
        given:
        ApplicationContext context = ApplicationContext.run()

        expect:
        context.getBeanDefinition(AwsCredentialsProviderChain).declaringType.get() == CredentialsAndRegionFactory
        context.getBeanDefinition(AwsRegionProviderChain).declaringType.get() == CredentialsAndRegionFactory
        !context.containsBean(AwsEnvironmentSnapshot)
        !context.containsBean(DevelopmentAwsRetentionPolicy)

        cleanup:
        context.close()
    }

    @Unroll
    void "the copied providers resolve what the environment providers resolve for #properties"() {
        given:
        Map<String, Object> configuration = new LinkedHashMap<>(properties)
        configuration.put(DevelopmentMode.PROPERTY, 'true')
        // outside the test environment, whose configuration supplies credentials
        ApplicationContext context = ApplicationContext.builder(configuration).deduceEnvironment(false).start()
        AwsEnvironmentSnapshot snapshot = context.getBean(AwsEnvironmentSnapshot)
        def environmentCredentials = EnvironmentAwsCredentialsProvider.create(context.environment)
        def copiedCredentials = new CopiedAwsCredentialsProvider(snapshot)

        expect:
        resolve { copiedCredentials.resolveCredentials() } == resolve { environmentCredentials.resolveCredentials() }
        new CopiedAwsRegionProvider(snapshot).region == new EnvironmentAwsRegionProvider(context.environment).region
        resolve { copiedCredentials.resolveCredentials() } == expected

        cleanup:
        context.close()

        where:
        properties                                                                                    | expected
        ['aws.access-key-id': 'id', 'aws.secret-key': 'secret']                                       | AwsBasicCredentials.create('id', 'secret')
        ['aws.accessKeyId': 'id', 'aws.secretKey': 'secret', 'aws.sessionToken': 'token']             | 'AwsSessionCredentials(id, secret, token)'
        ['aws.access-key': 'id', 'aws.secret-access-key': 'secret']                                   | AwsBasicCredentials.create('id', 'secret')
        ['aws.access-key-id': 'id', 'aws.access-key': 'other', 'aws.secret-key': 'secret',
         'aws.secret-access-key': 'other']                                                            | AwsBasicCredentials.create('id', 'secret')
        ['aws.access-key-id': ' id ', 'aws.secret-key': ' secret ', 'aws.session-token': ' token ']   | 'AwsSessionCredentials(id, secret, token)'
        ['aws.access-key-id': 'id']                                                                   | 'SdkClientException'
        ['aws.access-key-id': '  ', 'aws.secret-key': 'secret']                                       | 'SdkClientException'
        ['aws.region': 'eu-west-1']                                                                   | 'SdkClientException'
        [:]                                                                                           | 'SdkClientException'
    }

    void "the copied region provider gives the configured region, or none"() {
        given:
        ApplicationContext context = ApplicationContext.run((DevelopmentMode.PROPERTY): 'true', 'aws.region': 'eu-west-1')

        expect:
        new CopiedAwsRegionProvider(context.getBean(AwsEnvironmentSnapshot)).region == Region.EU_WEST_1
        new CopiedAwsRegionProvider(new AwsEnvironmentSnapshot()).region == null

        cleanup:
        context.close()
    }

    void "the policy refuses a client holding a class of the application and abstains on one of the SDK alone"() {
        given:
        ApplicationContext context = ApplicationContext.run((DevelopmentMode.PROPERTY): 'true')
        DevelopmentAwsRetentionPolicy policy = context.getBean(DevelopmentAwsRetentionPolicy)
        Class<?> applicationType = new ApplicationLoader(ApplicationInterceptor.classLoader).loadClass(ApplicationInterceptor.name)
        Object application = applicationType.getDeclaredConstructor().newInstance()
        SqsClient plain = client(null)
        SqsClient intercepted = client((ExecutionInterceptor) application)

        expect:
        policy.decide(registration(context, plain)) == BeanRetentionPolicy.Decision.ABSTAIN
        policy.decide(registration(context, intercepted)) == BeanRetentionPolicy.Decision.REFUSE
        policy.decide(registration(context, application)) == BeanRetentionPolicy.Decision.REFUSE
        policy.decide(registration(context, new ApplicationInterceptor())) == BeanRetentionPolicy.Decision.ABSTAIN
        policy.decide(registration(context, 'not an AWS bean')) == BeanRetentionPolicy.Decision.ABSTAIN

        cleanup:
        plain?.close()
        intercepted?.close()
        context.close()
    }

    @Unroll
    void "the policy refuses a client whose override configuration holds an application #kind"() {
        given:
        ApplicationContext context = ApplicationContext.run((DevelopmentMode.PROPERTY): 'true')
        DevelopmentAwsRetentionPolicy policy = context.getBean(DevelopmentAwsRetentionPolicy)
        SqsClient client = SqsClient.builder()
            .region(Region.US_EAST_1)
            .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create('id', 'secret')))
            .overrideConfiguration(override)
            .build()

        expect:
        policy.decide(registration(context, client)) == BeanRetentionPolicy.Decision.REFUSE

        cleanup:
        client?.close()
        context.close()

        where:
        kind                 | override
        'metric publisher'   | { ClientOverrideConfiguration.Builder it -> it.addMetricPublisher(applicationClass(MetricPublisher)) }
        'retry strategy'     | { ClientOverrideConfiguration.Builder it -> it.retryStrategy(applicationClass(RetryStrategy)) }
        'retry condition'    | { ClientOverrideConfiguration.Builder it -> it.retryPolicy(RetryPolicy.builder().retryCondition(applicationClass(RetryCondition)).build()) }
        'backoff strategy'   | { ClientOverrideConfiguration.Builder it -> it.retryPolicy(RetryPolicy.builder().backoffStrategy(applicationClass(BackoffStrategy)).build()) }
        'retry predicate'    | { ClientOverrideConfiguration.Builder it -> it.retryStrategy(AwsRetryStrategy.standardRetryStrategy().toBuilder().retryOnException(applicationClass(Predicate)).build()) }
        'thread factory'     | { ClientOverrideConfiguration.Builder it -> it.scheduledExecutorService(new ScheduledThreadPoolExecutor(1, applicationClass(ThreadFactory))) }
        'scheduled executor' | { ClientOverrideConfiguration.Builder it -> it.scheduledExecutorService(applicationClass(ScheduledExecutorService)) }
    }

    void "the policy abstains on a client whose override configuration holds only what the SDK and the JDK built"() {
        given:
        ApplicationContext context = ApplicationContext.run((DevelopmentMode.PROPERTY): 'true')
        DevelopmentAwsRetentionPolicy policy = context.getBean(DevelopmentAwsRetentionPolicy)
        ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1)
        SqsClient client = SqsClient.builder()
            .region(Region.US_EAST_1)
            .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create('id', 'secret')))
            .overrideConfiguration { ClientOverrideConfiguration.Builder it ->
                it.retryStrategy(AwsRetryStrategy.standardRetryStrategy().toBuilder().retryOnExceptionInstanceOf(IOException).build())
                it.retryPolicy(RetryPolicy.builder().numRetries(2).build())
                it.scheduledExecutorService(executor)
            }
            .build()

        expect:
        policy.decide(registration(context, client)) == BeanRetentionPolicy.Decision.ABSTAIN

        cleanup:
        client?.close()
        executor.shutdownNow()
        context.close()
    }

    /**
     * An implementation of an interface whose class is defined by a loader below the one of the SDK, as the loader of
     * the application's classes would.
     */
    private static <T> T applicationClass(Class<T> type) {
        ClassLoader application = new URLClassLoader(new URL[0], DevelopmentCredentialsAndRegionSpec.classLoader)
        return type.cast(Proxy.newProxyInstance(application, [type] as Class<?>[], { Object proxy, Method method, Object[] args ->
            switch (method.name) {
                case 'hashCode': return System.identityHashCode(proxy)
                case 'equals': return proxy.is(args[0])
                case 'toString': return 'application ' + type.simpleName
                default: return method.isDefault() ? InvocationHandler.invokeDefault(proxy, method, args) : null
            }
        } as InvocationHandler))
    }

    private static Object resolve(Closure<?> resolution) {
        try {
            def credentials = resolution.call()
            if (credentials instanceof software.amazon.awssdk.auth.credentials.AwsSessionCredentials) {
                return "AwsSessionCredentials(${credentials.accessKeyId()}, ${credentials.secretAccessKey()}, ${credentials.sessionToken()})".toString()
            }
            return credentials
        } catch (software.amazon.awssdk.core.exception.SdkClientException e) {
            assert e.message.startsWith('Unable to load AWS credentials from environment')
            return 'SdkClientException'
        }
    }

    private static SqsClient client(ExecutionInterceptor interceptor) {
        def builder = SqsClient.builder()
            .region(Region.US_EAST_1)
            .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create('id', 'secret')))
        if (interceptor != null) {
            builder.overrideConfiguration { it.addExecutionInterceptor(interceptor) }
        }
        return builder.build()
    }

    private static BeanRegistration<?> registration(ApplicationContext context, Object bean) {
        BeanDefinition definition = context.getBeanDefinition(AwsRegionProviderChain)
        return BeanRegistration.of(context, BeanIdentifier.of('test'), definition, bean)
    }

    /**
     * Defines {@link ApplicationInterceptor} again, as the loader of the application's classes would.
     */
    static class ApplicationLoader extends ClassLoader {
        ApplicationLoader(ClassLoader parent) {
            super(parent)
        }

        @Override
        protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
            if (name == ApplicationInterceptor.name) {
                synchronized (getClassLoadingLock(name)) {
                    Class<?> loaded = findLoadedClass(name)
                    if (loaded == null) {
                        byte[] bytes = parent.getResourceAsStream(name.replace('.', '/') + '.class').bytes
                        loaded = defineClass(name, bytes, 0, bytes.length)
                    }
                    return loaded
                }
            }
            return super.loadClass(name, resolve)
        }
    }
}
