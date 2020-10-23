package pl.com.karwowsm.musiqueue.api.dto;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

import lombok.Value;
import lombok.experimental.FieldNameConstants;

@Value
@FieldNameConstants
public class Room implements Serializable {

    private final UUID id;

    private final String name;

    private final Integer userQueuedTracksLimit;

    private final UserAccount host;

    private final RoomTrack currentTrack;

    private final Instant startedPlayingAt;

    private final Integer membersCount;

    private final Boolean playing;
}
