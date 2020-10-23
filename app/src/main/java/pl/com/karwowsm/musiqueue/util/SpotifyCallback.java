package pl.com.karwowsm.musiqueue.util;

import android.app.Activity;
import android.content.Context;

import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;
import com.spotify.sdk.android.player.Config;
import com.spotify.sdk.android.player.ConnectionStateCallback;
import com.spotify.sdk.android.player.Error;
import com.spotify.sdk.android.player.PlayerEvent;
import com.spotify.sdk.android.player.Spotify;
import com.spotify.sdk.android.player.SpotifyAudioController;
import com.spotify.sdk.android.player.SpotifyPlayer;

import java.time.Instant;

import lombok.CustomLog;
import lombok.Getter;
import lombok.Setter;
import pl.com.karwowsm.musiqueue.BuildConfig;
import pl.com.karwowsm.musiqueue.tracklist.player.Player;

@CustomLog
public class SpotifyCallback implements SpotifyPlayer.NotificationCallback, ConnectionStateCallback {

    public static final int REQUEST_CODE = 1337;

    @Getter
    private static final SpotifyCallback instance = new SpotifyCallback();

    private static final String REDIRECT_URI = "http://localhost:8888/callback";

    @Setter
    private Listener listener;
    @Getter
    private String token;
    private Instant tokenExpirationTime;
    private boolean trackDelivered = false;

    private SpotifyCallback() {
    }

    public void login(Activity contextActivity) {
        AuthenticationRequest request = new AuthenticationRequest.Builder(BuildConfig.SPOTIFY_CLIENT_ID,
            AuthenticationResponse.Type.TOKEN,
            REDIRECT_URI)
            .setScopes(new String[]{"user-read-private", "streaming", "user-top-read", "playlist-read-private"})
            .build();
        AuthenticationClient.openLoginActivity(contextActivity, REQUEST_CODE, request);
    }

    public void initPlayer(Context context, AuthenticationResponse authResponse) {
        token = authResponse.getAccessToken();
        tokenExpirationTime = Instant.now().plusSeconds(authResponse.getExpiresIn());

        Config playerConfig = new Config(context, token, BuildConfig.SPOTIFY_CLIENT_ID);
        playerConfig.useCache(false);
        SpotifyPlayer.Builder builder = new SpotifyPlayer.Builder(playerConfig);
        SpotifyAudioController spotifyAudioController = new SpotifyAudioController();
        builder.setAudioController(spotifyAudioController);
        Spotify.getPlayer(builder, instance, new SpotifyPlayer.InitializationObserver() {
            @Override
            public void onInitialized(SpotifyPlayer spotifyPlayer) {
                spotifyPlayer.addConnectionStateCallback(instance);
                spotifyPlayer.addNotificationCallback(instance);
                Player.setSpotifyPlayer(spotifyPlayer);
                Player.setSpotifyAudioController(spotifyAudioController);
            }

            @Override
            public void onError(Throwable throwable) {
                log.d("Could not initialize player: " + throwable.getMessage());
            }
        });
    }

    public boolean isTokenExpired() {
        return token != null && tokenExpirationTime.isBefore(Instant.now());
    }

    @Override
    public void onPlaybackEvent(PlayerEvent playerEvent) {
        log.d("Playback event received: " + playerEvent.name());
        switch (playerEvent) {
            case UNKNOWN:
            case kSpPlaybackNotifyPlay:
            case kSpPlaybackNotifyPause:
                break;
            case kSpPlaybackNotifyTrackChanged:
                if (trackDelivered) {
                    listener.onTrackDelivered();
                    trackDelivered = false;
                }
                break;
            case kSpPlaybackNotifyNext:
            case kSpPlaybackNotifyPrev:
            case kSpPlaybackNotifyShuffleOn:
            case kSpPlaybackNotifyShuffleOff:
            case kSpPlaybackNotifyRepeatOn:
            case kSpPlaybackNotifyRepeatOff:
            case kSpPlaybackNotifyBecameActive:
            case kSpPlaybackNotifyBecameInactive:
            case kSpPlaybackNotifyLostPermission:
            case kSpPlaybackEventAudioFlush:
            case kSpPlaybackNotifyAudioDeliveryDone:
            case kSpPlaybackNotifyContextChanged:
                break;
            case kSpPlaybackNotifyTrackDelivered:
                trackDelivered = true;
                break;
            case kSpPlaybackNotifyMetadataChanged:
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

        void onTrackDelivered();

        void onPlaybackError();
    }
}
