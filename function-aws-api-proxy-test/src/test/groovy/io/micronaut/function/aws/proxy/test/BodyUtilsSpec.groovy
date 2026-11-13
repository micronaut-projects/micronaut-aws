package io.micronaut.function.aws.proxy.test

import io.micronaut.http.MediaType
import io.micronaut.json.JsonMapper
import spock.lang.Specification

import java.nio.charset.StandardCharsets

class BodyUtilsSpec extends Specification {

    void "a map body with a form content type is url-encoded"() {
        given:
        Map<String, Object> form = new LinkedHashMap<>()
        form.put("name", "a b")
        form.put("tags", ["x&y", "z"])

        when:
        Optional<String> body = BodyUtils.bodyAsString(Mock(JsonMapper),
            () -> MediaType.APPLICATION_FORM_URLENCODED_TYPE,
            () -> StandardCharsets.UTF_8,
            () -> form)

        then:
        body.get() == "name=a+b&tags=x%26y&tags=z"
    }
}
