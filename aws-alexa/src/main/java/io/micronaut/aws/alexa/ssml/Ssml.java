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

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Speech Synthesis Markup Language builder.
 * @see <a href="https://developer.amazon.com/en-US/docs/alexa/custom-skills/speech-synthesis-markup-language-ssml-reference.html">SSML</a>.
 * @author sdelamo
 * @since 2.0.0
 */
public class Ssml {

    public static final String ATTRIBUTE_NAME = "name";
    public static final String OPEN_TAG = "<";
    public static final String CLOSE_TAG = ">";
    public static final String OPEN_CLOSE_TAG = "</";
    public static final String CLOSE_OPENING_TAG = "/>";
    public static final String INTERPRET_AS = "interpret-as";
    public static final String FORMAT = "format";
    public static final String TAG_AMAZON_DOMAIN = "amazon:domain";
    public static final String TAG_AMAZON_EFFECT = "amazon:effect";
    public static final String TAG_AMAZON_EMOTION = "amazon:emotion";
    public static final String TAG_AUDIO = "audio";
    public static final String TAG_BREAK = "break";
    public static final String TAG_EMPHASIS = "emphasis";
    public static final String TAG_LANG = "lang";
    public static final String TAG_P = "p";
    public static final String TAG_PHONEME = "phoneme";
    public static final String TAG_PROSODY = "prosody";
    public static final String TAG_S = "s";
    public static final String TAG_SAY_AS = "say-as";
    public static final String TAG_SPEAK = "speak";
    public static final String TAG_SUB = "sub";
    public static final String TAG_VOICE = "voice";
    public static final String TAG_W = "w";
    public static final String XML_LANG = "xml:lang";
    public static final String RATE = "rate";
    public static final String PITCH = "pitch";
    public static final String VOLUME = "volume";
    public static final String SRC = "src";

    private StringBuffer result = new StringBuffer();

    /**
     * Constructor.
     */
    public Ssml() {

    }

    /**
     *
     * @param text plain text
     */
    public Ssml(String text) {
        result.append(text);
    }

    /**
     * Applies different speaking styles to the speech. The styles are curated text-to-speech voices that employ different variations of intonation, emphasis, pausing and other techniques to match the speech to the type of content. For example, the news style makes Alexa's voice sound like what you might expect to hear in a TV or radio newscast, and was built primarily for customers to listen to news articles and other news-based content.
     * @param domain Name of the speaking style to apply to the speech
     * @param text Text to be wrapped inside the amazon:domain tag
     * @return An SsmlBuilder with an amazon:domain tag.
     */
    public Ssml domain(@NonNull AmazonDomain domain,
                       @NonNull String text) {
        StringBuffer sb = new StringBuffer();
        sb.append(openTag(TAG_AMAZON_DOMAIN, Collections.singletonMap(ATTRIBUTE_NAME, domain.toString())));
        sb.append(text);
        sb.append(closeTag(TAG_AMAZON_DOMAIN));
        result.append(sb);
        return this;
    }

    /**
     * The audio tag lets you provide the URL for an MP3 file that the Alexa service can play while rendering a response.
     * @param src Specifies the URL for the MP3 file
     * @return SSML Builder
     */
    public Ssml audio(@NonNull String src) {
        StringBuffer sb = new StringBuffer();
        sb.append(openTag(TAG_AUDIO, Collections.singletonMap(SRC, src), true));
        result.append(sb);
        return this;
    }

    /**
     * Applies Amazon-specific effect to the speech.
     * @param effect Effect
     * @param text text to apply the effect to
     * @return Text wrapped in amazon:effect tag
     */
    public Ssml effect(@NonNull AmazonEffect effect,
                       @NonNull String text) {
        StringBuffer sb = new StringBuffer();
        sb.append(openTag(TAG_AMAZON_EFFECT, Collections.singletonMap(ATTRIBUTE_NAME, effect.toString())));
        sb.append(text);
        sb.append(closeTag(TAG_AMAZON_EFFECT));
        result.append(sb);
        return this;
    }

    /**
     *
     * @param text plain text
     * @return Return builder
     */
    public Ssml text(@NonNull String text) {
        result.append(text);
        return this;
    }

    /**
     *
     * @param emotion The name of the emotion to apply to the speech
     * @param intensity The intensity or strength of teh emotion to express.
     * @param text plain text to apply emotion to
     * @return SSML builder
     */
    public Ssml emotion(@NonNull AmazonEmotion emotion,
                        @NonNull AmazonEmotionIntensity intensity,
                        @NonNull String text) {
        Map<String, String> attributes = new HashMap<>();
        attributes.put(ATTRIBUTE_NAME, emotion.toString());
        attributes.put("intensity", intensity.toString());
        StringBuffer sb = new StringBuffer();
        sb.append(openTag(TAG_AMAZON_EMOTION, attributes));
        sb.append(text);
        sb.append(closeTag(TAG_AMAZON_EMOTION));
        result.append(sb);
        return this;
    }

