package pl.com.karwowsm.musiqueue.api.dto;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.UUID;

import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.experimental.FieldNameConstants;
import pl.com.karwowsm.musiqueue.BuildConfig;

@Value
@FieldNameConstants
public class Track implements Serializable {

    private final UUID id;

    private final String title;

    private final String artist;

    private final Integer duration;

    private final String imageUrl;

    private final Source source;

    private final String trackId;

    @EqualsAndHashCode.Exclude
    private final Integer queuedNumber;

    public enum Source {

        @SerializedName("UPLOADED") UPLOADED,

        @SerializedName("YOUTUBE") YOUTUBE,

        @SerializedName("SPOTIFY") SPOTIFY,

        @SerializedName("SOUNDCLOUD") SOUNDCLOUD
    }

    public String getImageUrl() {
        if (source != Source.UPLOADED) {
            return imageUrl;
        } else if (imageUrl != null) {
            return BuildConfig.BASE_URL + imageUrl;
        } else {
            return null;
        }
    }
}
