package com.org.group.dto.admin;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
public class UpdateAnalyzerDto {
        @Size(min = 8,max = 100, message = "name must be  between 8-100 characters")
        private String name;


        @Email(message = "Please provide a valid email address")
        private String email;


        @Pattern(regexp = "^[0-9]{9,15}$", message = "Phone number must be between 9 and 15 digits")
        private String phone;


        private String expertise;


        @Size(min = 5, max = 50, message = "Nationality must be between 5 and 50 characters")
        private String nationality;


        private String gender;



        @Size(min = 16, max = 16, message = "National ID must be exactly 16 digits")
        @Pattern(regexp = "^[0-9]{16}$", message = "National ID must contain only numbers")
        private String nationalId;

        private String password;

}
