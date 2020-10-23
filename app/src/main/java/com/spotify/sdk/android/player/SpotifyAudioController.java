package com.spotify.sdk.android.player;

import android.media.AudioTrack;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;

public class SpotifyAudioController extends AudioTrackController {

    private final AudioRingBuffer mAudioBuffer = new AudioRingBuffer(81920);
    private final ExecutorService mExecutorService = Executors.newSingleThreadExecutor();
    private final Object mPlayingMutex = new Object();
    private AudioTrack mAudioTrack;
    private int mSampleRate;
    private int mChannels;
    private final Runnable mAudioRunnable = new Runnable() {
        final short[] pendingSamples = new short[4096];

        public void run() {
            int itemsRead = SpotifyAudioController.this.mAudioBuffer.peek(this.pendingSamples);
            if (itemsRead > 0) {
                int itemsWritten = SpotifyAudioController.this.writeSamplesToAudioOutput(this.pendingSamples, itemsRead);
                SpotifyAudioController.this.mAudioBuffer.remove(itemsWritten);
            }

        }
    };

    public void mute() {
        this.mAudioTrack.setVolume(0);
    }

    public void unMute() {
        this.mAudioTrack.setVolume(1);
    }

    public int onAudioDataDelivered(short[] samples, int sampleCount, int sampleRate, int channels) {
        if (this.mAudioTrack != null && (this.mSampleRate != sampleRate || this.mChannels != channels)) {
            synchronized (this.mPlayingMutex) {
                this.mAudioTrack.release();
                this.mAudioTrack = null;
            }
        }

        this.mSampleRate = sampleRate;
        this.mChannels = channels;
        if (this.mAudioTrack == null) {
            this.createAudioTrack(sampleRate, channels);
        }

        try {
            this.mExecutorService.execute(this.mAudioRunnable);
        } catch (RejectedExecutionException ignored) {
        }

        return this.mAudioBuffer.write(samples, sampleCount);
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
        float maxVolume = AudioTrack.getMaxVolume();
        synchronized (this.mPlayingMutex) {
            this.mAudioTrack = new AudioTrack(3, sampleRate, channelConfig, 2, bufferSize, 1);
            if (this.mAudioTrack.getState() == 1) {
                this.mAudioTrack.setVolume(maxVolume);
                this.mAudioTrack.play();
            } else {
                this.mAudioTrack.release();
                this.mAudioTrack = null;
            }

        }
    }

    private int writeSamplesToAudioOutput(short[] samples, int samplesCount) {
        if (this.isAudioTrackPlaying()) {
            int itemsWritten = this.mAudioTrack.write(samples, 0, samplesCount);
            if (itemsWritten > 0) {
                return itemsWritten;
            }
        }

        return 0;
    }

    private boolean isAudioTrackPlaying() {
        return this.mAudioTrack != null && this.mAudioTrack.getPlayState() == 3;
    }
}
