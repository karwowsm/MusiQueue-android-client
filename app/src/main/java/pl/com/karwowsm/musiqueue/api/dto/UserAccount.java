package pl.com.karwowsm.musiqueue.api.dto;

import java.io.Serializable;
import java.util.UUID;

import lombok.EqualsAndHashCode;
import lombok.Value;

@Value
@EqualsAndHashCode(of = "id")
public class UserAccount implements Serializable {

    private final UUID id;

    private final String username;

    private final String email;
}
