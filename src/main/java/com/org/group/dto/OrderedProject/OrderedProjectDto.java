package com.org.group.dto.OrderedProject;

import com.org.group.dto.LaunchProject.AnalyticStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OrderedProjectDto {
    private String clientName;
    private String companyName;
    private String professionalStatus;
    private String email;
    private String phone;
    private String linkedIn;
    private String projectTitle;
    private String projectType;
    private String projectDescription;
    private String targetAudience;
    private String references;
    private String projectLocation;
    private String specialityOfProject;
    private String doYouHaveSponsorship;
    private String sponsorName;
    private String doYouNeedIntellectualProject;
    private String doYouNeedBusinessPlan;
    private String businessIdea;
    private MultipartFile businessIdeaDocument;
    private MultipartFile businessPlanDocument;

}
