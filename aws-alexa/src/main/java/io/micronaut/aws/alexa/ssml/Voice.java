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
 * @see <a href="https://developer.amazon.com/en-US/docs/alexa/custom-skills/speech-synthesis-markup-language-ssml-reference.html#voice">SSML Voice</a>.
 * @author sdelamo
 * @since 2.0.0
 */
public enum Voice {
    ENGLISH_AMERICAN_IVY("Ivy"),
    ENGLISH_AMERICAN_JOANNA("Joanna"),
    ENGLISH_AMERICAN_JOEY("Joey"),
    ENGLISH_AMERICAN_JUSTIN("Justin"),
    ENGLISH_AMERICAN_KENDRA("Kendra"),
    ENGLISH_AMERICAN_KIMBERLY("Kimberly"),
    ENGLISH_AMERICAN_MATTHEW("Matthew"),
    ENGLISH_AMERICAN_SALLI("Salli"),
    ENGLISH_AUSTRALIAN_NICOLE("Nicole"),
    ENGLISH_AUSTRALIAN_RUSSELL("Russell"),
    ENGLISH_BRITISH_AMY("Amy"),
    ENGLISH_BRITISH_BRIAN("Brian"),
    ENGLISH_BRITISH_EMMA("Emma"),
    ENGLISH_INDIAN_ADITI("Aditi"),
    ENGLISH_INDIAN_RAVEENA("Raveena"),
    GERMAN_HANS("Hans"),
    GERMAN_MARLENE("Marlene"),
    GERMAN_VICKI("Vicki"),
    SPANISH_CASTILIAN_CONCHITA("Conchita"),
    SPANISH_CASTILIAN_ENRIQUE("Enrique"),
    HINDI_ADITI("Aditi"),
    ITALIAN_CARLA("Carla"),
    ITALIAN_GIORGIO("Giorgio"),
    JAPANESE_MIZUKI("Mizuki"),
    JAPANESE_TAKUMI("Takumi"),
    FRENCH_CELINE("Celine"),
    FRENCH_LEA("Lea"),
    FRENCH_MATHIEU("Mathieu");

    private String name;

    /**
     * Constructor.
     * @param name voice name
     */
    Voice(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
