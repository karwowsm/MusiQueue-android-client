package pl.com.karwowsm.musiqueue.api.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import lombok.Value;

@Value
public class RoomTracklist {

    private final List<RoomTrack> tracklist;

    private final UUID currentTrackId;

    private final Instant startedPlayingAt;
}
