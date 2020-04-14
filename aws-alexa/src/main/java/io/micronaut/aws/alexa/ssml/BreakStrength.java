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
 * @see <a href="https://developer.amazon.com/en-US/docs/alexa/custom-skills/speech-synthesis-markup-language-ssml-reference.html#break">SSML break</a>.
 * @author sdelamo
 * @since 2.0.0
 */
public enum  BreakStrength {

    /**
     *  No pause should be outputted. This can be used to remove a pause that would normally occur (such as after a period).
     */
    NONE("none"),

    /**
     * No pause should be outputted (same as none).
     */
    X_WEAK("x-weak"),

    /**
     * Treat adjacent words as if separated by a single comma (equivalent to medium).
     */
    WEAK("weak"),

    /**
     * Treat adjacent words as if separated by a single comma.
     */
    MEDIUM("medium"),

    /**
     * Make a sentence break (equivalent to using the s tag).
     */
    STRONG("strong"),

    /**
     * Make a paragraph break (equivalent to using the p tag).
     */
    X_STRONG("x-strong");

    private String value;

    /**
     * Constructor.
     * @param value Value
     */
    BreakStrength(String value) {
        this.value = value;
    }

    @Override
    public String toString()  {
        return this.value;
    }
}
