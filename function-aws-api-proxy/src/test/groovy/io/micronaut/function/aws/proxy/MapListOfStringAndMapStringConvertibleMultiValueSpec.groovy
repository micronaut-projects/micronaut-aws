package io.micronaut.function.aws.proxy

import io.micronaut.core.convert.ConversionService
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

    void "coalescence works as expected"() {
        given:
        def multi = ['foo': ['bar', 'baz']]
        def single = ['foo': 'qux']
        def map = new MapListOfStringAndMapStringConvertibleMultiValue(ConversionService.SHARED, multi, single)

        expect:
        map.size() == 1
        map.getAll('foo') == ['bar', 'baz', 'qux']
        map.getAll('FOO') == ['bar', 'baz', 'qux']
        map.get('foo') == 'bar'
    }
}
