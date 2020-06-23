/*
 * Copyright 2017-2020 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micronaut.aws.alexa.ssml;

/**
 * Supported Locales for xml-lang attribute.
 * @author sdelamo
 * @since 2.0.0
 */
public enum SupportedLang {
    EN_US("en-US"),
    EN_GB("en-GB"),
    EN_IN("en-IN"),
    EN_AU("en-AU"),
    EN_CA("en-CA"),
    DE("de-DE"),
    ES("es-ES"),
    HI_IN("hi-IN"),
    IT("it-IT"),
    JA("ja-JP"),
    FR("fr-FR");

    private String value;

    /**
     * Constructor.
     * @param value Value
     */
    SupportedLang(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return this.value;
    }
}
