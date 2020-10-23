package pl.com.karwowsm.musiqueue.api.request;

import lombok.Builder;
import lombok.ToString;
import lombok.Value;

@Value
@Builder
public final class TokenCreateRequest {

    private final String username;

    @ToString.Exclude
    private final String password;
}
