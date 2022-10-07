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

import groovy.xml.XmlSlurper;
import groovy.xml.slurpersupport.GPathResult;
import groovy.xml.slurpersupport.NodeChild;
import org.gradle.api.DefaultTask;
import org.gradle.api.artifacts.result.ArtifactResolutionResult;
import org.gradle.api.artifacts.result.ComponentArtifactsResult;
import org.gradle.api.artifacts.result.ResolvedArtifactResult;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.CacheableTask;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;
import org.gradle.maven.MavenModule;
import org.gradle.maven.MavenPomArtifact;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.TreeMap;

@CacheableTask
public abstract class WriteMicronautVersionInfoTask extends DefaultTask {
    @Input
    public abstract Property<String> getVersion();
    @OutputDirectory
    public abstract DirectoryProperty getOutputDirectory();

    public WriteMicronautVersionInfoTask() {
        getOutputs().doNotCacheIf("snapshot version", spec -> getVersion().get().endsWith("SNAPSHOT"));
    }

    @TaskAction
    public void writeVersionInfo() throws IOException {
        Map<String, String> props = generateProperties();
        try (OutputStream out = new FileOutputStream(getOutputDirectory().file("micronaut-versions.properties").get().getAsFile())) {
            for (Map.Entry<String, String> entry : props.entrySet()) {
                String line = entry.getKey() + "=" + entry.getValue() + "\n";
                out.write(line.getBytes(StandardCharsets.ISO_8859_1));
            }
        }
    }

    private Map<String, String> generateProperties() {
        Map<String, String> props = new TreeMap<>();
        props.put("micronaut.version", getVersion().get());
        return props;
    }
}
