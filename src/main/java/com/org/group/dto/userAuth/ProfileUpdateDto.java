package com.org.group.dto.userAuth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class ProfileUpdateDto {


    private UUID id;

    @NotBlank(message = "Profession is required")
    @Size(min = 5, max = 50, message = "Profession must be between 5 and 50 characters")
    private String professional;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[0-9]{9,15}$", message = "Phone number must be between 9 and 15 digits")
    private String phone;

    @NotBlank(message = "Name is required")
    @Size(min = 10, max = 50, message = "Name must be between 10 and 50 characters")
    private String name;

    private String nationality;
    private String profession;
    private String nationalId;
    private String gender;
    private String phoneNumber;

}
