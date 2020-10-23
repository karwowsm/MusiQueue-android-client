package pl.com.karwowsm.musiqueue.api.dto.spotify;

import java.util.List;
import java.util.Objects;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;
import lombok.experimental.NonFinal;

@Value
@NonFinal
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class SpotifyTrackSimplified extends SpotifyContent {

    private final List<SpotifyArtist> artists;

    private final Integer duration_ms;

    private final Boolean is_playable;

    @NonFinal
    private String artistsNames;

    @Override
    public String getImageUrl() {
        return null;
    }

    public String getArtistsNames() {
        if (Objects.nonNull(artistsNames)) {
            return artistsNames;
        }
        StringBuilder sb = new StringBuilder(artists.get(0).getName());
        for (int i = 1; i < artists.size(); i++) {
            sb.append(", ").append(artists.get(i).getName());
        }
        artistsNames = sb.toString();
        return artistsNames;
    }
}
