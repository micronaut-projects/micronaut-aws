/*
 * Copyright 2017-2023 original authors
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
package io.micronaut.function.aws.proxy;

import com.amazonaws.services.lambda.runtime.events.ApplicationLoadBalancerRequestEvent;
import com.amazonaws.services.lambda.runtime.events.ApplicationLoadBalancerResponseEvent;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.core.annotation.TypeHint;
import io.micronaut.function.aws.proxy.transformer.alb.MicronautAwsAlbRequestTransformer;
import io.micronaut.function.aws.proxy.transformer.alb.MicronautAwsAlbResponseTransformer;
import io.micronaut.serde.annotation.SerdeImport;
import jakarta.inject.Inject;

@TypeHint(
    accessType = {
        TypeHint.AccessType.ALL_DECLARED_CONSTRUCTORS,
        TypeHint.AccessType.ALL_PUBLIC
    },
    value = MicronautAlbRequestHandler.class
)
@Introspected(classes = {
    MicronautAlbRequestHandler.class,
    ApplicationLoadBalancerRequestEvent.class,
    ApplicationLoadBalancerRequestEvent.RequestContext.class,
    ApplicationLoadBalancerRequestEvent.Elb.class
})
@SerdeImport(ApplicationLoadBalancerRequestEvent.class)
@SerdeImport(ApplicationLoadBalancerRequestEvent.RequestContext.class)
@SerdeImport(ApplicationLoadBalancerRequestEvent.Elb.class)
public class MicronautAlbRequestHandler
    extends MicronautAwsHttpProxyRequestHandler<ApplicationLoadBalancerRequestEvent, ApplicationLoadBalancerResponseEvent> {

    @Inject
    private MicronautAwsAlbRequestTransformer<?> micronautAwsAlbRequestTransformer;

    @Inject
    private MicronautAwsAlbResponseTransformer micronautAwsAlbResponseTransformer;

    @Override
    protected MicronautAwsRequestTransformer<ApplicationLoadBalancerRequestEvent, ?> requestTransformer() {
        return micronautAwsAlbRequestTransformer;
    }

    @Override
    protected MicronautAwsResponseTransformer<ApplicationLoadBalancerResponseEvent> responseTransformer() {
        return micronautAwsAlbResponseTransformer;
    }

    @Override
    public Class<ApplicationLoadBalancerRequestEvent> inputTypeClass() {
        return ApplicationLoadBalancerRequestEvent.class;
    }

    @Override
    public Class<ApplicationLoadBalancerResponseEvent> outputTypeClass() {
        return ApplicationLoadBalancerResponseEvent.class;
    }
}
