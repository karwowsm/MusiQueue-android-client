package pl.com.karwowsm.musiqueue.api.dto.youtube;

import lombok.Value;

@Value
public class YouTubeContent {

    private final Id id;

    private final Snippet snippet;

    @Value
    public static class Id {

        private final String videoId;
    }
}
