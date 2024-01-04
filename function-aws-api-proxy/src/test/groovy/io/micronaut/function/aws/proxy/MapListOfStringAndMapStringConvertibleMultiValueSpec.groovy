package io.micronaut.function.aws.proxy

import io.micronaut.core.convert.ConversionService
import spock.lang.Narrative
import spock.lang.See
import spock.lang.Specification

class MapListOfStringAndMapStringConvertibleMultiValueSpec extends Specification {

    void "works with empty maps"() {
        given:
        def map = new MapListOfStringAndMapStringConvertibleMultiValue(ConversionService.SHARED, [:], [:])

        expect:
        map.empty
    }

    void "works with just multi maps"() {
        given:
        def multi = ['foo': ['bar', 'baz']]
        def map = new MapListOfStringAndMapStringConvertibleMultiValue(ConversionService.SHARED, multi, [:])

        expect:
        map.size() == 1
        map.getAll('foo') == ['bar', 'baz']
        map.getAll('FOO') == ['bar', 'baz']
        map.get('foo') == 'bar'
    }

    void "works with single maps"() {
        given:
        def single = ['foo': 'bar']
        def map = new MapListOfStringAndMapStringConvertibleMultiValue(ConversionService.SHARED, [:], single)

        expect:
        map.size() == 1
        map.getAll('foo') == ['bar']
        map.getAll('FOO') == ['bar']
        map.get('foo') == 'bar'
    }

    @See("https://docs.aws.amazon.com/apigateway/latest/developerguide/set-up-lambda-proxy-integrations.html#api-gateway-simple-proxy-for-lambda-input-format")
    void "If the same key-value pair is specified in both, only the values from multiValueHeaders will appear in the merged list"() {
        given:
        Map<String, List<String>> multi = ['foo': ['bar', 'baz']]
        Map<String, String> single = ['foo': 'qux']
        MapListOfStringAndMapStringConvertibleMultiValue map = new MapListOfStringAndMapStringConvertibleMultiValue(ConversionService.SHARED, multi, single)

        expect:
        map.size() == 1
        map.getAll('foo') == ['bar', 'baz']
        map.getAll('FOO') == ['bar', 'baz']
        map.get('foo') == 'bar'
    }
}
