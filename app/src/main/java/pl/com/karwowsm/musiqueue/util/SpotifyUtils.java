package pl.com.karwowsm.musiqueue.util;

import android.app.Activity;
import android.content.Context;

import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;
import com.spotify.sdk.android.player.Config;
import com.spotify.sdk.android.player.Spotify;
import com.spotify.sdk.android.player.SpotifyPlayer;

import lombok.CustomLog;
import lombok.experimental.UtilityClass;
import pl.com.karwowsm.musiqueue.BuildConfig;
import pl.com.karwowsm.musiqueue.api.controller.SpotifyController;
import pl.com.karwowsm.musiqueue.tracklist.player.Player;

@CustomLog
@UtilityClass
public class SpotifyUtils {

    private static final String REDIRECT_URI = "http://localhost:8888/callback";

    public void login(Activity contextActivity, int requestCode) {
        AuthenticationRequest request = new AuthenticationRequest.Builder(BuildConfig.SPOTIFY_CLIENT_ID,
            AuthenticationResponse.Type.TOKEN,
            REDIRECT_URI)
            .setScopes(new String[]{"streaming", "user-top-read", "playlist-read-private", "user-read-private"})
            .build();
        AuthenticationClient.openLoginActivity(contextActivity, requestCode, request);
    }

    public static void initPlayer(Context context, SpotifyCallback spotifyCallback, Runnable onPlaybackEnded) {
        Config playerConfig = new Config(context, SpotifyController.getToken(), BuildConfig.SPOTIFY_CLIENT_ID);
        playerConfig.useCache(false);
        SpotifyPlayer.Builder builder = new SpotifyPlayer.Builder(playerConfig);
        builder.setAudioController(Player.getSpotifyAudioController().setOnPlaybackEnded(onPlaybackEnded));
        Spotify.getPlayer(builder, context, new SpotifyPlayer.InitializationObserver() {
            @Override
            public void onInitialized(SpotifyPlayer spotifyPlayer) {
                spotifyPlayer.addConnectionStateCallback(spotifyCallback);
                spotifyPlayer.addNotificationCallback(spotifyCallback);
                Player.setSpotifyPlayer(spotifyPlayer);
            }

            @Override
            public void onError(Throwable throwable) {
                log.d("Could not initialize player: " + throwable.getMessage());
            }
        });
    }
}
