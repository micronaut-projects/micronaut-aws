/*
 * Copyright 2017-2019 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micronaut.discovery.aws.parameterstore

import io.micronaut.context.ApplicationContext
import io.micronaut.context.env.Environment
import io.micronaut.runtime.server.EmbeddedServer
import reactor.core.publisher.Flux
import software.amazon.awssdk.services.ssm.SsmAsyncClient
import software.amazon.awssdk.services.ssm.model.GetParametersByPathRequest
import software.amazon.awssdk.services.ssm.model.GetParametersByPathResponse
import software.amazon.awssdk.services.ssm.model.GetParametersRequest
import software.amazon.awssdk.services.ssm.model.GetParametersResponse
import software.amazon.awssdk.services.ssm.model.Parameter
import spock.lang.AutoCleanup
import spock.lang.Shared
import spock.lang.Specification

import java.util.concurrent.CompletableFuture

class AWSParameterStoreLegacyCharacterizationSpec extends Specification {

    static {
        System.setProperty("aws.region", "us-west-1")
    }

    @AutoCleanup
    @Shared
    EmbeddedServer embeddedServer = ApplicationContext.run(EmbeddedServer,
        [
            'aws.client.system-manager.parameterstore.enabled'     : 'true',
            'aws.system-manager.parameterstore.useSecureParameters': 'false',
            'micronaut.application.name'                           : 'amazonTest'
        ],
        Environment.AMAZON_EC2
    )

    void "legacy parameter store client requires AMAZON_EC2 environment"() {
        when:
        ApplicationContext nonEc2Context = ApplicationContext.run([
            'aws.client.system-manager.parameterstore.enabled': 'true',
            'micronaut.application.name'                      : 'amazonTest'
        ])

        then:
        !nonEc2Context.containsBean(AWSParameterStoreConfiguration)
        !nonEc2Context.containsBean(AWSParameterStoreConfigClient)

        cleanup:
        nonEc2Context.close()
    }

    void "legacy parameter store query ordering preserves application then environment precedence"() {
        given:
        AWSParameterStoreConfigClient client = embeddedServer.applicationContext.getBean(AWSParameterStoreConfigClient)
        client.client = Mock(SsmAsyncClient)

        List<GetParametersRequest> nameRequests = []
        List<GetParametersByPathRequest> pathRequests = []

        client.client.getParameters(_) >> { GetParametersRequest request ->
            nameRequests << request
            ArrayList<Parameter> parameters = new ArrayList<>()
            if (request.names().contains("/config/application_test")) {
                parameters.add(Parameter.builder()
                    .name("/config/application_test/foo")
                    .value("bar")
                    .type("String")
                    .build())
            }
            CompletableFuture.completedFuture(GetParametersResponse.builder().parameters(parameters).build())
        }
        client.client.getParametersByPath(_) >> { GetParametersByPathRequest request ->
            pathRequests << request
            ArrayList<Parameter> parameters = new ArrayList<>()
            if (request.path() == "/config/application") {
                parameters.add(Parameter.builder()
                    .name("/config/application/encryptedValue")
                    .value("true")
                    .type("String")
                    .build())
            }
            CompletableFuture.completedFuture(GetParametersByPathResponse.builder().parameters(parameters).build())
        }

        when:
        def environment = Mock(Environment)
        environment.getActiveNames() >> (['test'] as Set)
        def propertySources = Flux.from(client.getPropertySources(environment)).collectList().block()

        then:
        propertySources*.name == ['route53-application', 'route53-application[test]']
        propertySources*.order == [-99, -50]
        propertySources[1].order > propertySources[0].order
        propertySources[0].get('encryptedValue') == 'true'
        propertySources[1].get('foo') == 'bar'

        nameRequests*.names().flatten() == [
            '/config/application',
            '/config/amazon-test',
            '/config/application_test',
            '/config/amazon-test_test'
        ]
        pathRequests*.path() == [
            '/config/application',
            '/config/amazon-test',
            '/config/application_test',
            '/config/amazon-test_test'
        ]
    }

    void "legacy parameter store can disable active environment search and accept a custom query provider"() {
        given:
        EmbeddedServer customServer = ApplicationContext.run(EmbeddedServer,
            [
                'aws.client.system-manager.parameterstore.enabled'                 : 'true',
                'aws.client.system-manager.parameterstore.searchActiveEnvironments': 'false',
                'micronaut.application.name'                                       : 'amazonTest'
            ],
            Environment.AMAZON_EC2
        )
        AWSParameterStoreConfigClient client = customServer.applicationContext.getBean(AWSParameterStoreConfigClient)
        client.client = Mock(SsmAsyncClient)

        List<String> defaultSearchedPaths = []
        List<String> defaultSearchedNames = []
        client.client.getParametersByPath(_) >> { GetParametersByPathRequest request ->
            defaultSearchedPaths << request.path()
            CompletableFuture.completedFuture(GetParametersByPathResponse.builder().build())
        }
        client.client.getParameters(_) >> { GetParametersRequest request ->
            defaultSearchedNames.addAll(request.names())
            ArrayList<Parameter> parameters = new ArrayList<>()
            if (request.names().contains('/config/application')) {
                parameters.add(Parameter.builder()
                    .name('/config/application/someKey')
                    .value('someValue')
                    .type('String')
                    .build())
            }
            CompletableFuture.completedFuture(GetParametersResponse.builder().parameters(parameters).build())
        }

        def environment = Mock(Environment)
        environment.getActiveNames() >> (['first', 'second'] as Set)

        when:
        def propertySources = Flux.from(client.getPropertySources(environment)).collectList().block()

        then:
        propertySources*.name == ['route53-application']
        propertySources[0].get('someKey') == 'someValue'
        defaultSearchedPaths == ['/config/application', '/config/amazon-test']
        defaultSearchedNames == ['/config/application', '/config/amazon-test']

        when:
        client.queryProvider = { env, serviceId, configuration ->
            [
                new ParameterQuery('/root/application', '/root/application', -1),
                new ParameterQuery('/config/foo', '/config/foo', -10)
            ]
        }

        List<String> customSearchedPaths = []
        client.client = Mock(SsmAsyncClient)
        client.client.getParameters(_) >> { GetParametersRequest request ->
            CompletableFuture.completedFuture(GetParametersResponse.builder().build())
        }
        client.client.getParametersByPath(_) >> { GetParametersByPathRequest request ->
            customSearchedPaths << request.path()
            GetParametersByPathResponse.Builder response = GetParametersByPathResponse.builder()
            if (request.path().startsWith('/root/application')) {
                response.parameters([
                    Parameter.builder()
                        .name('/root/application/someKey')
                        .value('someValue')
                        .type('String')
                        .build()
                ])
            }
            CompletableFuture.completedFuture(response.build())
        }

        def customPropertySources = Flux.from(client.getPropertySources(environment)).collectList().block()

        then:
        customPropertySources*.name == ['route53-/root/application']
        customPropertySources[0].get('someKey') == 'someValue'
        customSearchedPaths == ['/root/application', '/config/foo']

        cleanup:
        customServer.close()
    }
}
