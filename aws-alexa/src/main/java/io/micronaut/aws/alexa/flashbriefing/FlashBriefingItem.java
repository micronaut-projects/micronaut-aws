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
package io.micronaut.aws.alexa.flashbriefing;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import io.micronaut.core.annotation.Introspected;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PastOrPresent;
import javax.validation.constraints.Size;
import java.time.ZonedDateTime;

/**
 * Flash Briefing item.
 * @see <a href="https://developer.amazon.com/en-US/docs/alexa/flashbriefing/flash-briefing-skill-api-feed-reference.html#details">Flash Breifing Skill API Feed Reference</a>
 * @author sdelamo
 * @since 2.0.0
 */
@Introspected
public class FlashBriefingItem implements Comparable<FlashBriefingItem> {

    /**
     * A unique identifier for each feed item. UUID format preferred, but not required.
     */
    @NonNull
    @NotBlank
    private String uid;

    /**
     * The date indicates freshness of the feed item.
     * A string in ISO 8601 format, YYYY-MM-DDThh:mm:ssZ, specified in UTC.
     */
    @NotNull
    @NonNull
    @PastOrPresent
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssZ")
    private ZonedDateTime updateDate;

    /**
     * The title of the feed item to display in the Alexa app.
     */
    @NotBlank
    @NonNull
    private String titleText;

    /**
     * The text that Alexa reads to the customer. For audio items, this element is ignored, and can contain an empty string ("").
     */
    @NonNull
    @NotNull
    @Size(max = 4500)
    @JsonInclude()
    private String mainText;

    /**
     * The audio that Alexa plays for the customer. An HTTPS URL specifying the location of the audio content. This field is required for audio items.
     */
    @Nullable
    private String streamUrl;

    /**
     * Provides the URL target for the Read More link in the Alexa app.
     */
    @NonNull
    @NotBlank
    private String redirectionUrl;

    /**
     * Constructor.
     */
    public FlashBriefingItem() {
    }

    /**
     *
     * @return A unique identifier for the feed item.
     */
    @NonNull
    public String getUid() {
        return uid;
    }

    /**
     *
     * @param uid A unique identifier for the feed item
     */
    public void setUid(@NonNull String uid) {
        this.uid = uid;
    }

    /**
     *
     * @return The date that indicates the freshness of the feed item
     */
    @NonNull
    public ZonedDateTime getUpdateDate() {
        return updateDate;
    }

    /**
     *
     * @param updateDate the date that indicates the freshness of the feed item
     */
    public void setUpdateDate(@NonNull ZonedDateTime updateDate) {
        this.updateDate = updateDate;
    }

    /**
     *
     * @return The title of the feed item to display in the alexa app.
     */
    @NonNull
    public String getTitleText() {
        return titleText;
    }

    /**
     *
     * @param titleText Title of the feed item to display in the alexa app.
     */
    public void setTitleText(@NonNull String titleText) {
        this.titleText = titleText;
    }

    /**
     *
     * @return The text that alexa read to the customer.
     */
    @NonNull
    public String getMainText() {
        return mainText;
    }

    /**
     *
     * @param mainText The text that alexa read to the customer.
     */
    public void setMainText(@NonNull String mainText) {
        this.mainText = mainText;
    }

    /**
     *
     * @return The HTTPs URL specifying the location of the audio content.
     */
    @Nullable
    public String getStreamUrl() {
        return streamUrl;
    }

    /**
     *
     * @param streamUrl Sets an HTTPs URL specifying the location of the audio content.
     */
    public void setStreamUrl(@Nullable String streamUrl) {
        this.streamUrl = streamUrl;
    }

    /**
     *
     * @return the url target for the Read more link in he alexa app.
     */
    @NonNull
    public String getRedirectionUrl() {
        return redirectionUrl;
    }

    /**
     *
     * @param redirectionUrl Sets the url target for the Read more link in he alexa app.
     */
    public void setRedirectionUrl(@NonNull String redirectionUrl) {
        this.redirectionUrl = redirectionUrl;
    }

    /**
     * Flash briefing items should be provided in order from newest to oldest, based on the date value for the item. Alexa may ignore older items.
     * @param o Flash briefing item being compared to
     * @return comparison results based on {@link FlashBriefingItem#updateDate}.
     */
    @Override
    public int compareTo(FlashBriefingItem o) {
        return o.getUpdateDate().compareTo(getUpdateDate());
    }
}
