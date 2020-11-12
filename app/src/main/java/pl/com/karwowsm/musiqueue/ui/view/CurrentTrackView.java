package pl.com.karwowsm.musiqueue.ui.view;

import android.content.res.ColorStateList;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import androidx.annotation.ColorInt;

import lombok.Getter;
import pl.com.karwowsm.musiqueue.MusiQueueApplication;
import pl.com.karwowsm.musiqueue.R;
import pl.com.karwowsm.musiqueue.api.dto.Track;
import pl.com.karwowsm.musiqueue.tracklist.player.Player;
import pl.com.karwowsm.musiqueue.ui.util.TrackUtils;

public class CurrentTrackView {

    private final LinearLayout layout;
    private final RelativeLayout currentTrackBox;
    private final CustomNetworkImageView imageView;
    private final LinearLayout videoBox;
    private final ProgressBar progressBar;

    private Track currentTrack;
    @Getter
    private boolean isFullscreen;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable refresher = this::refresh;

    public CurrentTrackView(LinearLayout layout) {
        currentTrackBox = layout.findViewById(R.id.current_track_box_layout);
        imageView = layout.findViewById(R.id.current_track_img);
        imageView.setDefaultImageResId(R.drawable.ico);
        imageView.setAlpha(0.5f);
        videoBox = layout.findViewById(R.id.video_box_layout);
        progressBar = layout.findViewById(R.id.progress_bar);
        this.layout = layout;
    }

    public void update(Track track) {
        this.currentTrack = track;
        if (!currentTrack.isYouTubeContent()) {
            hideVideoBox();
            if (isFullscreen) {
                Player.exitFullscreen();
            } else {
                showProgressBar();
            }
        }
        setProgressBarColor(progressBar.getResources().getColor(TrackUtils.getColor(currentTrack.getSource())));
        progressBar.setMax(currentTrack.getDuration());
        if (currentTrack.getImageUrl() != null) {
            imageView.setImageUrl(currentTrack.getImageUrl(), MusiQueueApplication.getInstance().getImageLoader());
        } else {
            imageView.setImageResource(TrackUtils.getImageResId(currentTrack.getSource()));
        }
        restart();
    }

    public void showVideoBox() {
        imageView.setVisibility(View.GONE);
        videoBox.setVisibility(View.VISIBLE);
        Player.enableFullscreen();
    }

    public void hideVideoBox() {
        imageView.setVisibility(View.VISIBLE);
        videoBox.setVisibility(View.GONE);
        Player.disableFullscreen();
    }

    public void showProgressBar() {
        if (!isFullscreen) {
            layout.setPadding(0, 0, 0, 0);
            currentTrackBox.getLayoutParams().height = layout.getResources().getDimensionPixelSize(R.dimen.current_track_box_height);
            progressBar.setVisibility(View.VISIBLE);
        }
    }

    public void hideProgressBar() {
        if (!isFullscreen && currentTrack.isYouTubeContent()) {
            int padding = layout.getResources().getDimensionPixelSize(R.dimen.current_track_view_bottom_padding);
            layout.setPadding(0, 0, 0, padding);
            currentTrackBox.getLayoutParams().height = layout.getResources().getDimensionPixelSize(R.dimen.current_track_box_height) + padding;
            progressBar.setVisibility(View.GONE);
        }
    }

    public void setFullscreen(boolean isFullscreen) {
        this.isFullscreen = isFullscreen;
        if (isFullscreen) {
            layout.setPadding(0, 0, 0, 0);
            currentTrackBox.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
            progressBar.setVisibility(View.GONE);
        } else {
            if (currentTrack.isYouTubeContent() && Player.isPlaying() && !Player.isYouTubePlayerPlaying()) {
                hideProgressBar();
            } else {
                showProgressBar();
            }
        }
    }

    public void restart() {
        finish();
        refresh();
    }

    public void finish() {
        handler.removeCallbacks(refresher);
    }

    private void refresh() {
        if (Player.isPlaying()) {
            imageView.setAlpha(1f);
            progressBar.setProgress(Player.getPlaybackPosition());
            handler.postDelayed(refresher, currentTrack.getDuration() / 230);
        } else {
            imageView.setAlpha(0.5f);
            progressBar.setProgress(currentTrack.getDuration());
        }
    }

    private void setProgressBarColor(@ColorInt int color) {
        progressBar.setProgressTintList(ColorStateList.valueOf(color));
    }
}
