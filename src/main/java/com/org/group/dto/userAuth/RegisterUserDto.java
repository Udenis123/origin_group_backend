package com.org.group.dto.userAuth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterUserDto {

    @NotBlank(message = "Name is required")
    @Size(min = 10, max = 50, message = "Name must be between 10 and 50 characters")
    private String name;

    @NotBlank(message = "National ID is required")
    @Size(min = 16, max = 16, message = "National ID must be exactly 16 digits")
    @Pattern(regexp = "^[0-9]{16}$", message = "National ID must contain only numbers")
    private String nationalId;

    @NotBlank(message = "Gender is required")
    private String gender;

    @NotBlank(message = "Nationality is required")
    @Size(min = 5, max = 50, message = "Nationality must be between 2 and 50 characters")
    private String nationality;

    @NotBlank(message = "Profession is required")
    @Size(min = 5, max = 50, message = "Profession must be between 5 and 50 characters")
    private String professional;

    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    private String email;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[0-9]{9,15}$", message = "Phone number must be between 9 and 15 digits")
    private String phone;

    @NotBlank(message = "Password is required")
    @Pattern(
        regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,20}$",
        message = "Password must be 8-20 characters long and contain at least one uppercase letter, " +
                "one lowercase letter, one number, and one special character"
    )
    private String password;
}
