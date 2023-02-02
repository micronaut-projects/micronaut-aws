package io.micronaut.function.aws.proxy;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.function.aws.proxy.transformer.httpgw.MicronautApiGatewayV2RequestTransformer;
import io.micronaut.function.aws.proxy.transformer.httpgw.MicronautApiGatewayV2ResponseTransformer;
import io.micronaut.http.HttpRequest;
import io.micronaut.serde.annotation.SerdeImport;
import jakarta.inject.Inject;


@Introspected(classes = {
    MicronautApiGatewayV2ContainerHandler.class,
    APIGatewayV2HTTPEvent.class,
    APIGatewayV2HTTPEvent.RequestContext.class,
    APIGatewayV2HTTPEvent.RequestContext.Authorizer.class,
    APIGatewayV2HTTPEvent.RequestContext.Http.class,
    APIGatewayV2HTTPEvent.RequestContext.IAM.class,
    APIGatewayV2HTTPEvent.RequestContext.CognitoIdentity.class,
    APIGatewayV2HTTPResponse.class,
})
@SerdeImport(APIGatewayV2HTTPEvent.class)
@SerdeImport(APIGatewayV2HTTPEvent.RequestContext.class)
@SerdeImport(APIGatewayV2HTTPEvent.RequestContext.Authorizer.class)
@SerdeImport(APIGatewayV2HTTPEvent.RequestContext.Http.class)
@SerdeImport(APIGatewayV2HTTPEvent.RequestContext.IAM.class)
@SerdeImport(APIGatewayV2HTTPEvent.RequestContext.CognitoIdentity.class)
@SerdeImport(APIGatewayV2HTTPResponse.class)
public class MicronautApiGatewayV2ContainerHandler
    extends MicronautAwsHttpProxyRequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {

    @Inject
    private MicronautApiGatewayV2RequestTransformer requestTransformer;

    @Inject
    private MicronautApiGatewayV2ResponseTransformer responseTransformer;

    @Override
    protected MicronautAwsRequestTransformer<APIGatewayV2HTTPEvent, ? extends HttpRequest<?>> requestTransformer() {
        return requestTransformer;
    }

    @Override
    protected MicronautAwsResponseTransformer<APIGatewayV2HTTPResponse> responseTransformer() {
        return responseTransformer;
    }

    @Override
    public Class<APIGatewayV2HTTPEvent> inputTypeClass() {
        return APIGatewayV2HTTPEvent.class;
    }

    @Override
    public Class<APIGatewayV2HTTPResponse> outputTypeClass() {
        return APIGatewayV2HTTPResponse.class;
    }
}
