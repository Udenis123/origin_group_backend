package com.org.group.responses;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ProfileResponse {
    private String  plan;
    private String status;
    private String fullName;
    private String email;
    private boolean emailStatus;
    private String phone ;
    private String idNumber;
    private String gender;
    private String nationality;
    private String profession;
    private String profilePicture;
}
