package io.micronaut.function.aws.proxy;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.async.publisher.Publishers;
import io.micronaut.core.execution.ExecutionFlow;
import io.micronaut.core.type.Argument;
import io.micronaut.core.type.TypeVariableResolver;
import io.micronaut.core.util.StringUtils;
import io.micronaut.http.HttpAttributes;
import io.micronaut.http.HttpMethod;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.annotation.Consumes;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.annotation.Status;
import io.micronaut.http.reactive.execution.ReactiveExecutionFlow;
import io.micronaut.http.server.RequestLifecycle;
import io.micronaut.http.server.RouteExecutor;
import io.micronaut.web.router.MethodBasedRouteMatch;
import io.micronaut.web.router.RouteInfo;
import io.micronaut.web.router.RouteMatch;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

class AwsRequestLifecycle extends RequestLifecycle {
    private final MicronautLambdaContainerHandler containerHandler;
    private final MicronautAwsProxyRequest<?> containerRequest;
    private final MicronautAwsProxyResponse<?> containerResponse;

    AwsRequestLifecycle(
        MicronautLambdaContainerHandler containerHandler,
        RouteExecutor routeExecutor,
        MicronautAwsProxyRequest<?> containerRequest,
        MicronautAwsProxyResponse<?> containerResponse) {
        super(routeExecutor, containerRequest);
        this.containerHandler = containerHandler;
        this.containerRequest = containerRequest;
        this.containerResponse = containerResponse;
    }

    ExecutionFlow<MutableHttpResponse<?>> run() {
        return normalFlow()
            .flatMap(r -> {
                Optional<RouteInfo> routeInfo = r.getAttribute(HttpAttributes.ROUTE_INFO, RouteInfo.class);
                if (routeInfo.isPresent()) {
                    return convertResponseBody(routeInfo.get(), r.body());
                } else {
                    // can happen on error
                    return ExecutionFlow.just(r);
                }
            })
            .onErrorResume(this::onError);
    }

    @Override
    protected ExecutionFlow<RouteMatch<?>> fulfillArguments(RouteMatch<?> routeMatch) {
        decodeRequestBody(routeMatch)
            .ifPresent(((MicronautAwsProxyRequest) containerRequest)::setDecodedBody);
        return super.fulfillArguments(routeMatch);
    }

    @NonNull
    private Optional<Object> decodeRequestBody(@NonNull RouteMatch<?> finalRoute) {
        return containerHandler.mediaTypeBodyDecoder.entrySet()
            .stream()
            .map(e -> decodeRequestBody(finalRoute, e))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .findFirst();
    }

    private Optional<Object> decodeRequestBody(@NonNull RouteMatch<?> finalRoute,
                                                      @NonNull Map.Entry<MediaType, BiFunction<Argument<?>, String, Optional<Object>>> entry) {
        return parseUndecodedBody(finalRoute, entry.getKey())
            .flatMap(body -> decodeRequestBody(body, entry.getValue(), finalRoute));
    }

    @NonNull
    private static Optional<Object> decodeRequestBody(@NonNull String body,
                                                      @NonNull BiFunction<Argument<?>, String, Optional<Object>> function,
                                                      @NonNull RouteMatch<?> finalRoute) {
        if (StringUtils.isNotEmpty(body)) {
            Argument<?> bodyArgument = parseBodyArgument(finalRoute);
            return function.apply(bodyArgument, body);
        }
        return Optional.empty();
    }

    @Nullable
    private static Argument<?> parseBodyArgument(@NonNull RouteMatch<?> finalRoute) {
        Argument<?> bodyArgument = finalRoute.getBodyArgument().orElse(null);
        if (bodyArgument == null) {
            if (finalRoute instanceof MethodBasedRouteMatch) {
                bodyArgument = Arrays.stream(((MethodBasedRouteMatch) finalRoute).getArguments())
                    .filter(arg -> HttpRequest.class.isAssignableFrom(arg.getType()))
                    .findFirst()
                    .flatMap(TypeVariableResolver::getFirstTypeVariable).orElse(null);
            }
        }
        if (bodyArgument != null) {
            final Class<?> rawType = bodyArgument.getType();
            if (Publishers.isConvertibleToPublisher(rawType) || HttpRequest.class.isAssignableFrom(rawType)) {
                bodyArgument = bodyArgument.getFirstTypeVariable().orElse(Argument.OBJECT_ARGUMENT);
            }
        }
        return bodyArgument;
    }

    @NonNull
    private Optional<String> parseUndecodedBody(@NonNull RouteMatch<?> finalRoute,
                                                @NonNull MediaType mediaType) {
        if (containerRequest.isBodyDecoded()) {
            return Optional.empty();
        }
        if (!HttpMethod.permitsRequestBody(containerRequest.getMethod())) {
            return Optional.empty();
        }
        final MediaType requestContentType = containerRequest.getContentType().orElse(null);
        if (requestContentType == null) {
            return Optional.empty();
        }
        if (!mediaType.getExtension().equals(requestContentType.getExtension())) {
            return Optional.empty();
        }
        final MediaType[] expectedContentType = finalRoute.getAnnotationMetadata().getValue(Consumes.class, MediaType[].class).orElse(null);
        return (expectedContentType == null || Arrays.stream(expectedContentType).anyMatch(ct -> mediaType.getExtension().equals(ct.getExtension()))) ?
            containerRequest.getBody(String.class) :
            Optional.empty();
    }

    private ExecutionFlow<MutableHttpResponse<?>> convertResponseBody(RouteInfo<?> routeInfo, Object body) {
        if (Publishers.isConvertibleToPublisher(body)) {
            ExecutionFlow<?> single;
            if (Publishers.isSingle(body.getClass()) || routeInfo.getReturnType().isSpecifiedSingle()) {
                single = ReactiveExecutionFlow.fromPublisher(Mono.from(Publishers.convertPublisher(body, Publisher.class)));
            } else {
                single = ReactiveExecutionFlow.fromPublisher(Flux.from(Publishers.convertPublisher(body, Publisher.class)).collectList());
            }
            return single.map((Function<Object, MutableHttpResponse<?>>) o -> {
                if (!(o instanceof MicronautAwsProxyResponse)) {
                    ((MutableHttpResponse) containerResponse).body(o);
                }
                applyRouteConfig(routeInfo);
                return containerResponse;
            });
        } else {
            if (!(body instanceof MicronautAwsProxyResponse)) {
                applyRouteConfig(routeInfo);
                ((MutableHttpResponse) containerResponse).body(body);
            }
            return ExecutionFlow.just(containerResponse);
        }
    }

    private void applyRouteConfig(RouteInfo<?> finalRoute) {
        if (!containerResponse.getContentType().isPresent()) {
            finalRoute.getAnnotationMetadata().getValue(Produces.class, String.class).ifPresent(containerResponse::contentType);
        }
        finalRoute.getAnnotationMetadata().getValue(Status.class, HttpStatus.class).ifPresent(httpStatus -> containerResponse.status(httpStatus));
    }
}
