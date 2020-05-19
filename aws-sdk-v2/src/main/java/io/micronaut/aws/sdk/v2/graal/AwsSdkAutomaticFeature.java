package io.micronaut.aws.sdk.v2.graal;

import com.oracle.svm.core.annotate.AutomaticFeature;
import com.oracle.svm.core.jdk.proxy.DynamicProxyRegistry;
import org.graalvm.nativeimage.ImageSingletons;
import org.graalvm.nativeimage.hosted.Feature;
import org.graalvm.nativeimage.hosted.RuntimeClassInitialization;
import org.graalvm.nativeimage.hosted.RuntimeReflection;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * GraalVM utility class.
 *
 * @author Álvaro Sánchez-Mariscal
 * @since 2.0.0
 */
@AutomaticFeature
public class AwsSdkAutomaticFeature implements Feature {

    /**
     * @param baa the {@link BeforeAnalysisAccess} instance
     */
    @Override
    public void beforeAnalysis(BeforeAnalysisAccess baa) {
        registerRuntimeReflection(baa, "org.apache.http.client.config.RequestConfig$Builder");

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

    private void registerRuntimeReflection(BeforeAnalysisAccess access, String className) {
        Class<?> clazz = initialize(RuntimeReflection::register, access, className);
        if (clazz != null) {
            registerAllAccess(clazz);
        }
    }

    private void initializeAtBuildTime(BeforeAnalysisAccess access, String className) {
        initialize(RuntimeClassInitialization::initializeAtBuildTime, access, className);
    }

    private void initializeAtRunTime(BeforeAnalysisAccess access, String className) {
        initialize(RuntimeClassInitialization::initializeAtRunTime, access, className);
    }

    private void addProxyClass(BeforeAnalysisAccess access, String... interfaces) {
        List<Class<?>> classList = new ArrayList<>();
        for (String anInterface : interfaces) {
            Class<?> clazz = access.findClassByName(anInterface);
            if (clazz != null) {
                classList.add(clazz);
            }
        }
        if (classList.size() == interfaces.length) {
            ImageSingletons.lookup(DynamicProxyRegistry.class).addProxyClass(classList.toArray(new Class<?>[interfaces.length]));
        }
    }

    private Class<?> initialize(Consumer<Class<?>> operation, BeforeAnalysisAccess access, String className) {
        Class<?> clazz = access.findClassByName(className);
        if (clazz != null) {
            operation.accept(clazz);
        }
        return clazz;

    }

    private void registerAllAccess(Class<?> t) {
        for (Method method : t.getMethods()) {
            RuntimeReflection.register(method);
        }
        Field[] fields = t.getFields();
        for (Field field : fields) {
            RuntimeReflection.register(field);
        }
    }

}
