/*
 * Copyright 2017-2019 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micronaut.aws.alexa.ssml;

/**
 * @see <a href="https://developer.amazon.com/en-US/docs/alexa/custom-skills/speech-synthesis-markup-language-ssml-reference.html#say-as">SSML say-as</a>.
 * @author sdelamo
 * @since 2.0.0
 */
public enum InterpretAs {

    /**
     * Spell out each letter.
     */
    CHARACTERS("characters"),

    SPELL_OUT("spell-out"),

    /**
     * Interpret the value as a cardinal number.
     */
    CARDINAL("cardinal"),

    NUMBER("number"),

    /**
     * Interpret the value as an ordinal number.
     */
    ORDINAL("ordinal"),

    /**
     * Spell each digit separately .
     */
    DIGITS("digits"),

    /**
     * Interpret the value as a fraction. This works for both common fractions (such as 3/20) and mixed fractions (such as 1+1/2).
     */
    FRACTION("fraction"),

    /**
     * Interpret a value as a measurement. The value should be either a number or fraction followed by a unit (with no space in between) or just a unit.
     */
    UNIT("unit"),

    /**
     * Interpret the value as a date. Specify the format with the format attribute.
     */
    DATE("date"),

    /**
     * Interpret a value such as 1'21" as duration in minutes and seconds.
     */
    TIME("time"),

    /**
     * Interpret a value as a 7-digit or 10-digit telephone number. This can also handle extensions (for example, 2025551212x345).
     */
    TELEPHONE("telephone"),

    /**
     * Interpret a value as part of street address.
     */
    ADDRESS("address"),

    /**
     * Interpret the value as an interjection.
     */
    INTERJECTION("interjection"),

    /**
     * "Bleep" out the content inside the tag.
     */
    EXPLETIVE("expletive");

    private String value;

    /**
     * Constructor.
     * @param value Value
     */
    InterpretAs(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return this.value;
    }
}
