package pl.com.karwowsm.musiqueue.api.request;

import lombok.Builder;
import lombok.ToString;
import lombok.Value;
import lombok.experimental.FieldNameConstants;

@Value
@Builder
@FieldNameConstants
public final class UserAccountCreateRequest {

    private final String username;

    @ToString.Exclude
    private final String password;

    @ToString.Exclude
    private final String passwordConfirmation;

    private final String email;
}
