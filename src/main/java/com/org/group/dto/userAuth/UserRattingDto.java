package com.org.group.dto.userAuth;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Setter
@Getter
public class UserRattingDto {
    private int stars;
    private String message;
    private UUID userId;
}
