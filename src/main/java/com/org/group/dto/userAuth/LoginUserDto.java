package com.org.group.dto.userAuth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginUserDto {

    @NotBlank(message = "specify your email")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "specify your password")
    private String password;
}
