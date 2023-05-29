package io.micronaut.function.aws.runtime

import io.micronaut.context.ApplicationContext
import io.micronaut.function.aws.MicronautRequestHandler
import io.micronaut.http.HttpHeaders
import io.micronaut.runtime.server.EmbeddedServer
import spock.lang.Specification
import spock.util.environment.RestoreSystemProperties

class TracingHeaderPropagationSysPropertySpec extends Specification {

    @RestoreSystemProperties
    void "Tracing header propagated as system property"() {
        given:
        String traceHeader = 'Root=1-5759e988-bd862e3fe1be46a994272793;Sampled=1'
        EmbeddedServer embeddedServer = ApplicationContext.run(EmbeddedServer, [:])
        String serverUrl = "localhost:$embeddedServer.port"
        CustomMicronautLambdaRuntime customMicronautLambdaRuntime = new CustomMicronautLambdaRuntime(serverUrl)
        Thread t = new Thread({ ->
            customMicronautLambdaRuntime.run([] as String[])
        })
        t.start()

        when:
        def httpHeaders = Stub(HttpHeaders) {
            get(LambdaRuntimeInvocationResponseHeaders.LAMBDA_RUNTIME_TRACE_ID) >> traceHeader
        }
        customMicronautLambdaRuntime.propagateTraceId(httpHeaders)

        then:
        System.getProperty(MicronautRequestHandler.LAMBDA_TRACE_HEADER_PROP) == traceHeader

        cleanup:
        embeddedServer.close()
    }

    class CustomMicronautLambdaRuntime extends MicronautLambdaRuntime {

        String serverUrl

        CustomMicronautLambdaRuntime(String serverUrl) {
            super()
            this.serverUrl = serverUrl
        }

        @Override
        String getEnv(String name) {
            if (name == ReservedRuntimeEnvironmentVariables.AWS_LAMBDA_RUNTIME_API) {
                return serverUrl
            }
        }
    }
}
