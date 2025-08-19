package com.org.group.dto.admin;

import com.org.group.dto.LaunchProject.AnalyticStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AssignedProjectDto {
    private UUID projectId;
    private String projectName;
    private String description;
    private AnalyticStatus status;
    private String projectPhotoUrl;
    // Add other relevant project fields if needed
}
