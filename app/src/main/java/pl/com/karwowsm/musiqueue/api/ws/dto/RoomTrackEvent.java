package pl.com.karwowsm.musiqueue.api.ws.dto;

import com.google.gson.annotations.SerializedName;

import java.time.Instant;

import lombok.EqualsAndHashCode;
import lombok.Value;
import pl.com.karwowsm.musiqueue.api.dto.RoomTrack;

@Value
@EqualsAndHashCode(callSuper = false)
public class RoomTrackEvent extends Event<RoomTrackEvent.Type> {

    private final RoomTrack track;

    private final Instant timestamp;

    private final Type type;

    public enum Type {

        @SerializedName("ADDED") ADDED,

        @SerializedName("DELETED") DELETED,

        @SerializedName("PLAYED") PLAYED
    }
}
