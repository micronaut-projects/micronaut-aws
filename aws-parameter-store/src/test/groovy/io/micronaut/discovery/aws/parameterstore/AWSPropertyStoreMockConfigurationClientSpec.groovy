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
import io.micronaut.context.env.EnvironmentPropertySource
import io.micronaut.context.env.PropertySource
import io.micronaut.context.env.PropertySourcePropertyResolver
import io.micronaut.core.order.OrderUtil
import io.micronaut.runtime.server.EmbeddedServer
import io.reactivex.Flowable
import software.amazon.awssdk.services.ssm.SsmAsyncClient
import software.amazon.awssdk.services.ssm.model.*
import spock.lang.AutoCleanup
import spock.lang.Shared
import spock.lang.Specification

import java.util.concurrent.CompletableFuture

/**
 * Test for mocking of aws property store.
 * @author RVanderwerf
 */
class AWSPropertyStoreMockConfigurationClientSpec extends Specification {

    static {
        System.setProperty("aws.region", "us-west-1")
    }

    @AutoCleanup
    @Shared
    EmbeddedServer embeddedServer = ApplicationContext.run(EmbeddedServer.class,
            [
                    'aws.client.system-manager.parameterstore.enabled'     : 'true',
                    'aws.system-manager.parameterstore.useSecureParameters': 'false',
                    'micronaut.application.name'                           : 'amazonTest'],
            Environment.AMAZON_EC2

    )

    @Shared
    AWSParameterStoreConfigClient client = embeddedServer.applicationContext.getBean(AWSParameterStoreConfigClient)


    def setup() {
        client.client = Mock(SsmAsyncClient)
    }

    void "test discovery property sources from AWS Systems Manager Parameter Store - StringList"() {

        given:



        client.client.getParametersByPath(_) >> { GetParametersByPathRequest getRequest ->
            ArrayList<Parameter> parameters = new ArrayList<Parameter>()
            if (getRequest.path() == "/config/application") {
                Parameter parameter = Parameter.builder()
                        .name("/config/application/pets")
                        .value("dino,marty")
                        .type("StringList")
                        .build()
                parameters.add(parameter)
            }
            return CompletableFuture.completedFuture(GetParametersByPathResponse.builder().parameters(parameters).build())
        }

        client.client.getParameters(_) >> { GetParametersRequest getRequest ->
            return CompletableFuture.completedFuture(GetParametersResponse.builder().build())
        }

        when:
        def env = Mock(Environment)
        env.getActiveNames() >> (['first', 'second'] as Set)
        List<PropertySource> propertySources = Flowable.fromPublisher(client.getPropertySources(env)).toList().blockingGet()
        propertySources.sort(OrderUtil.COMPARATOR)
        PropertySourcePropertyResolver resolver = new PropertySourcePropertyResolver(propertySources as PropertySource[])

        then: "verify property source characteristics"
        propertySources.size() == 1
        propertySources[0].name == "route53-application"
        propertySources[0].order > EnvironmentPropertySource.POSITION

        resolver.getRequiredProperty("pets", List<String>.class) == ["dino", "marty"]
    }


    void "test discovery property sources from AWS Systems Manager Parameter Store - String"() {

        given:
        client.client.getParametersByPath(_) >> { GetParametersByPathRequest getRequest ->
            ArrayList<Parameter> parameters = new ArrayList<Parameter>()
            if (getRequest.path() == "/config/application") {
                Parameter parameter = Parameter.builder()
                        .name("/config/application/encryptedValue")
                        .value("true")
                        .type("String")
                        .build()
                parameters.add(parameter)
            }
            return CompletableFuture.completedFuture(GetParametersByPathResponse.builder().parameters(parameters).build())
        }

        client.client.getParameters(_) >> { GetParametersRequest getRequest ->
            ArrayList<Parameter> parameters = new ArrayList<Parameter>()
            if (getRequest.names().contains("/config/application_test")) {
                Parameter parameter1 = Parameter.builder()
                        .name("/config/application_test/foo")
                        .value("bar")
                        .type("String")
                        .build()
                parameters.add(parameter1)
            }
            return CompletableFuture.completedFuture(GetParametersResponse.builder().parameters(parameters).build())
        }

        when:
        def env = Mock(Environment)
        env.getActiveNames() >> (['test'] as Set)
        List<PropertySource> propertySources = Flowable.fromPublisher(client.getPropertySources(env)).toList().blockingGet()

        then: "verify property source characteristics"
        propertySources.size() == 2
        propertySources[0].order > EnvironmentPropertySource.POSITION
        propertySources[0].name == 'route53-application'
        propertySources[0].get('encryptedValue') == "true"
        propertySources[0].size() == 1
        propertySources[1].name == 'route53-application[test]'
        propertySources[1].get("foo") == "bar"
        propertySources[1].order > propertySources[0].order
        propertySources[1].toList().size() == 1
    }

