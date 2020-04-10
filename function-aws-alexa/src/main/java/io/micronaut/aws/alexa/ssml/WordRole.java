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
 * Word role used to customize the pronunciation of words by specifying the word's part of speech.
 * @see <a href="https://developer.amazon.com/en-US/docs/alexa/custom-skills/speech-synthesis-markup-language-ssml-reference.html#w">SSML W</a>.
 * @author sdelamo
 * @since 2.0.0
 */
public enum WordRole {

    /**
     * Interpret the word as a verb (present simple).
     */
    VB("amazon:VB"),

    /**
     * Interpret the word as a past participle.
     */
    VBD("amazon:VBD"),

    /**
     * Interpret the word as a noun.
     */
    NN("amazon:NN"),

    /**
     * Use the non-default sense of the word. For example, the noun "bass" is pronounced differently depending on meaning.
     */
    SENSE_1("amazon:SENSE_1");

    private String value;

    /**
     * Constructor.
     * @param value value
     */
    WordRole(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return this.value;
    }
}
