package pl.com.karwowsm.musiqueue.api.dto;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

import lombok.EqualsAndHashCode;
import lombok.Setter;
import lombok.Value;
import lombok.experimental.NonFinal;

@Value
public class RoomTrack implements Serializable {

    private final UUID id;

    private final Track track;

    private final UUID roomId;

    private final UserAccount owner;

    private final Integer userIndex;

    private final Instant addedAt;

    @NonFinal
    private Integer index;

    @Setter
    @NonFinal
    @EqualsAndHashCode.Exclude
    private Boolean isPlayed;

    public void incrementIndex() {
        index++;
    }

    public void decrementIndex() {
        index--;
    }

    public boolean isUploadedContent() {
        return track.getSource().equals(Track.Source.UPLOADED);
    }

    public boolean isYouTubeContent() {
        return track.getSource().equals(Track.Source.YOUTUBE);
    }

    public boolean isSpotifyContent() {
        return track.getSource().equals(Track.Source.SPOTIFY);
    }

    public boolean isSoundCloudContent() {
        return track.getSource().equals(Track.Source.SOUNDCLOUD);
    }
}
