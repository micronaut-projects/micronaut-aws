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

import com.amazonaws.services.lambda.runtime.LambdaLogger;
import java.nio.charset.Charset;

/**
 * Implementation of {@link com.amazonaws.services.lambda.runtime.LambdaLogger} which logs to System.out.println.
 * @author Sergio del Amo
 * @since 4.0.0
 */
public class SystemOutLambdaLogger implements LambdaLogger {

    @Override
    public void log(String s) {
        System.out.println(s);
    }

    @Override
    public void log(byte[] bytes) {
        System.out.println(new String(bytes, Charset.defaultCharset()));
    }
}
