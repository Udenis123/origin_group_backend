package com.org.group.responses.project;

import com.org.group.dto.LaunchProject.AnalyticStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Builder
public class HomeProjectResponse {
    private UUID projectId;
    private String projectName;
    private String projectDescription;
    private String linkedInUrl;
    private String projectUrl;
    private AnalyticStatus analyticStatus;
    private String clientName;
    private String category;
    private String projectPurpose;
    private Double projectFinancialCategory;
    private String projectLocation;
    private String projectType;
}
