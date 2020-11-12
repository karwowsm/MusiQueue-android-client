package pl.com.karwowsm.musiqueue.util;

import com.spotify.sdk.android.player.ConnectionStateCallback;
import com.spotify.sdk.android.player.Error;
import com.spotify.sdk.android.player.PlayerEvent;
import com.spotify.sdk.android.player.SpotifyPlayer;

import lombok.CustomLog;
import lombok.RequiredArgsConstructor;

@CustomLog
@RequiredArgsConstructor
public class SpotifyCallback implements SpotifyPlayer.NotificationCallback, ConnectionStateCallback {

    private final Listener listener;

    @Override
    public void onPlaybackEvent(PlayerEvent playerEvent) {
        log.d("Playback event received: " + playerEvent.name());
        switch (playerEvent) {
            // Handle event type as necessary
            default:
                break;
        }
    }

    @Override
    public void onPlaybackError(Error error) {
        log.d("Playback error received: " + error.name());
        listener.onPlaybackError();
        switch (error) {
            // Handle error type as necessary
            default:
                break;
        }
    }

    @Override
    public void onLoggedIn() {
        log.d("User logged in");
        listener.onLoggedIn();
    }

    @Override
    public void onLoggedOut() {
        log.d("User logged out");
    }

    @Override
    public void onLoginFailed(Error error) {
        log.d("Login failed");
    }

    @Override
    public void onTemporaryError() {
        log.d("Temporary error occurred");
    }

    @Override
    public void onConnectionMessage(String message) {
        log.d("Received connection message: " + message);
    }

    public interface Listener {

        void onLoggedIn();

        void onPlaybackError();
    }
}
