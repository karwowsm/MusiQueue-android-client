package pl.com.karwowsm.musiqueue.api.request;

import lombok.Builder;
import lombok.Value;
import pl.com.karwowsm.musiqueue.api.dto.Track;
import pl.com.karwowsm.musiqueue.api.dto.soundcloud.SoundCloudTrack;
import pl.com.karwowsm.musiqueue.api.dto.spotify.SpotifyAlbum;
import pl.com.karwowsm.musiqueue.api.dto.spotify.SpotifyTrack;
import pl.com.karwowsm.musiqueue.api.dto.spotify.SpotifyTrackSimplified;
import pl.com.karwowsm.musiqueue.api.dto.youtube.YouTubeVideo;

@Value
@Builder
public final class RoomTrackCreateRequest {

    private final String title;

    private final String artist;

    private final Integer duration;

    private final String imageUrl;

    private final Track.Source source;

    private final String trackId;

    private final Integer index;

    public static RoomTrackCreateRequest of(YouTubeVideo youTubeVideo) {
        return RoomTrackCreateRequest.builder()
            .title(youTubeVideo.getSnippet().getTitle())
            .artist(youTubeVideo.getSnippet().getChannelTitle())
            .duration((int) youTubeVideo.getContentDetails().getDuration().toMillis())
            .imageUrl(youTubeVideo.getSnippet().getHighThumbnail().getUrl())
            .source(Track.Source.YOUTUBE)
            .trackId(youTubeVideo.getId())
            .build();
    }

    public static RoomTrackCreateRequest of(SpotifyTrack spotifyTrack) {
        return of(spotifyTrack, spotifyTrack.getAlbum());
    }

    public static RoomTrackCreateRequest of(SpotifyTrackSimplified spotifyTrack, SpotifyAlbum spotifyAlbum) {
        return RoomTrackCreateRequest.builder()
            .title(spotifyTrack.getName())
            .artist(spotifyTrack.getArtistsNames())
            .duration(spotifyTrack.getDuration_ms())
            .imageUrl(spotifyAlbum.getImageUrl())
            .source(Track.Source.SPOTIFY)
            .trackId(spotifyTrack.getId())
            .build();
    }

    public static RoomTrackCreateRequest of(SoundCloudTrack soundCloudTrack) {
        SoundCloudTrack.Media.Transcoding transcoding = soundCloudTrack.getMedia().getTranscodings().stream()
            .filter(it -> it.getFormat().getProtocol().equals(SoundCloudTrack.Media.Transcoding.Format.Protocol.PROGRESSIVE))
            .findAny()
            .orElse(soundCloudTrack.getMedia().getTranscodings().get(0));

        return RoomTrackCreateRequest.builder()
            .title(soundCloudTrack.getTitle())
            .artist(soundCloudTrack.getUser().getUsername())
            .duration(soundCloudTrack.getDuration())
            .imageUrl(soundCloudTrack.getArtwork_url())
            .source(Track.Source.SOUNDCLOUD)
            .trackId(transcoding.getUrl())
            .build();
    }
}
