package com.org.group.services.Admin;

import com.org.group.dto.LaunchProject.AnalyticStatus;
import com.org.group.dto.admin.AnalyzerDto;
import com.org.group.dto.admin.AnalyzerInfoDto;
import com.org.group.dto.admin.UpdateAnalyzerDto;
import com.org.group.dto.analytics.AnalyticsResponseDto;
import com.org.group.dto.userAuth.LoginUserDto;
import com.org.group.exceptionHandling.UnauthorizedException;
import com.org.group.model.Users;
import com.org.group.model.analyzer.AnalyticProject;
import com.org.group.model.analyzer.Analyzer;
import com.org.group.model.project.LaunchProject;
import com.org.group.repository.AnalyzerRepository;
import com.org.group.repository.UserRepository;
import com.org.group.repository.analytics.AnalyticProjectRepository;
import com.org.group.repository.project.LaunchProjectRepository;
import com.org.group.responses.Users.ClientResponseDto;
import com.org.group.responses.project.LaunchProjectResponse;
import com.org.group.responses.project.LaunchedProjectAnalyticsResponse;
import com.org.group.role.Role;
import com.org.group.services.emailAndJwt.PlanFilterServices;
import com.org.group.services.emailAndJwt.EmailService;
import jakarta.mail.MessagingException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;

import com.org.group.dto.admin.AssignedProjectDto;

@Service
@RequiredArgsConstructor
public class AdminServices {

    private final AnalyzerRepository analyzerRepository;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final AnalyticProjectRepository analyticsRepository;
    private final LaunchProjectRepository launchProjectRepository;
    private final UserRepository userRepository;
    private final PlanFilterServices planFilterServices;
    private final EmailService emailService;


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
        
