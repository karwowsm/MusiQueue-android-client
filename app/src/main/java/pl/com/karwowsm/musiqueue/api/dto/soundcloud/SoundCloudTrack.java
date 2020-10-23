package pl.com.karwowsm.musiqueue.api.dto.soundcloud;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import lombok.Value;

@Value
public class SoundCloudTrack {

    private final int full_duration;

    private final Media media;

    private final String title;

    private final int duration;

    private final String artwork_url;

    private final Policy policy;

    private final User user;

    @Value
    public static class Media {

        private final List<Transcoding> transcodings;

        @Value
        public static class Transcoding {

            private final String url;

            private final String preset;

            private final Format format;

            private final String quality;

            @Value
            public static class Format {

                private final Protocol protocol;

                private final String mime_type;

                public enum Protocol {

                    @SerializedName("hls") HLS,

                    @SerializedName("progressive") PROGRESSIVE
                }
            }
        }
    }

    public enum Policy {

        @SerializedName("ALLOW") ALLOW,

        @SerializedName("SNIP") SNIP
    }

    @Value
    public static class User {

        private final String username;
    }
}
