package com.org.group.services.LaunchProject;

import com.org.group.dto.LaunchProject.LaunchProjectDto;
import com.org.group.dto.LaunchProject.AnalyticStatus;
import com.org.group.dto.analytics.AnalyticsResponseDto;
import com.org.group.dto.analytics.UserAnalyticsResponse;
import com.org.group.model.analyzer.AnalyticProject;
import com.org.group.model.analyzer.AnalyticsFeedback;
import com.org.group.model.project.LaunchProject;
import com.org.group.model.Users;
import com.org.group.repository.analytics.AnalyticProjectRepository;
import com.org.group.repository.analytics.AnalyticsFeedbackRepository;
import com.org.group.repository.project.LaunchProjectRepository;
import com.org.group.repository.UserRepository;
import com.org.group.responses.project.HomeProjectResponse;
import com.org.group.responses.project.LaunchProjectResponse;
import com.org.group.responses.project.MyProjectResponse;
import com.org.group.services.UploadFileServices.CloudinaryService;
import com.org.group.services.UploadFileServices.FileStorageService;
import com.org.group.services.emailAndJwt.PlanFilterServices;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.Optional;

@Service
public class LaunchProjectServices {

    private final LaunchProjectRepository launchProjectRepository;
    private final CloudinaryService cloudinaryService;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;
    private final PlanFilterServices planFilterServices;
    private final AnalyticProjectRepository analyticsRepository;
    private final AnalyticsFeedbackRepository feedbackRepository;


    public LaunchProjectServices(LaunchProjectRepository launchProjectRepository, CloudinaryService cloudinaryService, UserRepository userRepository, FileStorageService fileStorageService, PlanFilterServices planFilterServices, AnalyticProjectRepository analyticsRepository, AnalyticsFeedbackRepository feedbackRepository) {
        this.launchProjectRepository = launchProjectRepository;
        this.cloudinaryService = cloudinaryService;

        this.userRepository = userRepository;
        this.fileStorageService = fileStorageService;
        this.planFilterServices = planFilterServices;
        this.analyticsRepository = analyticsRepository;
        this.feedbackRepository = feedbackRepository;
    }

