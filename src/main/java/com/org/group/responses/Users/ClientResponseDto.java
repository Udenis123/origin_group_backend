package com.org.group.responses.Users;

import com.org.group.model.UserSubscription;
import com.org.group.role.Role;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@Builder
public class ClientResponseDto {


    private UUID id;
    public String name;
    private String nationalId;
    private String gender;
    private String nationality;
    private String professional;
    private String email;
    private String phone;
    private boolean enabled;
    private String verificationCode;
    private LocalDateTime codeExpiryAt;
    private Set<Role> roles;
    private Boolean subscribed;
    private boolean isActive;
    private String photoUrl;
    private String tempEmail;
    private String currentSubscription;

}