    /**
     *
     * @param text text to speak tagged as a particular lang
     * @param lang specify the language model and rules to speak the tagged content
     * @return SSML Builder
     */
    public Ssml lang(@NonNull String text,
                     @NonNull SupportedLang lang) {
        StringBuffer sb = new StringBuffer();
        sb.append(openTag(TAG_LANG, Collections.singletonMap(XML_LANG, lang.toString())));
        sb.append(text);
        sb.append(closeTag(TAG_LANG));
        result.append(sb);
        return this;
    }

    /**
     *
     * @param text text to be wrapped in a paragraph
     * @return SSML builder
     */
    public Ssml paragraph(@NonNull String text) {
        return p(text);
    }

    /**
     *
     * @param text text to be wrapped in the prosody tag
     * @param rate Rate of Speech
     * @param pitch Tone (pitch) of the speech
     * @param volume Volume of the Speech
     * @return SSML Builder
     */
    public Ssml prosody(@NonNull String text,
                        @Nullable ProsodyRate rate,
                        @Nullable ProsodyPitch pitch,
                        @Nullable ProsodyVolume volume) {
        StringBuffer sb = new StringBuffer();
        Map<String, String> attributes = new HashMap<>();

        if (rate != null) {
            attributes.put(RATE, rate.toString());
        }
        if (pitch != null) {
            attributes.put(PITCH, pitch.toString());
        }
        if (volume != null) {
            attributes.put(VOLUME, volume.toString());
        }
        sb.append(openTag(TAG_PROSODY, attributes));
        sb.append(text);
        sb.append(closeTag(TAG_PROSODY));
        result.append(sb);
        return this;
    }

    /**
     *
     * @param text Text to be wrapped in a sentence tag (s)
     * @return SSML builder
     */
    public Ssml sentence(@NonNull String text) {
        return s(text);
    }

    /**
     *
     * @param text Text to be wrapped in speak tag
     * @return SSML Builder
     */
    public Ssml speak(@NonNull String text) {
        StringBuffer sb = new StringBuffer();
        sb.append(openTag(TAG_SPEAK, null));
        sb.append(text);
        sb.append(closeTag(TAG_SPEAK));
        result.append(sb);
        return this;
    }

    /**
     *
     * @param text Text to be wrapped in a sentence tag (s)
     * @return SSML builder
     */
    public Ssml s(@NonNull String text) {
        StringBuffer sb = new StringBuffer();
        sb.append(openTag(TAG_S, null));
        sb.append(text);
        sb.append(closeTag(TAG_S));
        result.append(sb);
        return this;
    }

    /**
     *
     * @param text Text to be wrapped in a paragraph tag
     * @return SSML builder
     */
    public Ssml p(@NonNull String text) {
        StringBuffer sb = new StringBuffer();
        sb.append(openTag(TAG_P, null));
        sb.append(text);
        sb.append(closeTag(TAG_P));
        result.append(sb);
        return this;
    }

    /**
     *
     * @param text text to be wrapped in a say as tag
     * @param interpretAs Indicate alexa how to interpret text
     * @param interpretAsDateFormat Format to be used when interpret-as is set to date.
     * @return SSML Builder
     */
    public Ssml sayAs(@NonNull String text,
                      @NonNull InterpretAs interpretAs,
                      @Nullable InterpretAsDateFormat interpretAsDateFormat) {
        Map<String, String> attributes = new HashMap<>();
        attributes.put(INTERPRET_AS, interpretAs.toString());
        if (interpretAsDateFormat != null) {
            attributes.put(FORMAT, interpretAsDateFormat.toString());
        }
        StringBuffer sb = new StringBuffer();
        sb.append(openTag(TAG_SAY_AS, attributes));
        sb.append(text);
        sb.append(closeTag(TAG_SAY_AS));
        result.append(sb);
        return this;
    }

    /**
     *
     * @see <a href="https://developer.amazon.com/en-US/docs/alexa/custom-skills/speech-synthesis-markup-language-ssml-reference.html#w">SSML w</a>
     * @param text Text to be wrapped
     * @param role Specify role of the word
     * @return SSML builder
     */
    public Ssml w(@NonNull String text, @NonNull WordRole role) {
        StringBuffer sb = new StringBuffer();
        sb.append(openTag(TAG_W, Collections.singletonMap("role", role.toString())));
        sb.append(text);
        sb.append(closeTag(TAG_W));
        result.append(sb);
        return this;
    }

