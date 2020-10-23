package pl.com.karwowsm.musiqueue.util;

import com.google.android.exoplayer2.Player;

import lombok.CustomLog;
import lombok.RequiredArgsConstructor;

import static com.google.android.exoplayer2.Player.STATE_BUFFERING;
import static com.google.android.exoplayer2.Player.STATE_ENDED;
import static com.google.android.exoplayer2.Player.STATE_IDLE;
import static com.google.android.exoplayer2.Player.STATE_READY;

@CustomLog
@RequiredArgsConstructor
public class ExoPlayerCallback implements Player.EventListener {

    private final Listener listener;

    @Override
    public void onPlaybackStateChanged(int state) {
        switch (state) {
            case STATE_IDLE:
                log.d("PlaybackStateChanged: STATE_IDLE");
                break;
            case STATE_BUFFERING:
                log.d("PlaybackStateChanged: STATE_BUFFERING");
                break;
            case STATE_READY:
                log.d("PlaybackStateChanged: STATE_READY");
                break;
            case STATE_ENDED:
                log.d("PlaybackStateChanged: STATE_ENDED");
                listener.onPlaybackEnded();
                break;
        }
    }

    public interface Listener {

        void onPlaybackEnded();
    }
}
