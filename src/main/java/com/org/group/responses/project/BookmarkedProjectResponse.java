package com.org.group.responses.project;


import com.org.group.dto.LaunchProject.AnalyticStatus;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookmarkedProjectResponse {
    private UUID projectId;
    private String clientName;
    private AnalyticStatus analyticStatus;
    private String projectDescription;
    private String projectUrl;
    private String category;
    private String projectName;
    private LocalDateTime bookmarkedDate;
    private String projectPurpose;
}
