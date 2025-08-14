package com.org.group.dto.LaunchProject;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LaunchProjectDto {
    @NotBlank(message = "Client name is required")
    private String clientName;

    @NotBlank(message = "Professional status is required")
    private String professionalStatus;

    @Email(message = "Invalid email address")
    private String email;

    @NotBlank(message = "Phone number is required")
    private String phone;


    private String linkedIn;

    @NotBlank(message = "Project name is required")
    private String projectName;

    @NotBlank(message = "Category is required")
    private String category;

    @Size(min = 20, max = 200, message = "Project description must be between 20 and 200 characters")
    private String description;

    @NotBlank(message = "Project location is required")
    private String projectLocation;

    @NotBlank(message = "project purpose is required")
    private String projectPurpose;

    @NotBlank(message = "Project status is required")
    private String projectStatus;

    //while project status is prototype
    private String prototypeLink;

    //while project status is Operating
    private int numberOfEmp;
    private Double monthlyIncome;
    private String websiteLink;


    @NotBlank(message = "Speciality of project is required")
    private String specialityOfProject;

    // Sponsorship

    private String haveSponsorQ;

    private String sponsorName;


    private String needSponsorQ;


    private String needOrgQ;


    private String doSellProjectQ;

    private Double projectAmount;


    private String intellectualProjectQ;


    private String wantOriginToBusinessPlanQ;

    private String businessIdea;


}
