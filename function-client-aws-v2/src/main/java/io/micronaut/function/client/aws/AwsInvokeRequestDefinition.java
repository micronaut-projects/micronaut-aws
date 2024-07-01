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

import io.micronaut.context.annotation.EachProperty;
import io.micronaut.context.annotation.Parameter;
import io.micronaut.function.client.FunctionDefinition;

/**
 * Builds an {@link InvokeRequest} for each definition under {@code aws.lambda.functions}.
 *
 * @since 4.7.0
 */
@EachProperty(AwsInvokeRequestDefinition.AWS_LAMBDA_FUNCTIONS)
public class AwsInvokeRequestDefinition implements FunctionDefinition {
    public static final String AWS_LAMBDA_FUNCTIONS = "aws.lambda.functions";

    private final String name;

    /**
     * Constructor.
     * @param name configured name from a property
     */
    public AwsInvokeRequestDefinition(@Parameter String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return this.name;
    }
}
