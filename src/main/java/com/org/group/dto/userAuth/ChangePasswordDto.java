package com.org.group.dto.userAuth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class ChangePasswordDto {
    private UUID userId;

    @NotBlank(message = "Password is required")
    private String oldPassword;
    @NotBlank(message = "Password is required")
    @Pattern(
            regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,20}$",
            message = "Password must be 8-20 characters long and contain at least one uppercase letter, " +
                    "one lowercase letter, one number, and one special character"
    )
    private String newPassword;
}
