package pl.com.karwowsm.musiqueue.api.error;

import java.util.Date;
import java.util.List;

import lombok.Value;

@Value
public class ErrorResponse {

    private final Date timestamp;

    private final Integer status;

    private final String error;

    private final List<Error> errors;

    private final String message;

    private final String path;

    @Value
    public static class Error {

        private final String field;

        private final String defaultMessage;
    }

    public interface Listener {

        void onResponse(ErrorResponse error);
    }
}
