package pl.com.karwowsm.musiqueue.util;

import com.google.android.youtube.player.YouTubePlayer;

import lombok.CustomLog;
import lombok.RequiredArgsConstructor;

@CustomLog
@RequiredArgsConstructor
public class YouTubePlayerCallback implements YouTubePlayer.PlayerStateChangeListener, YouTubePlayer.PlaybackEventListener {

    private final Listener listener;

    @Override
    public void onPlaying() {
        log.d("PlaybackEvent: onPlaying");
        listener.onPlaying();
    }

    @Override
    public void onPaused() {
        log.d("PlaybackEvent: onPaused");
        listener.onPaused();
    }

    @Override
    public void onStopped() {
        log.d("PlaybackEvent: onStopped");
    }

    @Override
    public void onBuffering(boolean b) {
        log.d("PlaybackEvent: onBuffering " + b);
    }

    @Override
    public void onSeekTo(int i) {
        log.d("PlaybackEvent: onSeekTo " + i);
    }

    @Override
    public void onLoading() {
        log.d("PlayerStateChange: onLoading");
    }

    @Override
    public void onLoaded(String s) {
        log.d("PlayerStateChange: onLoaded " + s);
        listener.onLoaded();
    }

    @Override
    public void onAdStarted() {
        log.d("PlayerStateChange: onAdStarted");
    }

    @Override
    public void onVideoStarted() {
        log.d("PlayerStateChange: onVideoStarted");
    }

    @Override
    public void onVideoEnded() {
        log.d("PlayerStateChange: onVideoEnded");
        listener.onVideoEnded();
    }

    @Override
    public void onError(YouTubePlayer.ErrorReason errorReason) {
        log.d("PlayerStateChange: onError " + errorReason);
        if (errorReason == YouTubePlayer.ErrorReason.UNAUTHORIZED_OVERLAY) {
            listener.onPaused();
        }
    }

    public interface Listener {

        void onLoaded();

        void onPlaying();

        void onPaused();

        void onVideoEnded();
    }
}
