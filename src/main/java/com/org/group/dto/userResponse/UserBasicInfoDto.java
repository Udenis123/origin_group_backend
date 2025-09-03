package com.org.group.dto.userResponse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserBasicInfoDto {
    private UUID id;
    private String name;
    private String email;
    private String phone;
    private String professional;
    private String photoUrl;
    private String nationalId;
    private boolean isActive;
    private boolean isSubscribed;
}
