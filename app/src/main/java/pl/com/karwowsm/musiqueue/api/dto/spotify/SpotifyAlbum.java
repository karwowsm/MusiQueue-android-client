package pl.com.karwowsm.musiqueue.api.dto.spotify;

import java.util.List;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;

@Value
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class SpotifyAlbum extends SpotifyContent {

    private final List<Image> images;

    @Override
    public String getImageUrl() {
        return images.get(0).getUrl();
    }
}
