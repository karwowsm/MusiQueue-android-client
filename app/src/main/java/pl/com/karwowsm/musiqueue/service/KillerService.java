package pl.com.karwowsm.musiqueue.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.spotify.sdk.android.player.Spotify;

import pl.com.karwowsm.musiqueue.api.ws.MessagingService;
import pl.com.karwowsm.musiqueue.util.SpotifyCallback;

public class KillerService extends Service {

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        MessagingService.disconnect();
        Spotify.destroyPlayer(SpotifyCallback.getInstance());
        super.onTaskRemoved(rootIntent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_NOT_STICKY;
    }
}
