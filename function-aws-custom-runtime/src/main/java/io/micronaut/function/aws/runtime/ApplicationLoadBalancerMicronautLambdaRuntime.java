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
package io.micronaut.function.aws.runtime;

import com.amazonaws.services.lambda.runtime.RequestHandler;
import io.micronaut.function.aws.proxy.alb.ApplicationLoadBalancerFunction;
import com.amazonaws.services.lambda.runtime.events.ApplicationLoadBalancerRequestEvent;
import com.amazonaws.services.lambda.runtime.events.ApplicationLoadBalancerResponseEvent;

/**
 * Main entry for AWS ALB with Micronaut.
 *
 * @author sdelamo
 * @since 2.0.0
 */
public class ApplicationLoadBalancerMicronautLambdaRuntime extends AbstractMicronautLambdaRuntime<ApplicationLoadBalancerRequestEvent, ApplicationLoadBalancerResponseEvent, ApplicationLoadBalancerRequestEvent, ApplicationLoadBalancerResponseEvent> {

    @Override
    protected RequestHandler<ApplicationLoadBalancerRequestEvent, ApplicationLoadBalancerResponseEvent> createRequestHandler(String... args) {
        return new ApplicationLoadBalancerFunction(createApplicationContextBuilderWithArgs(args).build());
    }

    /**
     *
     * @param args Command Line args
     */
    public static void main(String[] args) throws Exception {
        new ApplicationLoadBalancerMicronautLambdaRuntime().run(args);
    }
}
