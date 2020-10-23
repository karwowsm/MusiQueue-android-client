package pl.com.karwowsm.musiqueue.api.request;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public final class RoomUpdateRequest {

    private final String name;

    private final Integer userQueuedTracksLimit;
}
