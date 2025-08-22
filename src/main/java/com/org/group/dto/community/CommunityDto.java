package com.org.group.dto.community;

import com.org.group.model.project.TeamMember;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import java.util.List;


@Getter
@Setter
public class CommunityDto {

    @NotBlank(message = "fullName is required")
    private String fullName;
    @NotBlank(message = "profession is required")
    private String profession;
    @NotBlank(message = "email is required")
    private String email;
    @NotBlank(message = "phone is required")
    private String phone;
    @NotBlank(message = "linkedIn is required")
    private String linkedIn;
    @NotBlank(message = "projectName is required")
    private String projectName;
    @NotBlank(message = "category is required")
    private String category;
    @NotBlank(message = "location is required")
    private String location;
    @NotBlank(message = "description is required")
    private String description;
    @NotBlank
    private List<TeamMember> team;
}
