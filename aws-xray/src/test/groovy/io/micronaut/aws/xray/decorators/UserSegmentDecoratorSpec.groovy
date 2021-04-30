package io.micronaut.aws.xray.decorators

import com.amazonaws.xray.AWSXRayRecorder
import com.amazonaws.xray.entities.SegmentImpl
import io.micronaut.http.HttpRequest
import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Shared
import com.amazonaws.xray.entities.Segment
import java.security.Principal;

class UserSegmentDecoratorSpec extends Specification {

    @Shared
    @Subject
    UserSegmentDecorator decorator = new UserSegmentDecorator()

    void "UserSegmentDecorator extracts username from request"() {
        given:
        def request = Stub(HttpRequest) {
            getUserPrincipal() >> Optional.of(new Principal() {
                @Override
                String getName() {
                    'john snow'
                }
            })
        }
        Segment segment = new SegmentImpl(AWSXRayRecorder.newInstance(), 'foo')

        when:
        decorator.decorate(segment, request)

        then:
        segment.user == 'john snow'
    }
}