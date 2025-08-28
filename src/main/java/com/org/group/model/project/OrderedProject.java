package com.org.group.model.project;

import com.org.group.dto.LaunchProject.AnalyticStatus;
import com.org.group.model.Users;
import io.swagger.v3.core.util.Json;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Table(name = "ordered_project")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class OrderedProject {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "project_id", updatable = false, nullable = false)
    private UUID projectId;

    private String clientName;
    private String companyName;
    private String professionalStatus;
    private String Email;
    private String phone;
    private String linkedIn;
    private String ProjectTitle;
    private String projectType;
    private String projectDescription;
    private String targetAudience;
    private String references;
    private String projectLocation;
    private String SpecialityOfProject;

    //yes or no
    private String doYouHaveSponsorship;
    private String SponsorName;

   //yes or no
    private String doYouNeedIntellectualProject;
    private String businessPlanUrl;
    //yes or no
    private String doYouNeedBusinessPlan;

    private String BusinessIdea;
    private String businessIdeaDocumentUrl;

    private String reasons;


    private AnalyticStatus status;



}