    /**
     *
     * @param text Text to be emphasized
     * @param emphasisLevel Emphasis level
     * @return SSML builder
     */
    public Ssml emphasis(@NonNull String text, @Nullable EmphasisLevel emphasisLevel) {
        StringBuffer sb = new StringBuffer();
        if (emphasisLevel == null) {
            sb.append(openTag(TAG_EMPHASIS, null));
        } else {
            sb.append(openTag(TAG_EMPHASIS, Collections.singletonMap("level", emphasisLevel.toString())));
        }
        sb.append(text);
        sb.append(closeTag(TAG_EMPHASIS));
        result.append(sb);
        return this;
    }

    /**
     * @see <a href="https://developer.amazon.com/en-US/docs/alexa/custom-skills/speech-synthesis-markup-language-ssml-reference.html#break">SSML Break</a>.
     * @param seconds duration of break in seconds
     * @return SSML Builder
     */
    public Ssml breakWithSeconds(@NonNull Integer seconds) {
        return breakWithAttributes(Collections.singletonMap("time", seconds + "s"));
    }

    /**
     *
     * @param text text to be spoken in a particular voice
     * @param voice Amazon Polly voice to speak the text with
     * @return SSML Builder
     */
    public Ssml voice(@NonNull String text, @NonNull Voice voice) {
        StringBuffer sb = new StringBuffer();
        sb.append(openTag(TAG_VOICE, Collections.singletonMap(ATTRIBUTE_NAME, voice.toString())));
        sb.append(text);
        sb.append(closeTag(TAG_VOICE));
        result.append(sb);
        return this;
    }

    /**
     *
     * @param text text to be pronouced differently
     * @param alias pronunciation to substitute
     * @return SSML
     */
    public Ssml sub(@NonNull String text, @NonNull String alias) {
        StringBuffer sb = new StringBuffer();
        sb.append(openTag(TAG_SUB, Collections.singletonMap("alias", alias)));
        sb.append(text);
        sb.append(closeTag(TAG_SUB));
        result.append(sb);
        return this;
    }

    /**
     * @see <a href="https://developer.amazon.com/en-US/docs/alexa/custom-skills/speech-synthesis-markup-language-ssml-reference.html#break">SSML Break</a>
     * @param milliseconds duration of break in milliseconds
     * @return SSML Builder
     */
    public Ssml breakWithMilliseconds(@NonNull Integer milliseconds) {
        return breakWithAttributes(Collections.singletonMap("time", milliseconds + "ms"));
    }

    /**
     * @see <a href="https://developer.amazon.com/en-US/docs/alexa/custom-skills/speech-synthesis-markup-language-ssml-reference.html#break">SSML Break</a>
     * @param strength Break strength
     * @return SSML Builder
     */
    public Ssml breakWithStrength(@NonNull BreakStrength strength) {
        return breakWithAttributes(Collections.singletonMap("strength", strength.toString()));
    }

    /**
     *
     * @return SSML as String
     */
    public String build() {
        return result.toString();
    }

    private Ssml breakWithAttributes(@NonNull Map<String, String> attributes) {
        StringBuffer sb = new StringBuffer();
        sb.append(openTag(TAG_BREAK, attributes, true));
        result.append(sb);
        return this;
    }

    private String closeTag(@NonNull String tagName) {
        StringBuffer sb = new StringBuffer();
        sb.append(OPEN_CLOSE_TAG);
        sb.append(tagName);
        sb.append(CLOSE_TAG);
        return sb.toString();
    }

    private String openTag(@NonNull String tagName,
                           @Nullable Map<String, String> attributes) {
        return openTag(tagName, attributes, null);
    }

    private String openTag(@NonNull String tagName,
                           @Nullable Map<String, String> attributes,
                           @Nullable Boolean close) {
        StringBuffer sb = new StringBuffer();
        sb.append(OPEN_TAG);
        sb.append(tagName);
        if (attributes != null) {
            List<String> attributeNames = null;
            if (attributes.containsKey(ATTRIBUTE_NAME)) {
                Set<String> attributesSet = new HashSet<>(attributes.keySet());
                attributeNames = new ArrayList<>();
                if (attributesSet.contains(ATTRIBUTE_NAME)) {
                    attributeNames.add(ATTRIBUTE_NAME);
                    attributesSet.remove(ATTRIBUTE_NAME);
                }
                attributeNames.addAll(attributesSet);
            }
            for (String attributeName : (attributeNames != null ? attributeNames : attributes.keySet())) {
                sb.append(" ");
                sb.append(attributeName);
                sb.append("=\"");
                sb.append(attributes.get(attributeName));
                sb.append("\"");
            }
        }
        if (close != null && close) {
            sb.append(CLOSE_OPENING_TAG);
        } else {
            sb.append(CLOSE_TAG);
        }
        return sb.toString();
    }
}
