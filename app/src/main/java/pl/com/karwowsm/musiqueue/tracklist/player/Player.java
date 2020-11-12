package pl.com.karwowsm.musiqueue.tracklist.player;

import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.youtube.player.YouTubePlayer;
import com.spotify.sdk.android.player.SpotifyAudioController;
import com.spotify.sdk.android.player.SpotifyPlayer;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import lombok.CustomLog;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.UtilityClass;
import pl.com.karwowsm.musiqueue.BuildConfig;
import pl.com.karwowsm.musiqueue.api.controller.SoundCloudController;
import pl.com.karwowsm.musiqueue.api.dto.RoomTrack;

@CustomLog
@UtilityClass
public final class Player {

    @Getter
    private static final SpotifyAudioController spotifyAudioController = new SpotifyAudioController();

    @Setter
    private static SimpleExoPlayer exoPlayer;
    @Setter
    private static YouTubePlayer youTubePlayer;
    @Setter
    private static SpotifyPlayer spotifyPlayer;
    @Getter
    private static boolean isPlaying;
    @Getter
    private static boolean isMuted;
    @Getter
    private static Instant startedPlayingAt;
    @Setter
    private static boolean youTubePlayerPaused;

    public static boolean prepare(RoomTrack roomTrack, Instant startedPlayingAt) {
        stop();
        Player.startedPlayingAt = startedPlayingAt;
        return getPosition() < roomTrack.getTrack().getDuration();
    }

    public static void play(RoomTrack roomTrack) {
        int position = getPosition();
        log.d("Playing: " + roomTrack.getTrack().getSource() + " " + roomTrack.getId() + " " + position + " / " + roomTrack.getTrack().getDuration());
        if (position < roomTrack.getTrack().getDuration()) {
            if (roomTrack.isUploadedContent()) {
                if (isExoPlayerSet()) {
                    playMediaUri(buildUploadedTrackUri(roomTrack));
                }
            } else if (roomTrack.isYouTubeContent()) {
                if (isYouTubePlayerSet()) {
                    youTubePlayer.loadVideo(roomTrack.getTrack().getTrackId(), position);
                }
            } else if (roomTrack.isSpotifyContent()) {
                if (isSpotifyPlayerSet()) {
                    spotifyPlayer.playUri(null, buildSpotifyTrackUri(roomTrack), 0, position);
                }
            } else if (roomTrack.isSoundCloudContent()) {
                if (isExoPlayerSet()) {
                    SoundCloudController.generateSoundCloudTrackUrl(roomTrack.getTrack().getTrackId(),
                        soundCloudTrackUrl -> playMediaUri(soundCloudTrackUrl.getUrl())
                    );
                }
            }
            isPlaying = true;
        }
    }

    public static void stop() {
        if (isExoPlayerPlaying()) {
            exoPlayer.pause();
        }
        if (isYouTubePlayerPlaying()) {
            youTubePlayer.pause();
        }
        if (isSpotifyPlayerPlaying()) {
            spotifyPlayer.pause(null);
        }
        isPlaying = false;
    }

    public static void seekYouTubePlayerPositionIfNeeded() {
        if (youTubePlayerPaused) {
            youTubePlayer.seekToMillis(getPosition());
        }
        youTubePlayerPaused = false;
    }

    public static void enableFullscreen() {
        if (isYouTubePlayerSet()) {
            youTubePlayer.addFullscreenControlFlag(YouTubePlayer.FULLSCREEN_FLAG_ALWAYS_FULLSCREEN_IN_LANDSCAPE);
            youTubePlayer.addFullscreenControlFlag(YouTubePlayer.FULLSCREEN_FLAG_CUSTOM_LAYOUT);
        }
    }

    public static void disableFullscreen() {
        if (isYouTubePlayerSet()) {
            youTubePlayer.setFullscreenControlFlags(YouTubePlayer.FULLSCREEN_FLAG_CONTROL_ORIENTATION + YouTubePlayer.FULLSCREEN_FLAG_CONTROL_SYSTEM_UI);
        }
    }

    public static void exitFullscreen() {
        youTubePlayer.setFullscreen(false);
    }

    public static int getPlaybackPosition() {
        if (isExoPlayerPlaying()) {
            return (int) exoPlayer.getCurrentPosition();
        }
        if (isYouTubePlayerPlaying()) {
            return youTubePlayer.getCurrentTimeMillis();
        }
        if (isSpotifyPlayerPlaying()) {
            return (int) spotifyPlayer.getPlaybackState().positionMs;
        }
        return getPosition();
    }

    public static void mute() {
        if (isExoPlayerSet()) {
            exoPlayer.setVolume(0);
        }
        spotifyAudioController.mute();
        isMuted = true;
    }

    public static void unMute() {
        if (isExoPlayerSet()) {
            exoPlayer.setVolume(1);
        }
        spotifyAudioController.unMute();
        isMuted = false;
    }

    public static boolean isExoPlayerSet() {
        return exoPlayer != null;
    }

    public static boolean isYouTubePlayerSet() {
        return youTubePlayer != null;
    }

    public static boolean isSpotifyPlayerSet() {
        return spotifyPlayer != null;
    }

    public static boolean isExoPlayerPlaying() {
        return isExoPlayerSet() && exoPlayer.isPlaying();
    }

    public static boolean isYouTubePlayerPlaying() {
        return isYouTubePlayerSet() && youTubePlayer.isPlaying();
    }

    public static void releaseExoPlayer() {
        if (isExoPlayerSet()) {
            exoPlayer.release();
            exoPlayer = null;
        }
    }

    public static void releaseYouTubePlayer() {
        if (isYouTubePlayerSet()) {
            youTubePlayer.release();
            youTubePlayer = null;
        }
    }

    private static void playMediaUri(String uri) {
        exoPlayer.setMediaItem(MediaItem.fromUri(uri));
        exoPlayer.prepare();
        exoPlayer.play();
        exoPlayer.seekTo(getPosition());
    }

    private static boolean isSpotifyPlayerPlaying() {
        return isSpotifyPlayerSet()
            && spotifyPlayer.getPlaybackState() != null
            && spotifyPlayer.getPlaybackState().isPlaying;
    }

    private static int getPosition() {
        return (int) startedPlayingAt.until(Instant.now(), ChronoUnit.MILLIS);
    }

    private static String buildUploadedTrackUri(RoomTrack roomTrack) {
        return BuildConfig.BASE_URL + String.format("/rooms/%s/tracklist/play/%s", roomTrack.getRoomId(), roomTrack.getId());
    }

    private static String buildSpotifyTrackUri(RoomTrack roomTrack) {
        return String.format("spotify:track:%s", roomTrack.getTrack().getTrackId());
    }
}
