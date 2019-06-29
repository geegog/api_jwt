package com.icefire.api.user.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.icefire.api.common.rest.ResourceSupport;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserDTO extends ResourceSupport {

    private Long _id;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    private String username;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String publicKey;

    private LocalDateTime created;

    private LocalDateTime updated;
}
