/*
 * Copyright 2017-2022 original authors
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
package io.micronaut.internal.aws.tasks;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.CacheableTask;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;
import javax.lang.model.element.Modifier;
import java.io.IOException;

@CacheableTask
public abstract class UserAgentProviderGeneratorTask extends DefaultTask {
    @Input
    public abstract Property<String> getVersion();
    @OutputDirectory
    public abstract DirectoryProperty getOutputDirectory();
    public UserAgentProviderGeneratorTask() {
        getOutputs().doNotCacheIf("snapshot version", spec -> getVersion().get().endsWith("SNAPSHOT"));
    }
    @TaskAction
    public void writeVersionInfo() throws IOException {
        String ua = "micronaut/" + getVersion().get();
        JavaFile file = createFile(ua);
        file.writeTo(getOutputDirectory().get().getAsFile());
    }

    private JavaFile createFile(String ua) {
        String packageName = "io.micronaut.aws.ua";
        return JavaFile.builder(packageName, TypeSpec.classBuilder("GeneratedUserAgentProvider")
                .addAnnotation(ClassName.get("jakarta.inject", "Singleton"))
                .addSuperinterface(ClassName.get(packageName, "UserAgentProvider"))
                .addAnnotation(AnnotationSpec.builder(ClassName.get("io.micronaut.context.annotation","Requires"))
                    .addMember("property", "$S", "aws.ua.enabled")
                    .addMember("value", "$S", "true")
                    .addMember("defaultValue", "$S", "true")
                    .build())
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addMethod(MethodSpec.methodBuilder("userAgent")
                    .addModifiers(Modifier.PUBLIC)
                    .addAnnotation(ClassName.get("io.micronaut.core.annotation", "NonNull"))
                    .addAnnotation(Override.class)
                    .addJavadoc(CodeBlock.builder().build())
                    .returns(String.class)
                    .addStatement("return $S", ua)
                    .build())
                .build())
            .build();
    }
}
