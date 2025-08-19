package com.org.group.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AnalyzerInfoDto {
    private UUID id;
    private String name;
    private String email;
    private String phone;
    private String expertise;
    private String profileUrl;
    private String nationality;
    private String gender;
    private String nationalId;
    private boolean enabled;
    private List<AssignedProjectDto> assignedProjects;
}