    public void saveProjectWithFiles(
            UUID userId,
            LaunchProjectDto projectDto,
            MultipartFile businessPlan,
            MultipartFile businessIdeaDocument,
            MultipartFile projectPhoto,
            MultipartFile cashFlow,
            MultipartFile incomeStatement,
            MultipartFile balanceSheet,
            MultipartFile pitchingVideo
    ) throws IOException {

        String businessPlanUrl = null;
        String businessIdeaDocumentUrl = null;
        String projectPhotoUrl = null;
        String pitchingVideoUrl = null;

        String incomeStatementUrl = null;
        String balanceSheetUrl = null;
        String cashFlowUrl = null;

        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));


        try {
            if (businessPlan != null && !businessPlan.isEmpty()) {
                businessPlanUrl = cloudinaryService.uploadProjectPlan(businessPlan);
            }
        } catch (Exception e) {
            throw new IOException("Failed to upload business plan", e);
        }

        try {
            if (businessIdeaDocument != null && !businessIdeaDocument.isEmpty()) {
                businessIdeaDocumentUrl = cloudinaryService.uploadProjectIdea(businessIdeaDocument);
            }
        } catch (Exception e) {
            throw new IOException("Failed to upload business idea document", e);
        }

        try {
            if (projectPhoto != null && !projectPhoto.isEmpty()) {
                projectPhotoUrl = cloudinaryService.uploadProjectPhoto(projectPhoto);
            }
        } catch (Exception e) {
            throw new IOException("Failed to upload project photo", e);
        }
        try {
            if (incomeStatement != null && !incomeStatement.isEmpty()) {
                incomeStatementUrl = cloudinaryService.uploadProjectPlan(incomeStatement);
            }
        } catch (Exception e) {
            throw new IOException("Failed to upload income statement", e);
        }
        try {
            if (cashFlow != null && !cashFlow.isEmpty()) {
                cashFlowUrl = cloudinaryService.uploadProjectPlan(cashFlow);
            }
        } catch (Exception e) {
            throw new IOException("Failed to upload cash flow", e);
        }
        try {
            if (balanceSheet != null && !balanceSheet.isEmpty()) {
                balanceSheetUrl = cloudinaryService.uploadProjectPlan(balanceSheet);
            }
        } catch (Exception e) {
            throw new IOException("Failed to upload balance sheet", e);
        }

        try {
            if (pitchingVideo != null && !pitchingVideo.isEmpty()) {
                pitchingVideoUrl = cloudinaryService.uploadProjectVideo(pitchingVideo);
            }
        } catch (Exception e) {
            throw new IOException("Failed to upload pitching video", e);
        }

        LaunchProject newProject = LaunchProject.builder()
                .user(user)
                .clientName(projectDto.getClientName())
                .professionalStatus(projectDto.getProfessionalStatus())
                .email(projectDto.getEmail())
                .phone(projectDto.getPhone())
                .linkedIn(projectDto.getLinkedIn())
                .projectName(projectDto.getProjectName())
                .category(projectDto.getCategory())
                .projectPurpose(projectDto.getProjectPurpose())
                .description(projectDto.getDescription())
                .prototypeLink(projectDto.getPrototypeLink())
                .website(projectDto.getWebsiteLink())
                .monthlyIncome(projectDto.getMonthlyIncome())
                .projectLocation(projectDto.getProjectLocation())
                .projectStatus(projectDto.getProjectStatus())
                .specialityOfProject(projectDto.getSpecialityOfProject())
                .haveSponsorQ(projectDto.getHaveSponsorQ())
                .sponsorName(projectDto.getSponsorName())
                .needSponsorQ(projectDto.getNeedSponsorQ())
                .needOrgQ(projectDto.getNeedOrgQ())
                .doSellProjectQ(projectDto.getDoSellProjectQ())
                .projectAmount(projectDto.getProjectAmount())
                .intellectualProjectQ(projectDto.getIntellectualProjectQ())
                .wantOriginToBusinessPlanQ(projectDto.getWantOriginToBusinessPlanQ())
                .businessIdea(projectDto.getBusinessIdea())
                .projectPhotoUrl(projectPhotoUrl)
                .pitchingVideoUrl(pitchingVideoUrl)
                .businessPlanUrl(businessPlanUrl)
                .projectType("launched")
                .businessIdeaDocumentUrl(businessIdeaDocumentUrl)
                .incomeStatementUrl(incomeStatementUrl)
                .cashFlowUrl(cashFlowUrl)
                .balanceSheetUrl(balanceSheetUrl)
                .status(AnalyticStatus.PENDING)
                .countBookmark(0)
                .countAssignment(0)
                .submittedOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();

        launchProjectRepository.save(newProject);
    }

    public void updateProjectWithFiles(
            UUID projectId,
            LaunchProjectDto projectDto,
            MultipartFile businessPlan,
            MultipartFile businessIdeaDocument,
            MultipartFile projectPhoto,
            MultipartFile pitchingVideo,
            MultipartFile incomeStatement,
            MultipartFile cashFlow,
            MultipartFile balanceSheet
    ) throws IOException {

        LaunchProject project = launchProjectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        if(project.getStatus() == AnalyticStatus.APPROVED) {
            throw new RuntimeException("Your not Allowed to update this project");
        }


        // Delete and update each file if a new one is provided
     // Business Plan
        if (businessPlan != null && !businessPlan.isEmpty()) {
            if (project.getBusinessPlanUrl() != null && !project.getBusinessPlanUrl().isEmpty()) {
                cloudinaryService.deleteFile(project.getBusinessPlanUrl());
            }
            String url = cloudinaryService.uploadProjectPlan(businessPlan);
            project.setBusinessPlanUrl(url);
        } else if (businessPlan == null && project.getBusinessPlanUrl() != null && !project.getBusinessPlanUrl().isEmpty()) {
            cloudinaryService.deleteFile(project.getBusinessPlanUrl());
            project.setBusinessPlanUrl(null);
        }

        // Business Idea Document
        if (businessIdeaDocument != null && !businessIdeaDocument.isEmpty()) {
            if (project.getBusinessIdeaDocumentUrl() != null && !project.getBusinessIdeaDocumentUrl().isEmpty()) {
                cloudinaryService.deleteFile(project.getBusinessIdeaDocumentUrl());
            }
            String url = cloudinaryService.uploadProjectIdea(businessIdeaDocument);
            project.setBusinessIdeaDocumentUrl(url);
        } else if (businessIdeaDocument == null && project.getBusinessIdeaDocumentUrl() != null && !project.getBusinessIdeaDocumentUrl().isEmpty()) {
            cloudinaryService.deleteFile(project.getBusinessIdeaDocumentUrl());
            project.setBusinessIdeaDocumentUrl(null);
        }

        // Project Photo
        if (projectPhoto != null && !projectPhoto.isEmpty()) {
            if (project.getProjectPhotoUrl() != null && !project.getProjectPhotoUrl().isEmpty()) {
                cloudinaryService.deleteFile(project.getProjectPhotoUrl());
            }
            String url = cloudinaryService.uploadProjectPhoto(projectPhoto);
            project.setProjectPhotoUrl(url);
        } else if (projectPhoto == null && project.getProjectPhotoUrl() != null && !project.getProjectPhotoUrl().isEmpty()) {
            cloudinaryService.deleteFile(project.getProjectPhotoUrl());
            project.setProjectPhotoUrl(null);
        }

        // Pitching Video
        if (pitchingVideo != null && !pitchingVideo.isEmpty()) {
            if (project.getPitchingVideoUrl() != null && !project.getPitchingVideoUrl().isEmpty()) {
                cloudinaryService.deleteFile(project.getPitchingVideoUrl());
            }
            String url = cloudinaryService.uploadProjectVideo(pitchingVideo);
            project.setPitchingVideoUrl(url);
        } else if (pitchingVideo == null && project.getPitchingVideoUrl() != null && !project.getPitchingVideoUrl().isEmpty()) {
            cloudinaryService.deleteFile(project.getPitchingVideoUrl());
            project.setPitchingVideoUrl(null);
        }

        // Income Statement
        if (incomeStatement != null && !incomeStatement.isEmpty()) {
            if (project.getIncomeStatementUrl() != null && !project.getIncomeStatementUrl().isEmpty()) {
                cloudinaryService.deleteFile(project.getIncomeStatementUrl());
            }
            String url = cloudinaryService.uploadProjectPlan(incomeStatement);
            project.setIncomeStatementUrl(url);
        } else if (incomeStatement == null && project.getIncomeStatementUrl() != null && !project.getIncomeStatementUrl().isEmpty()) {
            cloudinaryService.deleteFile(project.getIncomeStatementUrl());
            project.setIncomeStatementUrl(null);
        }


        if (cashFlow != null && !cashFlow.isEmpty()) {
            if (project.getCashFlowUrl() != null && !project.getCashFlowUrl().isEmpty()) {
                cloudinaryService.deleteFile(project.getCashFlowUrl());
            }
            String url = cloudinaryService.uploadProjectPlan(cashFlow);
            project.setCashFlowUrl(url);
        } else if (cashFlow == null && project.getCashFlowUrl() != null && !project.getCashFlowUrl().isEmpty()) {
            cloudinaryService.deleteFile(project.getCashFlowUrl());
            project.setCashFlowUrl(null);
        }

        if (balanceSheet != null && !balanceSheet.isEmpty()) {
            if (project.getBalanceSheetUrl() != null && !project.getBalanceSheetUrl().isEmpty()) {
                cloudinaryService.deleteFile(project.getBalanceSheetUrl());
            }
            String url = cloudinaryService.uploadProjectPlan(balanceSheet);
            project.setBalanceSheetUrl(url);
        } else if (balanceSheet == null && project.getBalanceSheetUrl() != null && !project.getBalanceSheetUrl().isEmpty()) {
            cloudinaryService.deleteFile(project.getBalanceSheetUrl());
            project.setBalanceSheetUrl(null);
        }

        project.setClientName(projectDto.getClientName());
        project.setProfessionalStatus(projectDto.getProfessionalStatus());
        project.setEmail(projectDto.getEmail());
        project.setPhone(projectDto.getPhone());
        project.setLinkedIn(projectDto.getLinkedIn());
        project.setProjectName(projectDto.getProjectName());
        project.setCategory(projectDto.getCategory());
        project.setProjectPurpose(projectDto.getProjectPurpose());
        project.setDescription(projectDto.getDescription());
        project.setProjectLocation(projectDto.getProjectLocation());
        project.setProjectStatus(projectDto.getProjectStatus());
        project.setSpecialityOfProject(projectDto.getSpecialityOfProject());
        project.setHaveSponsorQ(projectDto.getHaveSponsorQ());
        project.setSponsorName(projectDto.getSponsorName());
        project.setNeedSponsorQ(projectDto.getNeedSponsorQ());
        project.setNeedOrgQ(projectDto.getNeedOrgQ());
        project.setPrototypeLink(projectDto.getPrototypeLink());
        project.setWebsite(projectDto.getWebsiteLink());
        project.setMonthlyIncome(projectDto.getMonthlyIncome());
        project.setDoSellProjectQ(projectDto.getDoSellProjectQ());
        project.setProjectAmount(projectDto.getProjectAmount());
        project.setIntellectualProjectQ(projectDto.getIntellectualProjectQ());
        project.setWantOriginToBusinessPlanQ(projectDto.getWantOriginToBusinessPlanQ());
        project.setBusinessIdea(projectDto.getBusinessIdea());
        project.setUpdatedOn(LocalDateTime.now());

        launchProjectRepository.save(project);
        
        if(project.getStatus() == AnalyticStatus.DECLINED) {
            List<AnalyticsFeedback> existingFeedback = feedbackRepository.findByProjectId(projectId);
            if (!existingFeedback.isEmpty()) {
                feedbackRepository.deleteAll(existingFeedback);
            }
            
            project.setStatus(AnalyticStatus.PENDING);
            launchProjectRepository.save(project);
        }
    }




    public List<LaunchProjectResponse> getProjectsByUserId(UUID userId) {
        List<LaunchProject> getProjects = launchProjectRepository.findByUserId(userId);


        return getProjects.stream().map(project -> LaunchProjectResponse.builder()
                .balanceSheetUrl(project.getBalanceSheetUrl())
                .projectId(project.getProjectId())
                .businessIdea(project.getBusinessIdea())
                .businessPlanUrl(project.getBusinessPlanUrl())
                .businessIdeaDocumentUrl(project.getBusinessIdeaDocumentUrl())
                .clientName(project.getClientName())
                .category(project.getCategory())
                .description(project.getDescription())
                .cashFlowUrl(project.getCashFlowUrl())
                .status(project.getStatus())
                .professionalStatus(project.getProfessionalStatus())
                .projectName(project.getProjectName())
                .haveSponsorQ(project.getHaveSponsorQ())
                .projectPurpose(project.getProjectPurpose())
                .projectLocation(project.getProjectLocation())
                .projectPhotoUrl(project.getProjectPhotoUrl())
                .doSellProjectQ(project.getDoSellProjectQ())
                .submittedOn(project.getSubmittedOn())
                .projectAmount(project.getProjectAmount())
                .needOrgQ(project.getNeedOrgQ())
                .pitchingVideoUrl(project.getPitchingVideoUrl())
                .sponsorName(project.getSponsorName())
                .website(project.getWebsite())
                .incomeStatementUrl(project.getIncomeStatementUrl())
                .prototypeLink(project.getPrototypeLink())
                .projectStatus(project.getProjectStatus())
                .updatedOn(project.getUpdatedOn())
                .linkedIn(project.getLinkedIn())
                .monthlyIncome(project.getMonthlyIncome())
                .specialityOfProject(project.getSpecialityOfProject())
                .phone(project.getPhone())
                .needSponsorQ(project.getNeedSponsorQ())
                .numberOfEmp(project.getNumberOfEmp())
                .wantOriginToBusinessPlanQ(project.getWantOriginToBusinessPlanQ())
                .intellectualProjectQ(project.getIntellectualProjectQ())
                .email(project.getEmail())
                .build()
        ).toList();
    }



    public LaunchProjectResponse getProjectById(UUID projectId) {
        // Get the latest feedback for the project if it exists
        Optional<AnalyticsFeedback> latestFeedback = feedbackRepository.findLatestByProjectId(projectId);

        return launchProjectRepository.findById(projectId)
                .map(project -> {
                            LaunchProjectResponse.LaunchProjectResponseBuilder builder = LaunchProjectResponse.builder()
                                    .balanceSheetUrl(project.getBalanceSheetUrl())
                                    .projectId(project.getProjectId())
                                    .businessIdea(project.getBusinessIdea())
                                    .businessPlanUrl(project.getBusinessPlanUrl())
                                    .businessIdeaDocumentUrl(project.getBusinessIdeaDocumentUrl())
                                    .clientName(project.getClientName())
                                    .category(project.getCategory())
                                    .description(project.getDescription())
                                    .cashFlowUrl(project.getCashFlowUrl())
                                    .status(project.getStatus())
                                    .professionalStatus(project.getProfessionalStatus())
                                    .projectName(project.getProjectName())
                                    .haveSponsorQ(project.getHaveSponsorQ())
                                    .projectPurpose(project.getProjectPurpose())
                                    .projectLocation(project.getProjectLocation())
                                    .projectPhotoUrl(project.getProjectPhotoUrl())
                                    .doSellProjectQ(project.getDoSellProjectQ())
                                    .submittedOn(project.getSubmittedOn())
                                    .projectAmount(project.getProjectAmount())
                                    .needOrgQ(project.getNeedOrgQ())
                                    .pitchingVideoUrl(project.getPitchingVideoUrl())
                                    .sponsorName(project.getSponsorName())
                                    .website(project.getWebsite())
                                    .incomeStatementUrl(project.getIncomeStatementUrl())
                                    .prototypeLink(project.getPrototypeLink())
                                    .projectStatus(project.getProjectStatus())
                                    .updatedOn(project.getUpdatedOn())
                                    .linkedIn(project.getLinkedIn())
                                    .monthlyIncome(project.getMonthlyIncome())
                                    .specialityOfProject(project.getSpecialityOfProject())
                                    .phone(project.getPhone())
                                    .needSponsorQ(project.getNeedSponsorQ())
                                    .numberOfEmp(project.getNumberOfEmp())
                                    .views(project.getViews())
                                    .interaction(project.getInteraction())
                                    .countAssignment(project.getCountAssignment())
                                    .countBookmark(project.getCountBookmark())
                                    .wantOriginToBusinessPlanQ(project.getWantOriginToBusinessPlanQ())
                                    .intellectualProjectQ(project.getIntellectualProjectQ())
                                    .email(project.getEmail());


                            
                            // Only add feedback if it exists and the project is declined
                            if (latestFeedback.isPresent() && project.getStatus() == AnalyticStatus.DECLINED) {
                                builder.feedback(latestFeedback.get().getFeedback());
                            }
                            
                            return builder.build();
                        }
                )
                .orElseThrow(() -> new RuntimeException("Project not found with ID: " + projectId));
    }

    public List<LaunchProject> getAllProject(){

        return launchProjectRepository.findAll().stream().map(project->LaunchProject.builder().projectId(project.getProjectId())
                .clientName(project.getClientName())
                .professionalStatus(project.getProfessionalStatus())
                .email(project.getEmail())
                .phone(project.getPhone())
                .linkedIn(project.getLinkedIn())
                .projectName(project.getProjectName())
                .category(project.getCategory())
                .description(project.getDescription())
                .projectLocation(project.getProjectLocation())
                .projectStatus(project.getProjectStatus())
                .projectPurpose(project.getProjectPurpose())
                .specialityOfProject(project.getSpecialityOfProject())
                .haveSponsorQ(project.getHaveSponsorQ())
                .sponsorName(project.getSponsorName())
                .needSponsorQ(project.getNeedSponsorQ())
                .needOrgQ(project.getNeedOrgQ())
                .doSellProjectQ(project.getDoSellProjectQ())
                .projectAmount(project.getProjectAmount())
                .intellectualProjectQ(project.getIntellectualProjectQ())
                .wantOriginToBusinessPlanQ(project.getWantOriginToBusinessPlanQ())
                .businessIdea(project.getBusinessIdea())
                .projectPhotoUrl(project.getProjectPhotoUrl())
                .pitchingVideoUrl(project.getPitchingVideoUrl())
                .businessPlanUrl(project.getBusinessPlanUrl())
                .businessIdeaDocumentUrl(project.getBusinessIdeaDocumentUrl()).build()).toList();

    }

    public List<HomeProjectResponse> getHomeProjects() {
        return launchProjectRepository.findAll().stream()
                .filter(project -> !"declined".equalsIgnoreCase(String.valueOf(project.getStatus())))
                .map(project-> {
                    Double projectFinancialCategory = null;
                    if (project.getAnalyticProject() != null) {
                        projectFinancialCategory = project.getAnalyticProject().getPrice();
                    } 
                    return HomeProjectResponse.builder()
                            .projectId(project.getProjectId())
                            .clientName(project.getClientName())
                            .analyticStatus(project.getStatus())
                            .projectDescription(project.getDescription())
                            .projectUrl(project.getProjectPhotoUrl())
                            .category(project.getCategory())
                            .projectName(project.getProjectName())
                            .linkedInUrl(project.getLinkedIn())
                            .projectLocation(project.getProjectLocation())
                            .projectPurpose(project.getProjectPurpose())
                            .projectType(project.getProjectType())
                            .projectFinancialCategory(projectFinancialCategory)
                            .build();
                }).toList();
    }



    public List<MyProjectResponse> getMyProjectsByUserId(UUID userId) {
        List<LaunchProject> getProjects = launchProjectRepository.findByUserId(userId);

        return getProjects.stream().map(project->MyProjectResponse.builder()
                .projectId(project.getProjectId())
                .title(project.getProjectName())
                .description(project.getDescription())
                .submittedOn(project.getSubmittedOn())
                .updatedOn(project.getUpdatedOn())
                .build()).toList();
    }

    public void deleteProjectAndFiles(UUID projectId) {
        LaunchProject project = launchProjectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        if (project.getBusinessPlanUrl() != null && !project.getBusinessPlanUrl().isEmpty()) {
            cloudinaryService.deleteFile(project.getBusinessPlanUrl());
        }

        if (project.getBusinessIdeaDocumentUrl() != null && !project.getBusinessIdeaDocumentUrl().isEmpty()) {
            cloudinaryService.deleteFile(project.getBusinessIdeaDocumentUrl());
        }

        if (project.getProjectPhotoUrl() != null && !project.getProjectPhotoUrl().isEmpty()) {
            cloudinaryService.deleteFile(project.getProjectPhotoUrl());
        }

        if (project.getPitchingVideoUrl() != null && !project.getPitchingVideoUrl().isEmpty()) {
            cloudinaryService.deleteFile(project.getPitchingVideoUrl());
        }

        if (project.getIncomeStatementUrl() != null && !project.getIncomeStatementUrl().isEmpty()) {
            cloudinaryService.deleteFile(project.getIncomeStatementUrl());
        }

        if (project.getCashFlowUrl() != null && !project.getCashFlowUrl().isEmpty()) {
            cloudinaryService.deleteFile(project.getCashFlowUrl());
        }

        if (project.getBalanceSheetUrl() != null && !project.getBalanceSheetUrl().isEmpty()) {
            cloudinaryService.deleteFile(project.getBalanceSheetUrl());
        }

        launchProjectRepository.delete(project);
    }


    public ResponseEntity<UserAnalyticsResponse> getAnalyticsOfProject(UUID userId, UUID projectId) {
        Users user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        LaunchProject project = launchProjectRepository.findById(projectId).orElseThrow(() -> new RuntimeException("Project not found"));

            String highestPriorityPlan = planFilterServices.getPlanFiltered(user);
        AnalyticProject analytics = analyticsRepository.findByLaunchProject_ProjectId(projectId)
                .orElseThrow(() -> new EntityNotFoundException("Analytics not found for project"));

        if (project.getStatus()== AnalyticStatus.PENDING || !analytics.isAnalyticsEnabled()){
            throw new RuntimeException("analytics will came soon us possible");
         }
        if (highestPriorityPlan.equals("BASIC") || highestPriorityPlan.equals("FREE")) {
            throw new RuntimeException("upgrade your plan");
        }
        LaunchProject project1 = launchProjectRepository.findById(projectId).orElseThrow(() -> new RuntimeException("Project not found"));
        assert project1 != null;
        project.setInteraction(project.getInteraction()+1);
        launchProjectRepository.save(project);

        return ResponseEntity.ok( UserAnalyticsResponse.builder()
                .analyticsId(analytics.getAnalyticId())
                .feasibility(analytics.getFeasibility())
                .monthlyIncome(analytics.getMonthlyIncome())
                .feasibilityReason(analytics.getFeasibilityReason())
                .annualIncome(analytics.getAnnualIncome())
                .roi(analytics.getRoi())
                .incomeDescription(analytics.getIncomeDescription())
                .totalView(project1.getViews())
                .bookmarks(project1.getCountBookmark())
                .interested(project1.getInterestedInvestors())
                .interactions(project1.getInteraction())
                .price(analytics.getPrice())
                .costOfDevelopment(analytics.getCostOfDevelopment())
                .analyticsDocumentUrl(analytics.getAnalyticsDocumentUrl())
                .projectId(analytics.getLaunchProject().getProjectId())
                .build());

    }


    public HomeProjectResponse getHomeProjectsById(UUID id) {
        LaunchProject launchProject = launchProjectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found"));
        
        Double projectFinancialCategory = null;
        if (launchProject.getAnalyticProject() != null) {
            projectFinancialCategory = launchProject.getAnalyticProject().getPrice();
        }
        LaunchProject project2= launchProjectRepository.findById(id).orElse(null);
        assert project2 != null;
        project2.setViews(project2.getViews() + 1);
        launchProjectRepository.save(project2);
        
        return HomeProjectResponse.builder()
                .projectId(launchProject.getProjectId())
                .clientName(launchProject.getClientName())
                .analyticStatus(launchProject.getStatus())
                .projectDescription(launchProject.getDescription())
                .projectUrl(launchProject.getProjectPhotoUrl())
                .category(launchProject.getCategory())
                .projectName(launchProject.getProjectName())
                .linkedInUrl(launchProject.getLinkedIn())
                .projectLocation(launchProject.getProjectLocation())
                .projectPurpose(launchProject.getProjectPurpose())
                .projectType(launchProject.getProjectType())
                .projectFinancialCategory(projectFinancialCategory)
                .build();
    }
}
