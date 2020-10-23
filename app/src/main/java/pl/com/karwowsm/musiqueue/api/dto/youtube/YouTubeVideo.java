package pl.com.karwowsm.musiqueue.api.dto.youtube;

import java.time.Duration;

import lombok.Value;

@Value
public class YouTubeVideo {

    private final String id;

    private final Snippet snippet;

    private final ContentDetails contentDetails;

    @Value
    public static class ContentDetails {

        private final Duration duration;
    }
}
