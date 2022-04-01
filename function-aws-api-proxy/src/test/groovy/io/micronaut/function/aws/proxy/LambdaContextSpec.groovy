package io.micronaut.function.aws.proxy

import com.amazonaws.serverless.proxy.internal.testutils.AwsProxyRequestBuilder
import com.amazonaws.serverless.proxy.model.AwsProxyResponse
import com.amazonaws.services.lambda.runtime.*
import io.micronaut.context.ApplicationContext
import io.micronaut.context.ApplicationContextBuilder
import io.micronaut.context.BeanProvider
import io.micronaut.context.annotation.Any
import io.micronaut.context.annotation.Requires
import io.micronaut.core.annotation.NonNull
import io.micronaut.core.util.CollectionUtils
import io.micronaut.http.HttpMethod
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Produces
import jakarta.inject.Inject
import jakarta.inject.Singleton
import spock.lang.Specification

class LambdaContextSpec extends Specification {

    void "Lambda Context beans are registered"() {
        given:
        Map<String, Object> properties = CollectionUtils.mapOf('micronaut.security.enabled', false, "spec.name", "LambdaContextSpec")
        ApplicationContextBuilder ctxBuilder = ApplicationContext.builder().properties(properties)
        MicronautLambdaContainerHandler handler = new MicronautLambdaContainerHandler(ctxBuilder)

        when:
        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder('/context', HttpMethod.GET.toString())
        AwsProxyResponse response = handler.proxy(builder.build(), createContext())

        then:
        handler.applicationContext.containsBean(Context)
        handler.applicationContext.containsBean(LambdaLogger)
        handler.applicationContext.containsBean(CognitoIdentity)
        handler.applicationContext.containsBean(ClientContext)
        "XXX" == response.body
    }

    void "verify LambdaLogger CognitoIdentity and ClientContext are not registered if null"() {
        given:
        Map<String, Object> properties = CollectionUtils.mapOf('micronaut.security.enabled', false, "spec.name", "LambdaContextSpec")
        ApplicationContextBuilder ctxBuilder = ApplicationContext.builder().properties(properties)
        MicronautLambdaContainerHandler handler = new MicronautLambdaContainerHandler(ctxBuilder)

        when:
        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder('/context', HttpMethod.GET.toString())
        AwsProxyResponse response = handler.proxy(builder.build(), createContextWithoutCollaborators())

        then:
        handler.applicationContext.containsBean(Context)
        and: 'LambdaLogger is not registered if Lambda Context::getLambdaLogger is null'
        !handler.applicationContext.containsBean(LambdaLogger)

        and: 'CognitoIdentity is not registered if Lambda Context::getIdentity is null"'
        !handler.applicationContext.containsBean(CognitoIdentity)

        and: 'ClientContext is not registered if Lambda Context::getClientContext is null"'
        !handler.applicationContext.containsBean(ClientContext)

        and:
        "XXX" == response.body
    }

    static interface RequestIdProvider {
        @NonNull
        Optional<String> requestId();
    }

    @Requires(property = "spec.name", value = "LambdaContextSpec")
    @Singleton
    static class DefaultRequestIdProvider implements RequestIdProvider {
        private final BeanProvider<Context> context

        DefaultRequestIdProvider(@Any BeanProvider<Context> context) {
            this.context = context
        }

        @Override
        @NonNull
        Optional<String> requestId() {
            context.isPresent() ? Optional.of(context.get().awsRequestId) : Optional.empty()
        }
    }

    @Requires(property = "spec.name", value = "LambdaContextSpec")
    @Controller("/context")
    static class LambdaContextSpecController {
        @Inject
        RequestIdProvider requestIdProvider

        @Produces(MediaType.TEXT_PLAIN)
        @Get
        String index() {
            requestIdProvider.requestId().orElse("not found")
        }
    }

    static Context createContextWithoutCollaborators() {
        new Context() {
            @Override
            String getAwsRequestId() {
                'XXX'
            }

            @Override
            String getLogGroupName() {
                null
            }

            @Override
            String getLogStreamName() {
                null
            }

            @Override
            String getFunctionName() {
                null
            }

            @Override
            String getFunctionVersion() {
                null
            }

            @Override
            String getInvokedFunctionArn() {
                null
            }

            @Override
            CognitoIdentity getIdentity() {
                null
            }

            @Override
            ClientContext getClientContext() {
                null
            }

            @Override
            int getRemainingTimeInMillis() {
                return 0
            }

            @Override
            int getMemoryLimitInMB() {
                return 0
            }

            @Override
            LambdaLogger getLogger() {
                null
            }
        }
    }
    static Context createContext() {
        return new Context() {
            @Override
            String getAwsRequestId() {
                return "XXX"
            }

            @Override
            String getLogGroupName() {
                null
            }

            @Override
            String getLogStreamName() {
                null
            }

            @Override
            String getFunctionName() {
                null
            }

            @Override
            String getFunctionVersion() {
                null
            }

            @Override
            String getInvokedFunctionArn() {
                null
            }

            @Override
            CognitoIdentity getIdentity() {
                new CognitoIdentity() {
                    @Override
                    String getIdentityId() {
                        return "identityIDXXX"
                    }

                    @Override
                    String getIdentityPoolId() {
                        return "identityPoolIdXXX"
                    }
                }
            }

            @Override
            ClientContext getClientContext() {
                new ClientContext() {
                    @Override
                    Client getClient() {
                        return new Client() {
                            @Override
                            String getInstallationId() {
                                return "installationId"
                            }

                            @Override
                            String getAppTitle() {
                                null
                            }

                            @Override
                            String getAppVersionName() {
                                null
                            }

                            @Override
                            String getAppVersionCode() {
                                null
                            }

                            @Override
                            String getAppPackageName() {
                                null
                            }
                        }
                    }

                    @Override
                    Map<String, String> getCustom() {
                        null
                    }

                    @Override
                    Map<String, String> getEnvironment() {
                        null
                    }
                }
            }

            @Override
            int getRemainingTimeInMillis() {
                return 0
            }

            @Override
            int getMemoryLimitInMB() {
                return 0
            }

            @Override
            LambdaLogger getLogger() {
                return new LambdaLogger() {
                    @Override
                    void log(String message) {

                    }

                    @Override
                    void log(byte[] message) {

                    }
                }
            }
        }
    }
}
