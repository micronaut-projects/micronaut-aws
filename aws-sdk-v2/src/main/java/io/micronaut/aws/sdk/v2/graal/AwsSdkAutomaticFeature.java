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
package io.micronaut.aws.sdk.v2.graal;

import com.oracle.svm.core.annotate.AutomaticFeature;
import io.micronaut.core.annotation.Internal;
import org.graalvm.nativeimage.hosted.Feature;

import static io.micronaut.core.graal.AutomaticFeatureUtils.*;

/**
 * GraalVM utility class.
 *
 * @author Álvaro Sánchez-Mariscal
 * @since 2.0.0
 */
@AutomaticFeature
@Internal
public final class AwsSdkAutomaticFeature implements Feature {

    /**
     * @param baa the {@link BeforeAnalysisAccess} instance
     */
    @Override
    public void beforeAnalysis(BeforeAnalysisAccess baa) {
        registerAllForRuntimeReflection(baa, "org.apache.http.client.config.RequestConfig$Builder");

        initializeAtBuildTime(baa, "org.apache.http.HttpClientConnection");
        initializeAtBuildTime(baa, "org.apache.http.conn.routing.HttpRoute");
        initializeAtBuildTime(baa, "org.apache.http.conn.HttpClientConnectionManager");
        initializeAtBuildTime(baa, "org.apache.http.conn.ConnectionRequest");
        initializeAtBuildTime(baa, "org.apache.http.pool.ConnPoolControl");
        initializeAtBuildTime(baa, "org.apache.http.protocol.HttpContext");
        initializeAtBuildTime(baa, "org.apache.http.client.config.RequestConfig");
        initializeAtBuildTime(baa, "org.apache.http.client.config.RequestConfig$Builder");

        initializeAtRunTime(baa, "io.netty.channel.epoll.Epoll");
        initializeAtRunTime(baa, "io.netty.channel.epoll.Native");
        initializeAtRunTime(baa, "io.netty.channel.epoll.EpollEventLoop");
        initializeAtRunTime(baa, "io.netty.channel.epoll.EpollEventArray");
        initializeAtRunTime(baa, "io.netty.channel.unix.Errors");
        initializeAtRunTime(baa, "io.netty.channel.unix.IovArray");
        initializeAtRunTime(baa, "io.netty.channel.unix.Socket");
        initializeAtRunTime(baa, "org.apache.commons.logging.LogFactory");

        addProxyClass(baa,
                "org.apache.http.conn.HttpClientConnectionManager",
                "org.apache.http.pool.ConnPoolControl",
                "software.amazon.awssdk.http.apache.internal.conn.Wrapped");
        addProxyClass(baa,
                "org.apache.http.conn.ConnectionRequest",
                "software.amazon.awssdk.http.apache.internal.conn.Wrapped");

    }

}
