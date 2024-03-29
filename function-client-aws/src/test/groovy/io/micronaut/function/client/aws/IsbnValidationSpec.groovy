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
package io.micronaut.function.client.aws

import io.micronaut.context.ApplicationContext
import io.micronaut.context.env.Environment
import spock.lang.AutoCleanup
import spock.lang.Issue
import spock.lang.Narrative
import spock.lang.Shared
import spock.lang.Specification

@Issue("https://github.com/micronaut-projects/micronaut-core/issues/525")
@Narrative("""
The beans participant in this tests are displayed in the docs.
""")
class IsbnValidationSpec extends Specification  {

    @Shared
    @AutoCleanup
    ApplicationContext context = ApplicationContext.run(
            [
            'spec.name': IsbnValidationSpec.class.simpleName,
            ], Environment.TEST)

    def "IsbnValidatorFunction and IsbnValidatorClient are loaded"() {
        when:
        context.getBean(IsbnValidatorFunction)

        then:
        noExceptionThrown()

        when:
        context.getBean(IsbnValidatorClient)

        then:
        noExceptionThrown()

    }
}
