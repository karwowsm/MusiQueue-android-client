package pl.com.karwowsm.musiqueue.ui.view;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.ViewGroup;

import androidx.annotation.DrawableRes;
import androidx.appcompat.widget.AppCompatImageView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;

import lombok.Setter;
import pl.com.karwowsm.musiqueue.R;

public class CustomNetworkImageView extends AppCompatImageView {

    private String url;

    @Setter
    private int defaultImageResId;

    private boolean isResImageSet;

    private ImageLoader imageLoader;

    private ImageLoader.ImageContainer imageContainer;

    public CustomNetworkImageView(Context context) {
        this(context, null);
    }

    public CustomNetworkImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomNetworkImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setImageUrl(String url, ImageLoader imageLoader) {
        this.url = url;
        this.imageLoader = imageLoader;
        loadImageIfNecessary(false);
    }

    @Override
    public void setImageResource(@DrawableRes int resId) {
        super.setImageResource(resId);
        isResImageSet = true;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (!isResImageSet) {
            loadImageIfNecessary(true);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        if (imageContainer != null) {
            imageContainer.cancelRequest();
            setImageBitmap(null);
            imageContainer = null;
        }
        super.onDetachedFromWindow();
    }

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        invalidate();
    }

    private void loadImageIfNecessary(final boolean isInLayoutPass) {
        int width = getWidth();
        int height = getHeight();
        ScaleType scaleType = getScaleType();

        boolean wrapWidth = false, wrapHeight = false;
        if (getLayoutParams() != null) {
            wrapWidth = getLayoutParams().width == ViewGroup.LayoutParams.WRAP_CONTENT;
            wrapHeight = getLayoutParams().height == ViewGroup.LayoutParams.WRAP_CONTENT;
        }

        boolean isFullyWrapContent = wrapWidth && wrapHeight;
        if (width == 0 && height == 0 && !isFullyWrapContent) {
            return;
        }

        if (TextUtils.isEmpty(url)) {
            if (imageContainer != null) {
                imageContainer.cancelRequest();
                imageContainer = null;
            }
            setDefaultImageOrNull();
            return;
        }

        if (imageContainer != null && imageContainer.getRequestUrl() != null) {
            if (imageContainer.getRequestUrl().equals(url)) {
                if (isResImageSet) {
                    isResImageSet = false;
                } else {
                    return;
                }
            } else {
                imageContainer.cancelRequest();
                setDefaultImageOrNull();
            }
        }

        int maxWidth = wrapWidth ? 0 : width;
        int maxHeight = wrapHeight ? 0 : height;

        imageContainer = imageLoader.get(url,
            new ImageLoader.ImageListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    setImageResource(R.drawable.ic_error);
                }

                @Override
                public void onResponse(final ImageLoader.ImageContainer response, boolean isImmediate) {
                    if (isImmediate && isInLayoutPass) {
                        post(() -> onResponse(response, false));
                        return;
                    }

                    if (response.getBitmap() != null) {
                        setImageBitmap(response.getBitmap());
                    } else if (defaultImageResId != 0) {
                        setImageResource(defaultImageResId);
                    }
                }
            }, maxWidth, maxHeight, scaleType);
    }

    private void setDefaultImageOrNull() {
        if (defaultImageResId != 0) {
            setImageResource(defaultImageResId);
        } else {
            setImageBitmap(null);
        }
    }
}
