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
package io.micronaut.aws.sdk.v2.processor;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import com.sun.tools.javac.code.Attribute;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Requires;
import io.micronaut.core.naming.NameUtils;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.inject.Singleton;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * An annotation processor that generates the Oracle Cloud SDK integration
 * for Micronaut.
 *
 * @author graemerocher
 * @since 1.0.0
 */
@SupportedAnnotationTypes("io.micronaut.aws.sdk.v2.service.SdkClients")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class AwsSdkProcessor extends AbstractProcessor {

    public static final String CLIENT_PACKAGE = "io.micronaut.aws.sdk.v2.service";
    private Filer filer;
    private Messager messager;
    private Elements elements;
    private Types types;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        System.out.println("HOVNO");
        this.filer = processingEnv.getFiler();
        this.messager = processingEnv.getMessager();
        this.elements = processingEnv.getElementUtils();
        this.types = processingEnv.getTypeUtils();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (TypeElement annotation : annotations) {
            final Set<? extends Element> element = roundEnv.getElementsAnnotatedWith(annotation);
            for (Element e : element) {
                List<AnnotationMirror> sdkClients = resolveSdkClients(e);
                for (AnnotationMirror sdkClient : sdkClients) {
                    writeSdkClient(sdkClient);
                }
            }
        }
        return false;
    }

    private void writeSdkClient(AnnotationMirror annotationMirror) {
        String clientClass = null;
        String clientBuilderClass = null;
        String clientAsyncClass = null;
        String clientAsyncBuilderClass = null;

        final Map<? extends ExecutableElement, ? extends AnnotationValue> values = annotationMirror.getElementValues();
        for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : values.entrySet()) {
            final Name elementName = entry.getKey().getSimpleName();

            switch (elementName.toString()) {
                case "client":
                    clientClass = getTypeValue(entry.getValue());
                    break;
                case "clientBuilder":
                    clientBuilderClass = getTypeValue(entry.getValue());
                    break;
                case "asyncClient":
                    clientAsyncClass = getTypeValue(entry.getValue());
                    break;
                case "asyncClientBuilder":
                    clientAsyncBuilderClass = getTypeValue(entry.getValue());
                    break;
            }
            System.out.println("DONE FOR " + elementName.toString() + " " + getTypeValue(entry.getValue()));
        }

        if (clientClass != null && clientBuilderClass != null && clientAsyncClass != null && clientAsyncBuilderClass != null) {
            writeClientFactory(annotationMirror.getAnnotationType().asElement(), clientClass, clientBuilderClass, clientAsyncClass, clientAsyncBuilderClass);
        }
    }

    private String getTypeValue(AnnotationValue value) {
        final Object nested = value.getValue();
        if (nested instanceof DeclaredType) {
            final TypeElement dte = (TypeElement) ((DeclaredType) nested).asElement();
            return dte.getQualifiedName().toString();
        }
        return null;
    }

    private void writeClientFactory(Element e, String clientClass, String clientBuilderClass, String clientAsyncClass, String clientAsyncBuilderClass) {
        final String packageName = NameUtils.getPackageName(clientClass);
        final String clientClassName = NameUtils.getSimpleName(clientClass);
        final String clientAsyncClassName = NameUtils.getSimpleName(clientAsyncClass);
        final String clientBuilderClassName = NameUtils.getSimpleName(clientBuilderClass);
        final String clientAsyncBuilderClassName = NameUtils.getSimpleName(clientAsyncBuilderClass);

        final String factoryName = clientClassName + "Factory";
        final String factoryPackageName = packageName.replace("software.amazon.awssdk.services", CLIENT_PACKAGE);

        System.out.println("WRITING FACTORY " + factoryName + " " + factoryPackageName);

        final TypeSpec.Builder builder = defineSuperclass(factoryName, packageName, clientClassName, clientBuilderClassName, clientAsyncClassName, clientAsyncBuilderClassName);
        final MethodSpec.Builder constructor = buildConstructor(builder);
        builder.addAnnotation(Factory.class);
        final AnnotationSpec.Builder requiresSpec = AnnotationSpec.builder(Requires.class)
                .addMember("classes", "{$L.class, $L.class}", clientClassName, clientAsyncClassName);
        builder.addAnnotation(requiresSpec.build());
        builder.addMethod(constructor.build());

        // create sync
        final ClassName clientBuilderClassType = ClassName.get(packageName, clientBuilderClassName);
        final MethodSpec.Builder createSyncBuilder = MethodSpec.methodBuilder("createSyncBuilder");
        createSyncBuilder.returns(clientBuilderClassType)
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PROTECTED)
                .addCode(CodeBlock.of("return $L.builder();", clientClassName));
        builder.addMethod(createSyncBuilder.build());

        final MethodSpec.Builder syncBuilder = MethodSpec.methodBuilder("syncBuilder");
        syncBuilder.returns(clientBuilderClassType)
                .addAnnotation(Override.class)
                .addAnnotation(Singleton.class)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(ParameterSpec.builder(ClassName.get("software.amazon.awssdk.http", "SdkHttpClient"), "httpClient")
                        .build())
                .addCode("return super.syncBuilder(httpClient);");
        builder.addMethod(syncBuilder.build());

        final ClassName clientClassType = ClassName.get(packageName, clientClassName);
        final MethodSpec.Builder syncClient = MethodSpec.methodBuilder("syncClient");
        syncClient.returns(clientClassType)
                .addAnnotation(Override.class)
                .addAnnotation(Singleton.class)
                .addAnnotation(AnnotationSpec.builder(Bean.class)
                        .addMember("preDestroy", "\"close\"")
                        .build())
                .addModifiers(Modifier.PUBLIC)
                .addParameter(clientBuilderClassType, "builder")
                .addCode("return super.syncClient(builder);");
        builder.addMethod(syncClient.build());

        // create async
        final ClassName clientAsyncBuilderClassType = ClassName.get(packageName, clientAsyncBuilderClassName);
        final MethodSpec.Builder createAsyncBuilder = MethodSpec.methodBuilder("createAsyncBuilder");
        createAsyncBuilder.returns(clientAsyncBuilderClassType)
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PROTECTED)
                .addCode(CodeBlock.of("return $L.builder();", clientAsyncClassName));
        builder.addMethod(createAsyncBuilder.build());

        final MethodSpec.Builder asyncBuilder = MethodSpec.methodBuilder("asyncBuilder");
        asyncBuilder.returns(clientAsyncBuilderClassType)
                .addAnnotation(Override.class)
                .addAnnotation(Singleton.class)
                .addAnnotation(AnnotationSpec.builder(Requires.class)
                        .addMember("beans","SdkAsyncHttpClient.class")
                        .build())
                .addModifiers(Modifier.PUBLIC)
                .addParameter(ParameterSpec.builder(ClassName.get("software.amazon.awssdk.http.async", "SdkAsyncHttpClient"), "httpClient")
                        .build())
                .addCode("return super.asyncBuilder(httpClient);");
        builder.addMethod(asyncBuilder.build());

        final MethodSpec.Builder asyncClient = MethodSpec.methodBuilder("asyncClient");
        final ClassName clientAsyncClientClassType = ClassName.get(packageName, clientAsyncClassName);
        asyncClient.returns(clientAsyncClientClassType)
                .addAnnotation(Override.class)
                .addAnnotation(Singleton.class)
                .addAnnotation(AnnotationSpec.builder(Requires.class)
                        .addMember("beans","SdkAsyncHttpClient.class")
                        .build())
                .addAnnotation(AnnotationSpec.builder(Bean.class)
                        .addMember("preDestroy", "\"close\"")
                        .build())
                .addModifiers(Modifier.PUBLIC)
                .addParameter(clientAsyncBuilderClassType, "builder")
                .addCode("return super.asyncClient(builder);");
        builder.addMethod(asyncClient.build());


        final JavaFile javaFile = JavaFile.builder(factoryPackageName, builder.build()).build();
        try {
            final JavaFileObject javaFileObject = filer.createSourceFile(factoryPackageName + "." + factoryName, e);
            try (Writer writer = javaFileObject.openWriter()) {
                javaFile.writeTo(writer);
            }
        } catch (IOException ioException) {
            messager.printMessage(Diagnostic.Kind.ERROR, "Error occurred generating Oracle SDK factories: " + ioException.getMessage(), e);
        }
    }

    private TypeSpec.Builder defineSuperclass(String factoryName, String packageName, String clientName, String clientBuilderName, String asyncClientName, String asyncClientBuilderName) {
        final TypeSpec.Builder builder = TypeSpec.classBuilder(factoryName);
        builder.superclass(ParameterizedTypeName.get(
                ClassName.get("io.micronaut.aws.sdk.v2.service", "AwsClientFactory"),
                ClassName.get(packageName, clientBuilderName),
                ClassName.get(packageName, asyncClientBuilderName),
                ClassName.get(packageName, clientName),
                ClassName.get(packageName, asyncClientName))
        );
        return builder;
    }

    private MethodSpec.Builder buildConstructor(TypeSpec.Builder builder) {
        final MethodSpec.Builder constructor = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PROTECTED)
                .addParameter(ParameterSpec.builder(ClassName.get("software.amazon.awssdk.auth.credentials", "AwsCredentialsProviderChain"), "credentialsProvider")
                                .build())
                .addParameter(ParameterSpec.builder(ClassName.get("software.amazon.awssdk.regions.providers", "AwsRegionProviderChain"), "regionProvider")
                                .build())
                .addCode(CodeBlock.builder()
                        .addStatement("super(credentialsProvider, regionProvider)")
                        .build());
        builder.addModifiers(Modifier.PUBLIC, Modifier.FINAL);
        return constructor;
    }

    private List<AnnotationMirror> resolveSdkClients(Element e) {
        List<AnnotationMirror> sdkClients = new ArrayList<>();
        final List<? extends AnnotationMirror> annotationMirrors = e.getAnnotationMirrors();
        for (AnnotationMirror annotationMirror : annotationMirrors) {
            TypeElement te = (TypeElement) annotationMirror.getAnnotationType().asElement();
            String ann = te.getSimpleName().toString();
            if (ann.equals("SdkClients")) {
                final Map<? extends ExecutableElement, ? extends AnnotationValue> values = annotationMirror.getElementValues();
                for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : values.entrySet()) {
                    final ExecutableElement executableElement = entry.getKey();
                    if (executableElement.getSimpleName().toString().equals("value")) {
                        final AnnotationValue value = entry.getValue();
                        final Object v = value.getValue();
                        if (v instanceof Iterable) {
                            Iterable<Object> i = (Iterable) v;
                            for (Object o : i) {
                                if (o instanceof AnnotationValue) {
                                    if (o instanceof AnnotationMirror) {
                                        final AnnotationMirror m = ((AnnotationMirror) o);
                                        sdkClients.add(m);
                                    }
                                }
                            }
                        }
                    }
                }

            }
        }
        return sdkClients;
    }
}
