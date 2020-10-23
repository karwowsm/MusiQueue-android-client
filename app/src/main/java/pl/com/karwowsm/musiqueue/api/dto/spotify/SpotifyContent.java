package pl.com.karwowsm.musiqueue.api.dto.spotify;

import com.google.gson.annotations.SerializedName;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public abstract class SpotifyContent {

    private String id;

    private String name;

    private Type type;

    public abstract String getImageUrl();

    public enum Type {

        @SerializedName("track") TRACK,

        @SerializedName("artist") ARTIST,

        @SerializedName("album") ALBUM,

        @SerializedName("playlist") PLAYLIST
    }
}
