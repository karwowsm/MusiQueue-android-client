package pl.com.karwowsm.musiqueue.api.dto.spotify;

import java.util.List;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;

@Value
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class SpotifyTrack extends SpotifyTrackSimplified {

    private final SpotifyAlbum album;

    public SpotifyTrack(List<SpotifyArtist> artists, Integer duration_ms, Boolean is_playable, String artistsNames, SpotifyAlbum album) {
        super(artists, duration_ms, is_playable, artistsNames);
        this.album = album;
    }

    @Override
    public String getImageUrl() {
        return album.getImageUrl();
    }
}
