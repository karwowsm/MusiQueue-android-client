package pl.com.karwowsm.musiqueue.ui.util;

import lombok.experimental.UtilityClass;
import pl.com.karwowsm.musiqueue.R;
import pl.com.karwowsm.musiqueue.api.dto.Track;

@UtilityClass
public final class TrackUtils {

    public static int getImageResId(Track.Source source) {
        switch (source) {
            case UPLOADED:
                return R.drawable.web_hi_res_512;
            case YOUTUBE:
                return R.drawable.ic_youtube;
            case SPOTIFY:
                return R.drawable.ic_spotify;
            case SOUNDCLOUD:
                return R.drawable.ic_soundcloud;
        }
        return 0;
    }

    public static int getColor(Track.Source source) {
        switch (source) {
            case UPLOADED:
                return R.color.colorAccent;
            case YOUTUBE:
                return R.color.youTube;
            case SPOTIFY:
                return R.color.spotify;
            case SOUNDCLOUD:
                return R.color.soundCloud;
        }
        return 0;
    }
}
