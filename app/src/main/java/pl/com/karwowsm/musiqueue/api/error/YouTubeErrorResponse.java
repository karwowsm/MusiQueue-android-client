package pl.com.karwowsm.musiqueue.api.error;

import java.util.List;

import lombok.Value;

@Value
public class YouTubeErrorResponse {

    private final Error error;

    @Value
    public static class Error {

        private final Integer code;

        private final String message;

        private final List<Item> errors;

        private final String status;

        @Value
        public static class Item {

            private final String message;

            private final String domain;

            private final String reason;

            private final String location;

            private final String locationType;
        }
    }

    public interface Listener {

        void onResponse(YouTubeErrorResponse error);
    }
}
