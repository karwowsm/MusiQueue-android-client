package pl.com.karwowsm.musiqueue.api.dto;

import lombok.Value;

@Value
public class Token {

    private final String access_token;

    private final String token_type;

    private final long expires_in;
}
