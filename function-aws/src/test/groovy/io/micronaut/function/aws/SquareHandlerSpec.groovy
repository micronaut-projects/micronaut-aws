package io.micronaut.function.aws

import com.amazonaws.services.lambda.runtime.ClientContext
import com.amazonaws.services.lambda.runtime.CognitoIdentity
import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.LambdaLogger
import io.micronaut.context.ApplicationContext
import org.spockframework.mock.MockUtil
import spock.lang.Shared
import spock.lang.Specification

class SquareHandlerSpec extends Specification {

    @Shared
    MockUtil mockUtil = new MockUtil()

    @Shared
    Context lambdaCtx = Mock(Context) {
        getLogger() >> Mock(LambdaLogger)
        getClientContext() >> Mock(ClientContext)
        getIdentity() >> Mock(CognitoIdentity)
    }

    def "test using detached mock"() {
        given:
        def handler = new SquareHandler()
        def appCtx = handler.buildApplicationContext(lambdaCtx)
        def squareService = appCtx.getBean(SquareService)
        assert mockUtil.isMock(squareService)
        mockUtil.attachMock(squareService, this)

        when:
        def result = handler.handleRequest(2, lambdaCtx)

        then:
        squareService.square(_) >> 4
        result == 4

        cleanup:
        mockUtil.detachMock(squareService)
    }
}
