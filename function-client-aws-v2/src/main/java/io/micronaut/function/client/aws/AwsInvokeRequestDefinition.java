/*
 * Copyright 2017-2020 original authors
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
package io.micronaut.function.client.aws;

import io.micronaut.aws.AWSConfiguration;
import io.micronaut.context.annotation.ConfigurationBuilder;
import io.micronaut.context.annotation.EachProperty;
import io.micronaut.context.annotation.Parameter;
import io.micronaut.context.annotation.Requires;
import io.micronaut.function.client.FunctionDefinition;
import software.amazon.awssdk.services.lambda.model.InvokeRequest;

/**
 * Builds an {@link InvokeRequest} for each definition under {@code aws.lambda.functions}.
 *
 * @author graemerocher
 * @since 1.0
 */
@EachProperty(AwsInvokeRequestDefinition.AWS_LAMBDA_FUNCTIONS)
@Requires(classes = InvokeRequest.class)
public class AwsInvokeRequestDefinition implements FunctionDefinition {
    public static final String AWS_LAMBDA_FUNCTIONS = AWSConfiguration.PREFIX + ".lambda.functions";

    @ConfigurationBuilder(prefixes = {""}, excludes = {"profileFile", "applyMutation"})
    protected InvokeRequest.Builder invokeRequestBuilder;

    private final String name;

    /**
     * Constructor.
     *
     * @param name configured name from a property
     */
    public AwsInvokeRequestDefinition(@Parameter String name) {
        this.name = name;
        this.invokeRequestBuilder = InvokeRequest.builder();
        this.invokeRequestBuilder.functionName(name);
    }

    /**
     * @return The {@link InvokeRequest} definition
     */
    public InvokeRequest.Builder getInvokeRequestBuilder() {
        return invokeRequestBuilder;
    }

    @Override
    public String getName() {
        return this.name;
    }
}
