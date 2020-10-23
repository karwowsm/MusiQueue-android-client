package pl.com.karwowsm.musiqueue.ui.view;

import android.content.res.ColorStateList;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import androidx.annotation.ColorInt;

import lombok.Getter;
import pl.com.karwowsm.musiqueue.AppController;
import pl.com.karwowsm.musiqueue.R;
import pl.com.karwowsm.musiqueue.api.dto.Track;
import pl.com.karwowsm.musiqueue.tracklist.player.Player;
import pl.com.karwowsm.musiqueue.ui.util.TrackUtils;

public class CurrentTrackView {

    private final LinearLayout layout;
    private final CustomNetworkImageView imageView;
    private final LinearLayout videoBox;
    private final ProgressBar progressBar;

    private Track currentTrack;
    @Getter
    private boolean isFullscreen;
    private boolean exitFullscreenCalled;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable refresher = this::refreshNowPlayingView;

    public CurrentTrackView(LinearLayout layout) {
        imageView = layout.findViewById(R.id.current_track_img);
        imageView.setDefaultImageResId(R.drawable.ico);
        imageView.setAlpha(0.5f);
        videoBox = layout.findViewById(R.id.video_box_layout);
        progressBar = layout.findViewById(R.id.progress_bar);
        this.layout = layout;
    }

    public void update(Track track) {
        this.currentTrack = track;
        if (currentTrack.getSource().equals(Track.Source.YOUTUBE) && Player.isPlaying()) {
            showVideoBox();
        } else {
            hideVideoBox();
            if (isFullscreen) {
                Player.exitFullscreen();
                exitFullscreenCalled = true;
            } else {
                showProgressBar();
            }
        }
        setProgressBarColor(progressBar.getResources().getColor(TrackUtils.getColor(track.getSource())));
        progressBar.setMax(currentTrack.getDuration());
        if (currentTrack.getImageUrl() != null) {
            imageView.setImageUrl(currentTrack.getImageUrl(), AppController.getInstance().getImageLoader());
        } else {
            imageView.setImageResource(TrackUtils.getImageResId(currentTrack.getSource()));
        }
        startRefreshingNowPlayingView();
    }

    public void finish() {
        handler.removeCallbacks(refresher);
    }

    public void showProgressBar() {
        if (!isFullscreen) {
            layout.setPadding(0, 0, 0, 0);
            videoBox.getLayoutParams().height = layout.getResources().getDimensionPixelSize(R.dimen.currently_playing_box_height);
            progressBar.setVisibility(View.VISIBLE);
            startRefreshingNowPlayingView();
        }
    }

    public void hideProgressBar() {
        if (!isFullscreen) {
            int padding = layout.getResources().getDimensionPixelSize(R.dimen.currently_playing_view_bottom_padding);
            layout.setPadding(0, 0, 0, padding);
            videoBox.getLayoutParams().height = layout.getResources().getDimensionPixelSize(R.dimen.currently_playing_box_height) + padding;
            progressBar.setVisibility(View.GONE);
        }
    }

    public void setFullscreen(boolean isFullscreen) {
        this.isFullscreen = isFullscreen;
        if (isFullscreen) {
            layout.setPadding(0, 0, 0, 0);
            videoBox.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
            progressBar.setVisibility(View.GONE);
        } else {
            if (!exitFullscreenCalled && !Player.isYouTubePlayerPlaying()) {
                hideProgressBar();
            } else {
                videoBox.getLayoutParams().height = layout.getResources().getDimensionPixelSize(R.dimen.currently_playing_box_height);
                progressBar.setVisibility(View.VISIBLE);
            }
            exitFullscreenCalled = false;
        }
    }

    private void showVideoBox() {
        imageView.setVisibility(View.GONE);
        videoBox.setVisibility(View.VISIBLE);
        Player.enableFullscreen();
    }

    private void hideVideoBox() {
        imageView.setVisibility(View.VISIBLE);
        videoBox.setVisibility(View.GONE);
        Player.disableFullscreen();
    }

    private void setProgressBarColor(@ColorInt int color) {
        progressBar.setProgressTintList(ColorStateList.valueOf(color));
    }

    private void startRefreshingNowPlayingView() {
        handler.removeCallbacks(refresher);
        refreshNowPlayingView();
    }

    private void refreshNowPlayingView() {
        if (Player.isPlaying()) {
            imageView.setAlpha(1f);
            progressBar.setProgress(Player.getPlaybackPosition());
            handler.postDelayed(refresher, currentTrack.getDuration() / 230);
        } else {
            imageView.setAlpha(0.5f);
            progressBar.setProgress(currentTrack.getDuration());
        }
    }
}
