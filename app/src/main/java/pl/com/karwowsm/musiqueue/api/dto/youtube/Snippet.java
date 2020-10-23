package pl.com.karwowsm.musiqueue.api.dto.youtube;

import com.google.gson.annotations.SerializedName;

import java.util.Map;

import lombok.Value;

@Value
public class Snippet {

    private final String title;

    private final Map<ThumbnailVersion, Thumbnail> thumbnails;

    private final String channelTitle;

    public Thumbnail getDefaultThumbnail() {
        return thumbnails.get(ThumbnailVersion.DEFAULT);
    }

    public Thumbnail getHighThumbnail() {
        return thumbnails.get(ThumbnailVersion.HIGH);
    }

    public enum ThumbnailVersion {

        @SerializedName("default") DEFAULT,

        @SerializedName("medium") MEDIUM,

        @SerializedName("high") HIGH,

        @SerializedName("standard") STANDARD,

        @SerializedName("maxres") MAXRES
    }

    @Value
    public static class Thumbnail {

        private final String url;
    }
}
