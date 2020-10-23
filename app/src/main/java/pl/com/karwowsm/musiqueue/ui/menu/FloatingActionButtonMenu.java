package pl.com.karwowsm.musiqueue.ui.menu;

import android.animation.Animator;
import android.view.View;
import android.widget.ImageButton;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class FloatingActionButtonMenu {

    private final FloatingActionButton fileBtn;
    private final FloatingActionButton youTubeBtn;
    private final FloatingActionButton spotifyBtn;
    private final FloatingActionButton soundCloudBtn;
    private final float singleMargin;

    @Getter
    private boolean shown;

    public void show() {
        if (!shown) {
            toggle();
        }
    }

    public void close() {
        if (shown) {
            toggle();
        }
    }

    private void toggle() {
        float translationFactor = shown ? 0 : -1;
        fileBtn.animate().translationY(translationFactor * singleMargin).setListener(new AnimatorListener(fileBtn, !shown));
        youTubeBtn.animate().translationY(translationFactor * singleMargin * 2).setListener(new AnimatorListener(youTubeBtn, !shown));
        spotifyBtn.animate().translationY(translationFactor * singleMargin * 3).setListener(new AnimatorListener(spotifyBtn, !shown));
        soundCloudBtn.animate().translationY(translationFactor * singleMargin * 4).setListener(new AnimatorListener(soundCloudBtn, !shown));
        shown = !shown;
    }

    private static class AnimatorListener implements Animator.AnimatorListener {

        private final ImageButton button;
        private final boolean isOpening;

        public AnimatorListener(ImageButton button, boolean isOpening) {
            this.button = button;
            this.isOpening = isOpening;
        }

        @Override
        public void onAnimationStart(Animator animator) {
            if (isOpening) {
                button.setEnabled(true);
                button.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onAnimationEnd(Animator animator) {
            if (!isOpening) {
                button.setEnabled(false);
                button.setVisibility(View.INVISIBLE);
            }
        }

        @Override
        public void onAnimationCancel(Animator animator) {

        }

        @Override
        public void onAnimationRepeat(Animator animator) {

        }
    }
}
