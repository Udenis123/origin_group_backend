package com.org.group.dto.community;

import com.org.group.dto.LaunchProject.AnalyticStatus;
import com.org.group.model.project.TeamMember;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommunityResponseDto {
    private UUID id;
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
    private AnalyticStatus status;
    private String reason;
    private List<TeamMember> team;
    private UUID userId; // Only include the user ID, not the full user object
}
