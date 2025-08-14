package com.org.group.responses;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;


import java.util.UUID;

@Getter
@Setter
@Builder
public class LoginResponse {
    private String token;
    private long expiresIn;
    private UUID id;
}