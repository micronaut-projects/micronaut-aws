package io.micronaut.aws.sdk.v2.client.crt

import io.micronaut.context.annotation.Requires
import spock.lang.Specification

class AwsCrtOptionalDependencySpec extends Specification {

    def "guards every CRT bean definition on both optional CRT clients"() {
        when:
        def definitionClass = Class.forName(
            'io.micronaut.aws.sdk.v2.client.crt.$AwsCrtClientFactory$Definition'
        )
        def metadata = definitionClass.getField('$ANNOTATION_METADATA').get(null)
        def expectedClasses = [
            'software.amazon.awssdk.http.crt.AwsCrtHttpClient',
            'software.amazon.awssdk.http.crt.AwsCrtAsyncHttpClient'
        ] as Set
        then:
        metadata.getAnnotationValuesByType(Requires)
            .any { it.annotationClassValues('classes')*.name as Set == expectedClasses }
    }
}
