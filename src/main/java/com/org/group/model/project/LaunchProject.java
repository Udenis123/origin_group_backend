package com.org.group.model.project;

import com.org.group.dto.LaunchProject.AnalyticStatus;
import com.org.group.model.Users;
import com.org.group.model.analyzer.AnalyticProject;
import com.org.group.model.analyzer.Assignment;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "launch_project")
@Getter
@Setter
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class LaunchProject {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false) // Ensure one user has only one subscription
    private Users user;
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Bookmark> bookmarks;
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Assignment> assignments;
    @OneToOne(mappedBy = "launchProject")
    private AnalyticProject analyticProject;


    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "project_id", updatable = false, nullable = false)
    private UUID projectId;
    @Column(nullable = false)
    private String clientName;
    @Column(nullable = false)
    private String professionalStatus;
    @Column(nullable = false)
    private String email;
    @Column(nullable = false)
    private String phone;
    @Column(nullable = false)
    private String linkedIn;

    @Column(unique = true)
    private String projectName;
    private String category;
    private String description;
    private String projectLocation;
    private String projectStatus;
    @Column(nullable = false)
    private  String projectPurpose;

    //if projectStatus is prototype fill link bellow if no
    private String prototypeLink;
    //if projectStatus is operating
    private int numberOfEmp;
    private Double monthlyIncome;
    private String website;
    private String incomeStatementUrl;
    private String cashFlowUrl;
    private String balanceSheetUrl;


    private String specialityOfProject;

     //sponsorship
    //have sponsor
    private String haveSponsorQ;
    private String sponsorName;

    //need sponsor of project if yes specify if is origin
    private String needSponsorQ;
    //yes or no while need origin group us your sponsor
    private String needOrgQ;

    //selling project if yes specify amount
    private String doSellProjectQ;
    private Double projectAmount;
//Intellectual property
    //if yes for Q you specify it bellow
    private String intellectualProjectQ;

    //yes or no
    private String wantOriginToBusinessPlanQ;
    private String businessIdea;
    @Column(nullable = false)
    private LocalDateTime submittedOn;
    private LocalDateTime updatedOn;
    private String projectType;


    private String projectPhotoUrl;
    private String pitchingVideoUrl;
    private String businessPlanUrl;
    private String businessIdeaDocumentUrl;
    private AnalyticStatus status;

    //extra field
    private int countBookmark;
    private int countAssignment;
    private long views;
    private long interaction;
    private long interestedInvestors;






}
