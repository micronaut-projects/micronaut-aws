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
package io.micronaut.function.aws;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.ApplicationContextBuilder;
import io.micronaut.context.env.Environment;
import io.micronaut.function.executor.StreamFunctionExecutor;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static io.micronaut.function.aws.MicronautRequestHandler.registerContextBeans;

/**
 * <p>An implementation of the {@link RequestStreamHandler} for Micronaut</p>.
 *
 * @author Graeme Rocher
 * @since 1.0
 */
public class MicronautRequestStreamHandler extends StreamFunctionExecutor<Context> implements RequestStreamHandler, MicronautLambdaContext {

    private String functionName;

    /**
     * Default constructor.
     */
    public MicronautRequestStreamHandler() {
        // initialize the application context in the constructor
        // this is faster in Lambda as init cost is giving higher processor priority
        // see https://github.com/micronaut-projects/micronaut-aws/issues/18#issuecomment-530903419
        buildApplicationContext(null);
    }

    @Override
    public void handleRequest(InputStream input, OutputStream output, Context context) throws IOException {
        execute(input, output, context);
    }

    @Override
    protected ApplicationContext buildApplicationContext(Context context) {
        ApplicationContext applicationContext = super.buildApplicationContext(context);
        if (context != null) {
            registerContextBeans(context, applicationContext);
            this.functionName = context.getFunctionName();
        }
        return applicationContext;
    }

    @Nonnull
    @Override
    protected ApplicationContextBuilder newApplicationContextBuilder() {
        ApplicationContextBuilder builder = super.newApplicationContextBuilder();
        builder.environments(ENVIRONMENT_LAMBDA);
        return builder;
    }

    @Override
    protected void closeApplicationContext() {
        // Avoid closing the application context when running the function in lambda to keep it warm
    }

    @Override
    protected String resolveFunctionName(Environment env) {
        if (this.functionName != null) {
            return functionName;
        } else {
            return super.resolveFunctionName(env);
        }
    }
}
