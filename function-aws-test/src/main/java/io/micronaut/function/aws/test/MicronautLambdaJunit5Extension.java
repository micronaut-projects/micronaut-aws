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
package io.micronaut.function.aws.test;

import io.micronaut.context.ApplicationContext;
import io.micronaut.function.aws.LambdaApplicationContextBuilder;
import io.micronaut.function.aws.test.annotation.MicronautLambdaTest;
import io.micronaut.test.annotation.MicronautTestValue;
import io.micronaut.test.context.TestContext;
import io.micronaut.test.extensions.junit5.MicronautJunit5Extension;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestInstantiationException;
import org.junit.platform.commons.support.AnnotationSupport;

import java.util.Optional;

/**
 * Extension for testing Lambda environments with Junit 5.
 *
 * @author graemerocher
 * @since 1.0
 */
public class MicronautLambdaJunit5Extension extends MicronautJunit5Extension {
    private static final ExtensionContext.Namespace NAMESPACE =
            ExtensionContext.Namespace.create(MicronautLambdaJunit5Extension.class);

    @Override
    public void beforeAll(ExtensionContext extensionContext) throws Exception {
        final Class<?> testClass = extensionContext.getRequiredTestClass();
        MicronautTestValue micronautTestValue = AnnotationSupport
                        .findAnnotation(testClass, MicronautLambdaTest.class)
                        .map(this::buildValueObject)
                        .orElse(null);
        beforeClass(extensionContext, testClass, micronautTestValue);
        getStore(extensionContext).put(ApplicationContext.class, applicationContext);
        if (specDefinition != null) {
            TestInstance ti = AnnotationSupport.findAnnotation(testClass, TestInstance.class).orElse(null);
            if (ti != null && ti.value() == TestInstance.Lifecycle.PER_CLASS) {
                Object testInstance = extensionContext.getRequiredTestInstance();
                applicationContext.inject(testInstance);
            }
        }
        beforeTestClass(buildContext(extensionContext));
    }

    private static ExtensionContext.Store getStore(ExtensionContext context) {
        return context.getRoot().getStore(NAMESPACE);
    }

    private TestContext buildContext(ExtensionContext context) {
        return new TestContext(
                applicationContext,
                context.getTestClass().orElse(null),
                context.getTestMethod().orElse(null),
                context.getTestInstance().orElse(null),
                context.getExecutionException().orElse(null));
    }

    @SuppressWarnings("unchecked")
    private MicronautTestValue buildValueObject(MicronautLambdaTest micronautTest) {
        return new MicronautTestValue(
                micronautTest.application(),
                micronautTest.environments(),
                micronautTest.packages(),
                micronautTest.propertySources(),
                micronautTest.rollback(),
                micronautTest.transactional(),
                micronautTest.rebuildContext(),
                new Class[]{ LambdaApplicationContextBuilder.class },
                micronautTest.transactionMode(),
                micronautTest.startApplication());
    }

    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext extensionContext) {
        final Optional<Object> testInstance = extensionContext.getTestInstance();
        if (testInstance.isPresent()) {

            final Class<?> requiredTestClass = extensionContext.getRequiredTestClass();
            if (applicationContext.containsBean(requiredTestClass)) {
                return ConditionEvaluationResult.enabled("Test bean active");
            } else {

                final boolean hasBeanDefinition = isTestSuiteBeanPresent(requiredTestClass);
                if (!hasBeanDefinition) {
                    throw new TestInstantiationException(MISCONFIGURED_MESSAGE);
                } else {
                    return ConditionEvaluationResult.disabled(DISABLED_MESSAGE);
                }

            }
        } else {
            final Class<?> testClass = extensionContext.getRequiredTestClass();
            if (AnnotationSupport.isAnnotated(testClass, MicronautLambdaTest.class)) {
                return ConditionEvaluationResult.enabled("Test bean active");
            } else {
                return ConditionEvaluationResult.disabled(DISABLED_MESSAGE);
            }
        }
    }
}
