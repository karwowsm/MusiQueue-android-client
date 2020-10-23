package pl.com.karwowsm.musiqueue.api;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

public class TokenHolder {

    @Getter(AccessLevel.PROTECTED)
    @Setter
    private static String token;
}