    void "given a nextToken from AWS, client should paginate to retrieve all properties"() {
        given:
        String paramPath = "/config/application"
        client.client.getParametersByPath({ req -> req.nextToken == null } as GetParametersByPathRequest) >> { GetParametersByPathRequest req ->
            setupParamByPathResultMock(paramPath, req, 1..10)
        }
        client.client.getParametersByPath({ req -> req.nextToken != null } as GetParametersByPathRequest) >> { GetParametersByPathRequest req ->
            setupParamByPathResultMock(paramPath, req, 11..20)
        }

        client.client.getParameters(_) >> { GetParametersRequest getRequest ->
            ArrayList<Parameter> parameters = new ArrayList<Parameter>()
            if (getRequest.names().contains(paramPath)) {
                Parameter parameter = Parameter.builder()
                        .name("/config/application/datasource/url")
                        .value("mysql://blah")
                        .type("String")
                        .build()
                parameters.add(parameter)
            }
            if (getRequest.names().contains("/config/application_test")) {
                Parameter parameter1 = Parameter.builder()
                        .name("/config/application_test/foo")
                        .value("bar")
                        .type("String")
                        .build()
                parameters.add(parameter1)
            }
            return CompletableFuture.completedFuture(GetParametersResponse.builder().parameters(parameters).build())
        }

        when:
        def env = Mock(Environment)
        env.getActiveNames() >> (['test'] as Set)
        List<PropertySource> propertySources = Flowable.fromPublisher(client.getPropertySources(env)).toList().blockingGet()

        then: "verify property source characteristics"
        propertySources.size() == 2
        propertySources[0].order > EnvironmentPropertySource.POSITION
        propertySources[0].name == 'route53-application'
        propertySources[0].get('datasource.url') == "mysql://blah"
        propertySources[0].get('parameter-1') == "parameter-value-1"
        propertySources[0].get('parameter-15') == "parameter-value-15"
        propertySources[0].size() == 21
        propertySources[1].name == 'route53-application[test]'
        propertySources[1].get("foo") == "bar"
        propertySources[1].order > propertySources[0].order
        propertySources[1].toList().size() == 1

    }

    CompletableFuture setupParamByPathResultMock(String paramPath, GetParametersByPathRequest req, IntRange paramRange) {
        ArrayList<Parameter> parameters = new ArrayList<Parameter>()
        GetParametersByPathResponse.Builder builder = GetParametersByPathResponse.builder()
        if (req.path() == paramPath) {
            (paramRange).each {
                Parameter parameter = Parameter.builder()
                        .name("${paramPath}/parameter-${it}")
                        .value("parameter-value-${it}")
                        .type("String")
                        .build()
                parameters.add(parameter)
            }
            builder.nextToken(req.nextToken() == null ? "nextPage" : null)
        }

        return CompletableFuture.completedFuture(builder.parameters(parameters).build())
    }


    void "test discovery property sources from AWS Systems Manager Parameter Store - SecureString"() {

        given:

        client.client.getParametersByPath(_) >> { GetParametersByPathRequest getRequest ->
            ArrayList<Parameter> parameters = new ArrayList<Parameter>()
            if (getRequest.path() == "/config/application") {
                Parameter parameter = Parameter.builder()
                        .name("/config/application/encryptedValue")
                        .value("true")
                        .type("SecureString")
                        .build()
                parameters.add(parameter)
            }
            return CompletableFuture.completedFuture(GetParametersByPathResponse.builder().parameters(parameters).build())
        }

        client.client.getParameters(_) >> { GetParametersRequest getRequest ->
            ArrayList<Parameter> parameters = new ArrayList<Parameter>()
            if (getRequest.names().contains("/config/application")) {
                Parameter parameter = Parameter.builder()
                        .name("/config/application/datasource/url")
                        .value("mysql://blah")
                        .type("SecureString")
                        .build()
                parameters.add(parameter)
            }
            if (getRequest.names().contains("/config/application_test")) {
                Parameter parameter1 = Parameter.builder()
                        .name("/config/application_test/foo")
                        .value("bar")
                        .type("SecureString")
                        .build()
                parameters.add(parameter1)
            }
            return CompletableFuture.completedFuture(GetParametersResponse.builder().parameters(parameters).build())
        }

        when:
        def env = Mock(Environment)
        env.getActiveNames() >> (['test'] as Set)
        List<PropertySource> propertySources = Flowable.fromPublisher(client.getPropertySources(env)).toList().blockingGet()

        then: "verify property source characteristics"
        propertySources.size() == 2
        propertySources[0].order > EnvironmentPropertySource.POSITION
        propertySources[0].name == 'route53-application'
        propertySources[0].get('datasource.url') == "mysql://blah"
        propertySources[0].size() == 2
        propertySources[1].name == 'route53-application[test]'
        propertySources[1].get("foo") == "bar"
        propertySources[1].order > propertySources[0].order
        propertySources[1].toList().size() == 1
    }

