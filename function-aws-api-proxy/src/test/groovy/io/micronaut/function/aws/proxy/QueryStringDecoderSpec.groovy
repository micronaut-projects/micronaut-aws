package io.micronaut.function.aws.proxy

import spock.lang.Specification

class QueryStringDecoderSpec extends Specification {

    private final static CAFFE = new String(
            // "Caffé" but instead of putting the literal E-acute in the
            // source file, we directly use the UTF-8 encoding so as to
            // not rely on the platform's default encoding (not portable).
            new byte[]{'C', 'a', 'f', 'f', (byte) 0xC3, (byte) 0xA9},
            "UTF-8");

    void "basic"() {
        when:
        QueryStringDecoder d = new QueryStringDecoder("/foo?a=b=c")
        then:
        d.path() == "/foo"
        d.parameters().size() == 1
        d.parameters().a.size() == 1
        d.parameters().a[0] == "b=c"

        when:
        d = new QueryStringDecoder("/foo?a=1&a=2")
        then:
        d.path() == "/foo"
        d.parameters().size() == 1
        d.parameters().a.size() == 2
        d.parameters().a[0] == "1"
        d.parameters().a[1] == "2"

        when:
        d = new QueryStringDecoder("/foo?a=&a=2")
        then:
        d.path() == "/foo"
        d.parameters().size() == 1
        d.parameters().a.size() == 2
        d.parameters().a[0] == ""
        d.parameters().a[1] == "2"

        when:
        d = new QueryStringDecoder("/foo?a=1&a=")
        then:
        d.path() == "/foo"
        d.parameters().size() == 1
        d.parameters().a.size() == 2
        d.parameters().a[0] == "1"
        d.parameters().a[1] == ""

        when:
        d = new QueryStringDecoder("/foo?a=1&a=&a=")
        then:
        d.path() == "/foo"
        d.parameters().size() == 1
        d.parameters().a.size() == 3
        d.parameters().a[0] == "1"
        d.parameters().a[1] == ""
        d.parameters().a[2] == ""

        when:
        d = new QueryStringDecoder("/foo?a=1=&a==2")
        then:
        d.path() == "/foo"
        d.parameters().size() == 1
        d.parameters().a.size() == 2
        d.parameters().a[0] == "1="
        d.parameters().a[1] == "=2"
    }

    void "#expected is the same as #actual"() {
        when:
        QueryStringDecoder ed = new QueryStringDecoder(expected);
        QueryStringDecoder ad = new QueryStringDecoder(actual);

        then:
        ed.path() == ad.path()
        ed.parameters() == ad.parameters()

        where:
        expected          | actual
        ""                | ""
        "foo"             | "foo"
        "/foo"            | "/foo"
        "?a="             | "?a"
        "foo?a="          | "foo?a"
        "/foo?a="         | "/foo?a"
        "/foo?a="         | "/foo?a&"
        "/foo?a="         | "/foo?&a"
        "/foo?a="         | "/foo?&a&"
        "/foo?a="         | "/foo?&=a"
        "/foo?a="         | "/foo?=a&"
        "/foo?a="         | "/foo?a=&"
        "/foo?a=b&c=d"    | "/foo?a=b&&c=d"
        "/foo?a=b&c=d"    | "/foo?a=b&=&c=d"
        "/foo?a=b&c=d"    | "/foo?a=b&==&c=d"
        "/foo?a=b&c=&x=y" | "/foo?a=b&c&x=y"
        "/foo?a="         | "/foo?a="
        "/foo?a="         | "/foo?&a="
        "/foo?a=b&c=d"    | "/foo?a=b&c=d"
        "/foo?a=1&a=&a="  | "/foo?a=1&a&a="
    }

    void "hash dos"() {
        given:
        StringBuilder buf = new StringBuilder()
        buf.append('?')
        for (int i = 0; i < 65536; i++) {
            buf.append('k')
            buf.append(i)
            buf.append("=v")
            buf.append(i)
            buf.append('&')
        }

        expect:
        new QueryStringDecoder(buf.toString()).parameters().size() == 1024
    }

    void "has path"() {
        when:
        QueryStringDecoder decoder = new QueryStringDecoder("1=2", false)

        then:
        decoder.path() == ""

        with(decoder.parameters()) {
            size() == 1
            get("1") == ["2"]
        }
    }

    void "valid url decoding for '#encoded' is '#decoded'"() {
        expect:
        QueryStringDecoder.decodeComponent(encoded) == decoded

        where:
        encoded      | decoded
        ""           | ""
        "foo"        | "foo"
        "f%%b"       | "f%b"
        "f+o"        | "f o"
        "f++"        | "f  "
        "%42"        | "B"
        "%5f"        | "_"
        "Caff%C3%A9" | CAFFE
    }

    void "invalid encoding '#encoded' gives error '#error'"() {
        when:
        QueryStringDecoder.decodeComponent(encoded)

        then:
        IllegalArgumentException ex = thrown()
        ex.message.contains(error)

        where:
        encoded | error
        "fo%"   | "unterminated escape sequence"
        "f%4"   | "partial escape sequence"
        "%x2"   | "invalid escape sequence `%x2' at index 0 of: %x2"
        "%4x"   | "invalid escape sequence `%4x' at index 0 of: %4x"
    }
}
