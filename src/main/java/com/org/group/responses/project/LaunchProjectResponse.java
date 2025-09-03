package com.org.group.responses.project;

import com.org.group.dto.LaunchProject.AnalyticStatus;
import com.org.group.model.analyzer.AnalyticProject;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LaunchProjectResponse {
    private String balanceSheetUrl;
    private UUID projectId;
    private String businessIdea;
    private String businessPlanUrl;
    private String businessIdeaDocumentUrl;
    private String clientName;
    private String category;
    private String description;
    private String cashFlowUrl;
    private AnalyticStatus status;
    private String professionalStatus;
    private String projectName;
    private String haveSponsorQ;
    private String projectPurpose;
    private String projectLocation;
    private String projectPhotoUrl;
    private String doSellProjectQ;
    private LocalDateTime submittedOn;
    private Double projectAmount;
    private String needOrgQ;
    private String pitchingVideoUrl;
    private String sponsorName;
    private String website;
    private String incomeStatementUrl;
    private String prototypeLink;
    private String projectStatus;
    private LocalDateTime updatedOn;
    private String linkedIn;
    private Double monthlyIncome;
    private String specialityOfProject;
    private String phone;
    private String needSponsorQ;
    private Integer numberOfEmp;
    private String wantOriginToBusinessPlanQ;
    private String intellectualProjectQ;
    private String email;
    private String feedback;

    private int countBookmark;
    private int countAssignment;
    private long views;
    private long interaction;

}
