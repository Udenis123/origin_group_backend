package com.org.group.services.Analyzer;

import com.org.group.dto.LaunchProject.AnalyticStatus;
import com.org.group.dto.admin.AnalyzerInfoDto;
import com.org.group.dto.analytics.AnalyticsDto;
import com.org.group.dto.analytics.AnalyticsResponseDto;
import com.org.group.model.analyzer.AnalyticProject;
import com.org.group.model.analyzer.AnalyticsFeedback;
import com.org.group.model.analyzer.Analyzer;
import com.org.group.model.analyzer.Assignment;
import com.org.group.model.project.LaunchProject;
import com.org.group.repository.AnalyzerRepository;
import com.org.group.repository.analytics.AnalyticProjectRepository;
import com.org.group.repository.analytics.AnalyticsFeedbackRepository;
import com.org.group.repository.analytics.AssignmentRepository;
import com.org.group.repository.project.LaunchProjectRepository;
import com.org.group.responses.project.LaunchProjectResponse;
import com.org.group.services.UploadFileServices.FileStorageService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AnalyzerServices {
    private final LaunchProjectRepository launchProjectRepository;
    private final AnalyzerRepository analyzerRepository;
    private final AssignmentRepository assignmentRepository;
    private final FileStorageService fileStorageService;
    private final AnalyticProjectRepository analyticsRepository;
    private final AnalyticsFeedbackRepository  feedbackRepository;
    public ResponseEntity<List<LaunchProjectResponse>> getAllPendingProject() {
        List<LaunchProject> projects = launchProjectRepository.findAll();

        List<LaunchProjectResponse> pendingProjects = projects.stream()
                .filter(project -> "PENDING".equalsIgnoreCase(project.getStatus().toString())) // adjust the value based on your enum or constant
                .map(project -> LaunchProjectResponse.builder()
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
                        .build())
                .toList();

        return ResponseEntity.ok(pendingProjects);
    }

    public void assignProject(UUID projectId, UUID analyzerId) {
        LaunchProject project = launchProjectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found"));

        Analyzer analyzer = analyzerRepository.findById(analyzerId)
                .orElseThrow(() -> new IllegalArgumentException("Analyzer not found"));

        // ✅ Check if the assignment already exists
        if (assignmentRepository.existsByProject_ProjectIdAndAnalyzer_Id(projectId, analyzerId)) {
            throw new IllegalArgumentException("This project is already assigned to this analyzer");
        }

        // ✅ Enforce max assignment rule
        if (project.getCountAssignment() >= 5) {
            throw new IllegalArgumentException("Project cannot be assigned to more than 5 analyzers");
        }

        // Proceed with assignment
        project.setCountAssignment(project.getCountAssignment() + 1);

        Assignment assignment = Assignment.builder()
                .analyzer(analyzer)
                .project(project)
                .build();

        assignmentRepository.save(assignment);
    }

    public String unassignProject(UUID projectId, UUID analyzerId) {
        LaunchProject project = launchProjectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found"));

        // Verify analyzer exists
        analyzerRepository.findById(analyzerId)
                .orElseThrow(() -> new IllegalArgumentException("Analyzer not found"));

        // Check if the assignment exists
        if (!assignmentRepository.existsByProject_ProjectIdAndAnalyzer_Id(projectId, analyzerId)) {
            throw new IllegalArgumentException("No assignment found between this project and analyzer");
        }

        // Find and delete the assignment
        Assignment assignment = assignmentRepository.findByProject_ProjectIdAndAnalyzer_Id(projectId, analyzerId)
                .orElseThrow(() -> new IllegalArgumentException("Assignment not found"));

        // Decrease assignment count
        if (project.getCountAssignment() > 0) {
            project.setCountAssignment(project.getCountAssignment() - 1);
            launchProjectRepository.save(project);
        }

        assignmentRepository.delete(assignment);
        
        return "Project unassigned from analyzer successfully";
    }

    public ResponseEntity<List<LaunchProjectResponse>> getAllAssignedProject(UUID analyzerId) {
        List<LaunchProject> projects = assignmentRepository.findPendingProjectsByAnalyzerId(analyzerId);

        List<LaunchProjectResponse> responseList = projects.stream()
                .map(project -> LaunchProjectResponse.builder()
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
                        .build())
                .toList();

        return ResponseEntity.ok(responseList);
    }


    public void putAnalytics(@Valid AnalyticsDto analyticsDto, MultipartFile analyticsDocument) throws IOException {
        LaunchProject project = launchProjectRepository.findById(analyticsDto.getProjectId()).orElse(null);
        AnalyticProject analytics = analyticsRepository.findByLaunchProject_ProjectId(project.getProjectId()).orElse(null);

        if(project == null){
            throw new IllegalArgumentException("incorrect project id");
        }
        if(analytics!=null){
            throw new IllegalArgumentException("project already  has analytics");
        }
        String analyticsDocUrl=null ;
        if(!analyticsDocument.isEmpty() ){
             analyticsDocUrl = fileStorageService.storeFile(analyticsDocument,"analytic_doc_for_"+analyticsDto.getProjectId());
        }

        AnalyticProject analyticProject = AnalyticProject.builder()
                .roi(analyticsDto.getRoi())
                .analyticsDocumentUrl(analyticsDocUrl)
                .annualIncome(analyticsDto.getAnnualIncome())
                .feasibility(analyticsDto.getFeasibility())
                .launchProject(project)
                .feasibilityReason(analyticsDto.getFeasibilityReason())
                .interested(0)
                .totalView(0)
                .bookmarks(project.getCountBookmark() != 0 ? project.getCountBookmark() : 0)
                .monthlyIncome(project.getMonthlyIncome() != null ? project.getMonthlyIncome() : 0.0)
                .price(analyticsDto.getPrice())
                .costOfDevelopment(analyticsDto.getCostOfDevelopment())
                .incomeDescription(analyticsDto.getIncomeDescription())
                .analyticsEnabled(false)

                .build();

        analyticsRepository.save(analyticProject);

    }

    public AnalyticsResponseDto getAnalyticsOfProject(UUID projectId) {
        AnalyticProject analytics = analyticsRepository.findByLaunchProject_ProjectId(projectId)
                .orElseThrow(() -> new EntityNotFoundException("Analytics not found for project"));

        return AnalyticsResponseDto.builder()
                .analyticsId(analytics.getAnalyticId())
                .feasibility(analytics.getFeasibility())
                .monthlyIncome(analytics.getMonthlyIncome())
                .feasibilityReason(analytics.getFeasibilityReason())
                .annualIncome(analytics.getAnnualIncome())
                .roi(analytics.getRoi())
                .incomeDescription(analytics.getIncomeDescription())
                .price(analytics.getPrice())
                .costOfDevelopment(analytics.getCostOfDevelopment())
                .analyticsDocumentUrl(analytics.getAnalyticsDocumentUrl())
                .projectId(analytics.getLaunchProject().getProjectId())
                .build();

    }

    public String updateAnalytics(AnalyticsDto analyticsDto, MultipartFile analyticsDocument) throws IOException {
        // Fetch the analytic project based on the project ID
        AnalyticProject analyticProject = analyticsRepository.findByLaunchProject_ProjectId(analyticsDto.getProjectId())
                .orElseThrow(() -> new EntityNotFoundException("Analytics not found for project"));

        // Double-check with the ID from the fetched project
        long id = analyticProject.getAnalyticId();
        AnalyticProject analyticProject1 = analyticsRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Incorrect project id or analytics"));

        // Prevent update if analytics is already marked as complete
        if (analyticProject1.isAnalyticsEnabled()) {
            throw new RuntimeException("Can't update analytics since it is marked as complete");
        }

        // Handle optional document update
        String analyticsDocUrl = analyticProject1.getAnalyticsDocumentUrl();
        if (analyticsDocument != null && !analyticsDocument.isEmpty()) {
            if (analyticsDocUrl != null && !analyticsDocUrl.isEmpty()) {
                fileStorageService.deleteFile(analyticsDocUrl);
            }
            analyticsDocUrl = fileStorageService.storeFile(
                    analyticsDocument,
                    "analytic_doc_for_" + analyticsDto.getProjectId()
            );
        }

        // Update project fields
        analyticProject1.setFeasibility(analyticsDto.getFeasibility());
        analyticProject1.setMonthlyIncome(analyticsDto.getMonthlyIncome());
        analyticProject1.setAnnualIncome(analyticsDto.getAnnualIncome());
        analyticProject1.setRoi(analyticsDto.getRoi());
        analyticProject1.setIncomeDescription(analyticsDto.getIncomeDescription());
        analyticProject1.setCostOfDevelopment(analyticsDto.getCostOfDevelopment());
        analyticProject1.setFeasibilityReason(analyticsDto.getFeasibilityReason());
        analyticProject1.setPrice(analyticsDto.getPrice());
        analyticProject1.setAnalyticsDocumentUrl(analyticsDocUrl);

        // Save the updated entity
        analyticsRepository.save(analyticProject1);

        return "Project updated successfully";
    }


    public ResponseEntity<?>declineProject(UUID projectId, UUID analyzerId, String feedback) {
        LaunchProject project = launchProjectRepository.findById(projectId)
                .orElseThrow(()-> new RuntimeException("Project not found"));
        AnalyticProject analyticProject = analyticsRepository.findByLaunchProject_ProjectId(projectId).orElse(null);

        if(analyticProject != null) {
            analyticsRepository.delete(analyticProject);
        }

        // Delete any existing feedback for this project
        List<AnalyticsFeedback> existingFeedback = feedbackRepository.findByProjectId(projectId);
        if (!existingFeedback.isEmpty()) {
            feedbackRepository.deleteAll(existingFeedback);
        }

        project.setStatus(AnalyticStatus.DECLINED);
        launchProjectRepository.save(project);

        // Create and save new feedback
        AnalyticsFeedback analyticsFeedback = AnalyticsFeedback.builder()
                .projectId(projectId)
                .analyzerId(analyzerId)
                .Feedback(feedback)
                .build();
        feedbackRepository.save(analyticsFeedback);
        
        return ResponseEntity.ok("Project declined successfully");
    }

    public String enableAnalyticsOfProject(UUID projectId) {
        AnalyticProject analyticProject = analyticsRepository.
                findByLaunchProject_ProjectId(projectId).
                orElseThrow(() -> new EntityNotFoundException("Analytics not found for project"));
        analyticProject.setAnalyticsEnabled(true);
        analyticsRepository.save(analyticProject);
        return "Project enabled successfully";
    }

    public ResponseEntity<List<AnalyzerInfoDto>> getAnalyzersForProject(UUID projectId) {
        // Verify the project exists
        launchProjectRepository.findById(projectId)
                .orElseThrow(() -> new EntityNotFoundException("Project not found with ID: " + projectId));
        
        // Find all assignments for the project
        List<Assignment> assignments = assignmentRepository.findAssignmentsByProjectId(projectId);
        
        // Map assignments to analyzer info DTOs
        List<AnalyzerInfoDto> analyzerInfoList = assignments.stream()
                .map(assignment -> {
                    Analyzer analyzer = assignment.getAnalyzer();
                    return AnalyzerInfoDto.builder()
                            .id(analyzer.getId())
                            .name(analyzer.getName())
                            .email(analyzer.getEmail())
                            .phone(analyzer.getPhone())
                            .expertise(analyzer.getExpertise())
                            .profileUrl(analyzer.getProfileUrl())
                            .nationality(analyzer.getNationality())
                            .gender(analyzer.getGender())
                            .nationalId(analyzer.getNationalId())
                            .enabled(analyzer.isEnabled())
                            .build();
                })
                .toList();
        
        return ResponseEntity.ok(analyzerInfoList);
    }
}
