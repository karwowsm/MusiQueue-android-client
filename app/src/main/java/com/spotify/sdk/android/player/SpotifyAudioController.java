package com.spotify.sdk.android.player;

import android.media.AudioTrack;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;

import lombok.Setter;
import lombok.experimental.Accessors;

public class SpotifyAudioController extends AudioTrackController {

    private final AudioRingBuffer mAudioBuffer = new AudioRingBuffer(81920);
    private final ExecutorService mExecutorService = Executors.newSingleThreadExecutor();
    private final Object mPlayingMutex = new Object();
    private AudioTrack mAudioTrack;
    private int mSampleRate;
    private int mChannels;
    private final Runnable mAudioRunnable = new Runnable() {
        final short[] pendingSamples = new short[4096];
        private boolean audioEnded;

        @Override
        public void run() {
            int itemsRead = mAudioBuffer.peek(pendingSamples);
            if (itemsRead > 0) {
                int itemsWritten = writeSamplesToAudioOutput(pendingSamples, itemsRead);
                mAudioBuffer.remove(itemsWritten);
                audioEnded = false;
            } else if (samplesEnded && !audioEnded) {
                if (onPlaybackEnded != null) {
                    onPlaybackEnded.run();
                }
                audioEnded = true;
            }
        }
    };
    private boolean isMuted;
    @Setter
    @Accessors(chain = true)
    private Runnable onPlaybackEnded;
    private boolean samplesEnded;

    public void mute() {
        isMuted = true;
        setAudioTrackVolume();
    }

    public void unMute() {
        isMuted = false;
        setAudioTrackVolume();
    }

    @Override
    public int onAudioDataDelivered(short[] samples, int sampleCount, int sampleRate, int channels) {
        if (mAudioTrack != null && (this.mSampleRate != sampleRate || this.mChannels != channels)) {
            synchronized (mPlayingMutex) {
                mAudioTrack.release();
                mAudioTrack = null;
            }
        }

        this.mSampleRate = sampleRate;
        this.mChannels = channels;
        if (mAudioTrack == null) {
            createAudioTrack(sampleRate, channels);
        }

        try {
            mExecutorService.execute(mAudioRunnable);
        } catch (RejectedExecutionException ignored) {
        }

        samplesEnded = sampleCount <= 0;

        return mAudioBuffer.write(samples, sampleCount);
    }

    private void createAudioTrack(int sampleRate, int channels) {
        byte channelConfig;
        switch (channels) {
            case 0:
                throw new IllegalStateException("Input source has 0 channels");
            case 1:
                channelConfig = 4;
                break;
            case 2:
                channelConfig = 12;
                break;
            default:
                throw new IllegalArgumentException("Unsupported input source has " + channels + " channels");
        }

        int bufferSize = AudioTrack.getMinBufferSize(sampleRate, channelConfig, 2) * 2;
        synchronized (mPlayingMutex) {
            mAudioTrack = new AudioTrack(3, sampleRate, channelConfig, 2, bufferSize, 1);
            if (mAudioTrack.getState() == 1) {
                setAudioTrackVolume();
                mAudioTrack.play();
            } else {
                mAudioTrack.release();
                mAudioTrack = null;
            }

        }
    }

    private int writeSamplesToAudioOutput(short[] samples, int samplesCount) {
        if (isAudioTrackPlaying()) {
            int itemsWritten = mAudioTrack.write(samples, 0, samplesCount);
            if (itemsWritten > 0) {
                return itemsWritten;
            }
        }
        return 0;
    }

    private boolean isAudioTrackPlaying() {
        return mAudioTrack != null && mAudioTrack.getPlayState() == 3;
    }

    private void setAudioTrackVolume() {
        if (mAudioTrack != null) {
            mAudioTrack.setVolume(isMuted ? AudioTrack.getMinVolume() : AudioTrack.getMaxVolume());
        }
    }
}
