package io.micronaut.function.aws.proxy;

import com.amazonaws.services.lambda.runtime.events.ApplicationLoadBalancerRequestEvent;
import com.amazonaws.services.lambda.runtime.events.ApplicationLoadBalancerResponseEvent;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.function.aws.proxy.transformer.alb.MicronautAwsAlbRequestTransformer;
import io.micronaut.function.aws.proxy.transformer.alb.MicronautAwsAlbResponseTransformer;
import io.micronaut.http.HttpRequest;
import io.micronaut.serde.annotation.SerdeImport;
import jakarta.inject.Inject;


@Introspected(classes = {
    MicronautAlbRequestHandler.class,
    ApplicationLoadBalancerRequestEvent.class,
    ApplicationLoadBalancerRequestEvent.RequestContext.class,
    ApplicationLoadBalancerRequestEvent.Elb.class
})
@SerdeImport(ApplicationLoadBalancerRequestEvent.class)
@SerdeImport(ApplicationLoadBalancerRequestEvent.RequestContext.class)
@SerdeImport(ApplicationLoadBalancerRequestEvent.Elb.class)
public class MicronautAlbRequestHandler
    extends MicronautAwsHttpProxyRequestHandler<ApplicationLoadBalancerRequestEvent, ApplicationLoadBalancerResponseEvent> {

    @Inject
    private MicronautAwsAlbRequestTransformer micronautAwsAlbRequestTransformer;

    @Inject
    private MicronautAwsAlbResponseTransformer micronautAwsAlbResponseTransformer;

    @Override
    protected MicronautAwsRequestTransformer<ApplicationLoadBalancerRequestEvent, ? extends HttpRequest<?>> requestTransformer() {
        return micronautAwsAlbRequestTransformer;
    }

    @Override
    protected MicronautAwsResponseTransformer<ApplicationLoadBalancerResponseEvent> responseTransformer() {
        return micronautAwsAlbResponseTransformer;
    }

    @Override
    public Class<ApplicationLoadBalancerRequestEvent> inputTypeClass() {
        return ApplicationLoadBalancerRequestEvent.class;
    }

    @Override
    public Class<ApplicationLoadBalancerResponseEvent> outputTypeClass() {
        return ApplicationLoadBalancerResponseEvent.class;
    }
}
