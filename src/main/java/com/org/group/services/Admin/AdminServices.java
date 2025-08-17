package com.org.group.services.Admin;

import com.org.group.dto.LaunchProject.AnalyticStatus;
import com.org.group.dto.admin.AnalyzerDto;
import com.org.group.dto.admin.AnalyzerInfoDto;
import com.org.group.dto.admin.UserInfoDto;
import com.org.group.dto.analytics.AnalyticsResponseDto;
import com.org.group.dto.userAuth.LoginUserDto;
import com.org.group.exceptionHandling.UnauthorizedException;
import com.org.group.model.analyzer.AnalyticProject;
import com.org.group.model.analyzer.Analyzer;
import com.org.group.model.project.LaunchProject;
import com.org.group.model.Users;
import com.org.group.repository.AnalyzerRepository;
import com.org.group.repository.analytics.AnalyticProjectRepository;
import com.org.group.repository.project.LaunchProjectRepository;
import com.org.group.repository.UserRepository;
import com.org.group.responses.project.LaunchProjectResponse;
import com.org.group.responses.project.LaunchedProjectAnalyticsResponse;
import com.org.group.role.Role;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminServices {

    private final AnalyzerRepository analyzerRepository;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final AnalyticProjectRepository analyticsRepository;
    private final LaunchProjectRepository launchProjectRepository;
    private final UserRepository userRepository;


    public void registerAnalyzer(AnalyzerDto analyzerDto) {
        Analyzer analyzer = Analyzer.builder()
                .name(analyzerDto.getName())
                .gender(analyzerDto.getGender())
                .phone(analyzerDto.getPhone())
                .expertise(analyzerDto.getExpertise())
                .email(analyzerDto.getEmail()) // ✅ Corrected here
                .roles(Set.of(Role.ANALYZER))
                .password(passwordEncoder.encode(analyzerDto.getPassword())) // ✅ Also ensure encoding
                .profileUrl("https://static.vecteezy.com/system/resources/thumbnails/010/260/479/small_2x/default-avatar-profile-icon-of-social-media-user-in-clipart-style-vector.jpg")
                .enabled(true)
                .nationalId(analyzerDto.getNationalId())
                .nationality(analyzerDto.getNationality())
                .build();
        analyzerRepository.save(analyzer);
    }
    public String enableOrDisableAnalyzer(UUID analyzerId) {
          Analyzer analyzer = analyzerRepository.findById(analyzerId).orElseThrow(()-> new EntityNotFoundException("Analyzer with id " + analyzerId + " not found"));
          if(analyzer.isEnabled()){
              analyzer.setEnabled(false);
          }else {
              analyzer.setEnabled(true);
          }
          analyzerRepository.save(analyzer);
        return "Action Successful" ;
    }


    public Analyzer authenticate(LoginUserDto input) {
        Optional<Analyzer> analyzerOpt = analyzerRepository.findByEmail(input.getEmail());

        if (!analyzerOpt.isPresent()) {
            throw new UnauthorizedException("No user found with that email.");
        }

        Analyzer analyzer = analyzerOpt.get();

        if (!analyzer.isEnabled()) {
            throw new UnauthorizedException("Contact Administrator to enable your account.");
        }

        // Authenticate using authentication manager
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(input.getEmail(), input.getPassword())
            );
        } catch (Exception ex) {
            throw new UnauthorizedException("Invalid email or password.");
        }

        return analyzer;
    }

    public AnalyticsResponseDto getAnalyticsOfProject(UUID projectId) {
        AnalyticProject analytics = analyticsRepository.findByLaunchProject_ProjectId(projectId)
                .orElseThrow(() -> new EntityNotFoundException("Analytics not found for project"));
        if(!analytics.isAnalyticsEnabled()) {
            throw  new RuntimeException("project analytics is still disabled.");
        }
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

    public String approveProject(UUID projectId) {
        LaunchProject launchProject = launchProjectRepository.findById(projectId).
                orElseThrow(() -> new EntityNotFoundException("Launch project not found"));
        launchProject.setStatus(AnalyticStatus.APPROVED);
        launchProjectRepository.save(launchProject);
        return "Approved";
    }

    public ResponseEntity<List<LaunchProjectResponse>> getAllProject() {
        List<LaunchProject> projects = launchProjectRepository.findAll();
        List<LaunchProjectResponse> pendingProjects = projects.stream()
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
    public ResponseEntity<List<LaunchedProjectAnalyticsResponse>> getAllPendingAndHaveAnalytics() {
        List<LaunchProject> projects = launchProjectRepository.findAll();


        List<LaunchedProjectAnalyticsResponse> pendingProjects = projects.stream()
                .filter(project -> "PENDING".equalsIgnoreCase(project.getStatus().toString()) && project.getAnalyticProject().isAnalyticsEnabled()) // adjust the value based on your enum or constant
                .map(project -> LaunchedProjectAnalyticsResponse.builder()
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
                        .analyticProject(project.getAnalyticProject())
                        .build())
                .toList();

        return ResponseEntity.ok(pendingProjects);
    }

    public ResponseEntity<List<LaunchProjectResponse>> getAllApprovedProject() {
        List<LaunchProject> projects = launchProjectRepository.findAll();

        List<LaunchProjectResponse> pendingProjects = projects.stream()
                .filter(project -> "APPROVED".equalsIgnoreCase(project.getStatus().toString())) // adjust the value based on your enum or constant
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

    public List<AnalyzerInfoDto> getAllAnalyzersInfo() {
        return analyzerRepository.findAll().stream()
                .map(analyzer -> AnalyzerInfoDto.builder()
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
                        .build())
                .toList();
    }

    public Analyzer getAnalyzerById(UUID analyzerId) {
        return analyzerRepository.findById(analyzerId)
                .orElseThrow(() -> new EntityNotFoundException("Analyzer with id " + analyzerId + " not found"));
    }

    public List<UserInfoDto> getAllUsersInfo() {
        return userRepository.findAll().stream()
                .map(user -> UserInfoDto.builder()
                        .id(user.getId())
                        .name(user.getName())
                        .email(user.getEmail())
                        .phone(user.getPhone())
                        .nationalId(user.getNationalId())
                        .gender(user.getGender())
                        .nationality(user.getNationality())
                        .professional(user.getProfessional())
                        .photoUrl(user.getPhotoUrl())
                        .enabled(user.isEnabled())
                        .subscribed(user.getSubscribed())
                        .isActive(user.isActive())
                        .build())
                .toList();
    }

    public Users getUserById(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User with id " + userId + " not found"));
    }

    public String DisableOrEnableUser(UUID userId) {
        Users user = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("User with id " + userId + " not found"));
        if (user.isActive()) {
            user.setActive(false);
            userRepository.save(user);
        }else {
            user.setActive(true);
            userRepository.save(user);
        }

        return "ok";
    }
}
