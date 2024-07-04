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
package io.micronaut.function.client.aws.v2;

import io.micronaut.context.annotation.EachProperty;
import io.micronaut.context.annotation.Parameter;
import io.micronaut.function.client.FunctionDefinition;

/**
 * Builds an {@link AwsInvokeRequestDefinition} for each definition under {@code aws.lambda.functions}.
 *
 * @since 4.7.0
 */
@EachProperty(AwsInvokeRequestDefinition.AWS_LAMBDA_FUNCTIONS)
public class AwsInvokeRequestDefinition implements FunctionDefinition {
    public static final String AWS_LAMBDA_FUNCTIONS = "aws.lambda.functions";

    private final String name;

    private String functionName;

    private String qualifier;

    private String clientContext;

    /**
     * Constructor.
     *
     * @param name configured name from a property
     */
    public AwsInvokeRequestDefinition(@Parameter String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return this.name;
    }

    public String getFunctionName() {
        return functionName;
    }

    public String getQualifier() {
        return qualifier;
    }

    public String getClientContext() {
        return clientContext;
    }

    public void setFunctionName(String functionName) {
        this.functionName = functionName;
    }

    public void setQualifier(String qualifier) {
        this.qualifier = qualifier;
    }

    public void setClientContext(String clientContext) {
        this.clientContext = clientContext;
    }
}