    void "searching for active environments in AWS Systems Manager Parameter Store can be disabled"() {
        given:
        EmbeddedServer embeddedServer = ApplicationContext.run(EmbeddedServer,
                [
                        'aws.client.system-manager.parameterstore.enabled'                 : 'true',
                        'aws.client.system-manager.parameterstore.searchActiveEnvironments': 'false',
                        'micronaut.application.name'                                       : 'amazonTest'],
                Environment.AMAZON_EC2

        )

        AWSParameterStoreConfigClient client = embeddedServer.applicationContext.getBean(AWSParameterStoreConfigClient)

        client.client = Mock(SsmAsyncClient)

        def searchedPaths = []
        client.client.getParametersByPath(_) >> { GetParametersByPathRequest getRequest ->

            searchedPaths += getRequest.path
            return CompletableFuture.completedFuture(GetParametersByPathResponse.builder().build())
        }

        def searchedNames = []
        client.client.getParameters(_) >> { GetParametersRequest getRequest ->

            searchedNames.addAll(getRequest.names())
            ArrayList<Parameter> parameters = new ArrayList<Parameter>()
            if (getRequest.names().contains("/config/application")) {
                parameters.add(Parameter.builder()
                        .name("/config/application/someKey")
                        .value("someValue")
                        .type("String").
                        build())
            }
            return CompletableFuture.completedFuture(GetParametersResponse.builder().parameters(parameters).build())
        }

        when:
        def env = Mock(Environment)
        env.getActiveNames() >> (['first', 'second'] as Set)
        def propertySources = Flowable.fromPublisher(client.getPropertySources(env)).toList().blockingGet()

        then: "verify that active environment paths are not searched"
        propertySources.size() == 1
        propertySources[0].get('someKey') == 'someValue'

        [searchedPaths, searchedNames].forEach {
            assert it.size() == 2
            assert it.contains('/config/application')
            assert it.contains('/config/amazon-test')
        }
    }

    void "custom parameter query providers can be configured"() {
        given:
        EmbeddedServer embeddedServer = ApplicationContext.run(EmbeddedServer,
                [
                        'aws.client.system-manager.parameterstore.enabled': 'true',
                        'micronaut.application.name'                      : 'amazonTest'],
                Environment.AMAZON_EC2

        )
        AWSParameterStoreConfigClient client = embeddedServer.applicationContext.getBean(AWSParameterStoreConfigClient)

        client.client = Mock(SsmAsyncClient)
        client.queryProvider = (env, serviceId, configuration) -> [
                new ParameterQuery("/root/application", "/root/application", -1),
                new ParameterQuery('/config/foo', '/config/foo', -10)
        ]

        def searchedPaths = []
        client.client.getParametersByPath(_) >> { GetParametersByPathRequest getRequest ->

            searchedPaths += getRequest.path()

            def responseBuilder = GetParametersByPathResponse.builder()
            if (getRequest.path().startsWith("/root/application")) {
                def parameter = Parameter.builder()
                        .name("/root/application/someKey")
                        .value("someValue")
                        .type("String")
                        .build()
                responseBuilder.parameters([parameter])
            }
            return CompletableFuture.completedFuture(responseBuilder.build())
        }

        when:
        def env = Mock(Environment)
        def propertySources = Flowable.fromPublisher(client.getPropertySources(env)).toList().blockingGet()

        then: "verify that the custom paths were searched"
        propertySources.size() == 1
        propertySources[0].get('someKey') == 'someValue'

        searchedPaths.size() == 2
        searchedPaths.contains('/root/application')
        searchedPaths.contains('/config/foo')
    }
}
