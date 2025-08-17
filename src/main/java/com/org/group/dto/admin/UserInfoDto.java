package com.org.group.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserInfoDto {
    private UUID id;
    private String name;
    private String email;
    private String phone;
    private String nationalId;
    private String gender;
    private String nationality;
    private String professional;
    private String photoUrl;
    private boolean enabled;
    private boolean subscribed;
    private boolean isActive;
}
