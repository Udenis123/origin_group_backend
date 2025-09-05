package com.org.group.dto.community;

import com.org.group.dto.LaunchProject.AnalyticStatus;
import com.org.group.model.JoinStatus;
import com.org.group.model.project.TeamMember;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JoinedProjectWithDetailsDto {
    // Community Project Details
    private UUID projectId;
    private String fullName;
    private String profession;
    private String email;
    private String phone;
    private String linkedIn;
    private String projectPhoto;
    private String projectName;
    private String category;
    private String location;
    private String description;
    private AnalyticStatus projectStatus;
    private String reason;
    private LocalDateTime projectCreatedAt;
    private LocalDateTime projectUpdatedOn;
    private List<TeamMember> team;
    private UUID projectOwnerId;
    
    // Join Details
    private UUID joinId;
    private String joinDescription;
    private JoinStatus joinStatus;
    private String joinedTeam;
    private LocalDateTime joinCreatedAt;
}
