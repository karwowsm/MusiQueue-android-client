package pl.com.karwowsm.musiqueue.api.dto.spotify;

import lombok.Value;

@Value
public class SpotifySearchResult {

    private final Page<SpotifyTrack> tracks;

    private final Page<SpotifyArtist> artists;

    private final Page<SpotifyPlaylist> playlists;

    private final Page<SpotifyAlbum> albums;
}