        // Send welcome email with login credentials
        try {
            sendAnalyzerWelcomeEmail(analyzer, analyzerDto.getPassword());
        } catch (MessagingException e) {
            // Log the error but don't fail the registration
            System.err.println("Failed to send welcome email to analyzer: " + e.getMessage());
        }
    }

    public void updateAnalyzer(UUID analyzerId, UpdateAnalyzerDto analyzerDto) {
        // Find existing analyzer by ID
        Analyzer existingAnalyzer = analyzerRepository.findById(analyzerId)
                .orElseThrow(() -> new EntityNotFoundException("Analyzer with id " + analyzerId + " not found"));

        // Update fields only if provided
        if (analyzerDto.getName() != null && !analyzerDto.getName().trim().isEmpty()) {
            existingAnalyzer.setName(analyzerDto.getName());
        }
        
        if (analyzerDto.getGender() != null && !analyzerDto.getGender().trim().isEmpty()) {
            existingAnalyzer.setGender(analyzerDto.getGender());
        }
        
        if (analyzerDto.getPhone() != null && !analyzerDto.getPhone().trim().isEmpty()) {
            existingAnalyzer.setPhone(analyzerDto.getPhone());
        }
        
        if (analyzerDto.getExpertise() != null && !analyzerDto.getExpertise().trim().isEmpty()) {
            existingAnalyzer.setExpertise(analyzerDto.getExpertise());
        }
        
        if (analyzerDto.getNationalId() != null && !analyzerDto.getNationalId().trim().isEmpty()) {
            existingAnalyzer.setNationalId(analyzerDto.getNationalId());
        }
        
        if (analyzerDto.getNationality() != null && !analyzerDto.getNationality().trim().isEmpty()) {
            existingAnalyzer.setNationality(analyzerDto.getNationality());
        }

        // Update password only if provided
        if (analyzerDto.getPassword() != null && !analyzerDto.getPassword().trim().isEmpty()) {
            existingAnalyzer.setPassword(passwordEncoder.encode(analyzerDto.getPassword()));
        }

        analyzerRepository.save(existingAnalyzer);
    }
    public String enableOrDisableAnalyzer(UUID analyzerId) {
        Analyzer analyzer = analyzerRepository.findById(analyzerId)
                .orElseThrow(() -> new EntityNotFoundException("Analyzer with id " + analyzerId + " not found"));
        if(analyzer.isEnabled()){
            analyzer.setEnabled(false);
        } else {
            analyzer.setEnabled(true);
        }
        analyzerRepository.save(analyzer);

        return "Action Successful";
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

       if(projects.isEmpty()) {
           return ResponseEntity.noContent().build();
       }

        List<LaunchedProjectAnalyticsResponse> pendingProjects = projects.stream()
                .filter(project -> "PENDING".equalsIgnoreCase(project.getStatus().toString()) && project.getAnalyticProject() != null && project.getAnalyticProject().isAnalyticsEnabled()) // adjust the value based on your enum or constant
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
                .filter(analyzer -> !analyzer.getRoles().contains(Role.ADMIN))
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

    public AnalyzerInfoDto getAnalyzerById(UUID analyzerId) {
        Analyzer analyzer = analyzerRepository.findByIdWithAssignments(analyzerId)
                .orElseThrow(() -> new EntityNotFoundException("Analyzer with id " + analyzerId + " not found"));

        List<AssignedProjectDto> assignedProjects = analyzer.getAssignment().stream()
                .map(assignment -> AssignedProjectDto.builder()
                        .projectId(assignment.getProject().getProjectId())
                        .projectName(assignment.getProject().getProjectName())
                        .description(assignment.getProject().getDescription())
                        .status(assignment.getProject().getStatus())
                        .projectPhotoUrl(assignment.getProject().getProjectPhotoUrl())
                        .build())
                .toList();

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
                .assignedProjects(assignedProjects)
                .build();
    }


    public List<ClientResponseDto> getAllClient() {
        List<Users> users = userRepository.findAll();
        List<ClientResponseDto> clients = users.stream().map(

                client->{
                    String filteredPlan = planFilterServices.getPlanFiltered(client);
                     return ClientResponseDto.builder()
                            .id(client.getId())
                            .name(client.getName())
                            .email(client.getEmail())
                            .phone(client.getPhone())
                            .enabled(client.isEnabled())
                            .isActive(client.isActive())
                            .nationalId(client.getNationalId())
                            .gender(client.getGender())
                            .photoUrl(client.getPhotoUrl())
                            .professional(client.getProfessional())
                            .tempEmail(client.getTempEmail())
                            .roles(client.getRoles())
                            .subscribed(client.getSubscribed())
                            .nationality(client.getNationality())
                             .currentSubscription(filteredPlan)
                            .build();
                }
        ).toList();


        return clients;
    }

    private void sendAnalyzerWelcomeEmail(Analyzer analyzer, String plainPassword) throws MessagingException {
        String subject = "Welcome to Origin Group - Your Analyzer Account";
        String htmlMessage = createAnalyzerWelcomeEmailContent(analyzer, plainPassword);
        emailService.sendVerificationEmail(analyzer.getEmail(), subject, htmlMessage);
    }

    private String createAnalyzerWelcomeEmailContent(Analyzer analyzer, String plainPassword) {
        return "<html>" +
                "<head>" +
                "<style>" +
                "  body { font-family: Arial, sans-serif; background-color: #f7f7f7; margin: 0; padding: 0; }" +
                "  .email-container { max-width: 600px; margin: 0 auto; background-color: #ffffff; border-radius: 8px; overflow: hidden; box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1); }" +
                "  .header { background-color: #131b5a; color: #ffffff; text-align: center; padding: 20px; }" +
                "  .header h1 { margin: 0; font-size: 24px; font-weight: bold; }" +
                "  .content { padding: 30px; text-align: center; }" +
                "  .content h2 { color: #333333; font-size: 20px; margin-bottom: 20px; }" +
                "  .credentials { background-color: #f0f0f0; padding: 20px; border-radius: 6px; margin: 20px 0; text-align: left; }" +
                "  .credentials h3 { color: #131b5a; margin-top: 0; }" +
                "  .credentials p { margin: 10px 0; font-size: 16px; }" +
                "  .login-button { display: inline-block; background-color: #131b5a; color: #ffffff; padding: 15px 30px; text-decoration: none; border-radius: 6px; font-weight: bold; margin: 20px 0; }" +
                "  .login-button:hover { background-color: #0f1447; }" +
                "  .footer { text-align: center; padding: 20px; font-size: 14px; color: #666666; background-color: #fbdfb8; }" +
                "  .footer a { color: #007bff; text-decoration: none; }" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<div class='email-container'>" +
                "  <div class='header'>" +
                "    <h1>Welcome to Origin Group</h1>" +
                "  </div>" +
                "  <div class='content'>" +
                "    <h2>Your Analyzer Account is Ready!</h2>" +
                "    <p>Dear " + analyzer.getName() + ",</p>" +
                "    <p>Welcome to Origin Group! Your analyzer account has been successfully created.</p>" +
                "    <div class='credentials'>" +
                "      <h3>Your Login Credentials:</h3>" +
                "      <p><strong>Email:</strong> " + analyzer.getEmail() + "</p>" +
                "      <p><strong>Password:</strong> " + plainPassword + "</p>" +
                "    </div>" +
                "    <p>Please use these credentials to log in to your account and start analyzing projects.</p>" +
                "    <a href='https://orgin-group-analyzer.vercel.app/' class='login-button'>Login to Your Account</a>" +
                "    <p><small>If the button doesn't work, copy and paste this link: <a href='http://localhost:4201/login'>http://localhost:4201/login</a></small></p>" +
                "  </div>" +
                "  <div class='footer'>" +
                "    <p>Need help? <a href='mailto:origin@group.com'>Contact Support</a></p>" +
                "    <p>&copy; 2025 Origin Group. All rights reserved.</p>" +
                "  </div>" +
                "</div>" +
                "</body>" +
                "</html>";
    }
}
