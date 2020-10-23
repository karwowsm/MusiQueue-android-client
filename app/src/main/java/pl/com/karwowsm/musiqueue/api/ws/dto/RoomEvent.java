package pl.com.karwowsm.musiqueue.api.ws.dto;

import com.google.gson.annotations.SerializedName;

import lombok.EqualsAndHashCode;
import lombok.Value;
import pl.com.karwowsm.musiqueue.api.dto.Room;

@Value
@EqualsAndHashCode(callSuper = false)
public class RoomEvent extends Event<RoomEvent.Type> {

    private final Room room;

    private final Type type;

    public enum Type {

        @SerializedName("UPDATED") UPDATED,

        @SerializedName("DELETED") DELETED
    }
}
