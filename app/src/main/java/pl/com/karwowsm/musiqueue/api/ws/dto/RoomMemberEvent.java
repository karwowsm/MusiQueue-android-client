package pl.com.karwowsm.musiqueue.api.ws.dto;

import com.google.gson.annotations.SerializedName;

import lombok.EqualsAndHashCode;
import lombok.Value;
import pl.com.karwowsm.musiqueue.api.dto.UserAccount;

@Value
@EqualsAndHashCode(callSuper = false)
public class RoomMemberEvent extends Event<RoomMemberEvent.Type> {

    private final UserAccount userAccount;

    private final Type type;

    public enum Type {

        @SerializedName("JOINED") JOINED,

        @SerializedName("LEFT") LEFT
    }
}
