package pl.com.karwowsm.musiqueue.api.dto.spotify;

import java.util.List;

import lombok.Value;

@Value
public class SpotifyTrackList {

    private final List<SpotifyTrack> tracks;
}
