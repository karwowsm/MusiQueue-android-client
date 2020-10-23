package pl.com.karwowsm.musiqueue.api.error;

import com.google.gson.annotations.SerializedName;

import lombok.Value;

@Value
public class SpotifyErrorResponse {

    private final Integer status;

    private final String message;

    private final Reason reason;

    public enum Reason {

        @SerializedName("NO_PREV_TRACK") NO_PREV_TRACK,

        @SerializedName("NO_NEXT_TRACK") NO_NEXT_TRACK,

        @SerializedName("NO_SPECIFIC_TRACK") NO_SPECIFIC_TRACK,

        @SerializedName("ALREADY_PAUSED") ALREADY_PAUSED,

        @SerializedName("NOT_PAUSED") NOT_PAUSED,

        @SerializedName("NOT_PLAYING_LOCALLY") NOT_PLAYING_LOCALLY,

        @SerializedName("NOT_PLAYING_TRACK") NOT_PLAYING_TRACK,

        @SerializedName("NOT_PLAYING_CONTEXT") NOT_PLAYING_CONTEXT,

        @SerializedName("ENDLESS_CONTEXT") ENDLESS_CONTEXT,

        @SerializedName("CONTEXT_DISALLOW") CONTEXT_DISALLOW,

        @SerializedName("ALREADY_PLAYING") ALREADY_PLAYING,

        @SerializedName("RATE_LIMITED") RATE_LIMITED,

        @SerializedName("REMOTE_CONTROL_DISALLOW") REMOTE_CONTROL_DISALLOW,

        @SerializedName("DEVICE_NOT_CONTROLLABLE") DEVICE_NOT_CONTROLLABLE,

        @SerializedName("VOLUME_CONTROL_DISALLOW") VOLUME_CONTROL_DISALLOW,

        @SerializedName("NO_ACTIVE_DEVICE") NO_ACTIVE_DEVICE,

        @SerializedName("PREMIUM_REQUIRED") PREMIUM_REQUIRED,

        @SerializedName("UNKNOWN") UNKNOWN,
    }

    public interface Listener {

        void onResponse(SpotifyErrorResponse error);
    }
}
