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
 * The name of the speaking style to apply to the speech.
 * @see <a href="https://developer.amazon.com/en-US/docs/alexa/custom-skills/speech-synthesis-markup-language-ssml-reference.html#amazon-domain">Amazon Domain</a>
 */
public enum AmazonDomain {

    /**
     * Style the speech for talking about music, video, or other multi-media content (available in English (US)).
     */
    MUSIC,

    /**
     * Style the speech similar to what you hear when listening to the news on the radio or television (available in English (US) and English (AU)).
     */
    NEWS;

    @Override
    public String toString() {
        return this.name().toLowerCase();
    }
}
