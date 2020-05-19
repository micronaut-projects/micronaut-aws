/*
 * Copyright 2017-2020 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micronaut.aws.alexa.httpserver.verifiers;

import io.micronaut.aws.alexa.httpserver.AskHttpServerConstants;
import io.micronaut.context.condition.Condition;
import io.micronaut.context.condition.ConditionContext;

/**
 * {@link Condition} which return true if System property {@value AskHttpServerConstants#DISABLE_REQUEST_SIGNATURE_CHECK_SYSTEM_PROPERTY} is present and evaluates to true.
 *
 * @since 2.0.0
 * @author sdelamo
 */
public class RequestSignatureCheckSystemPropertyCondition implements Condition {

    @Override
    public boolean matches(ConditionContext context) {
        String isRequestSignatureCheckDisabled = System.getProperty(AskHttpServerConstants.DISABLE_REQUEST_SIGNATURE_CHECK_SYSTEM_PROPERTY);
        return Boolean.parseBoolean(isRequestSignatureCheckDisabled);
    }
}
