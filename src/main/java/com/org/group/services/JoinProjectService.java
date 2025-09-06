package com.org.group.services;

import com.org.group.dto.community.*;
import com.org.group.model.JoinStatus;
import com.org.group.model.Users;
import com.org.group.model.project.CommunityProject;
import com.org.group.model.project.JoinedProject;
import com.org.group.model.project.TeamMember;
import com.org.group.repository.JoinRepository;
import com.org.group.repository.UserRepository;
import com.org.group.repository.project.CommunityProjectRepository;
import com.org.group.services.emailAndJwt.EmailService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import jakarta.mail.MessagingException;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class JoinProjectService {

    private final JoinRepository joinRepository;
    private final CommunityProjectRepository communityProjectRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    public JoinProjectResponseDto joinProject(UUID userId, JoinProjectRequestDto requestDto) {
        // Validate user exists
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

        // Validate community project exists
        CommunityProject project = communityProjectRepository.findById(requestDto.getCommunityProjectId())
                .orElseThrow(() -> new EntityNotFoundException("Community project not found with id: " + requestDto.getCommunityProjectId()));

        // Check if user is trying to join their own project
        if (project.getUser().getId().equals(userId)) {
            throw new RuntimeException("You cannot join your own community project");
        }

        // Check if user already has a join request for this project
        List<JoinedProject> existingRequests = joinRepository.findByUserId(userId);
        boolean hasExistingRequest = existingRequests.stream()
                .anyMatch(request -> request.getCommunityProjectId().equals(requestDto.getCommunityProjectId()));
        if (hasExistingRequest) {
            throw new RuntimeException("You have already submitted a join request for this project");
        }

        // Validate team exists and has available slots
        TeamMember selectedTeam = validateAndGetTeam(project, requestDto.getJoinedTeam());

        // Create join request
        JoinedProject joinRequest = JoinedProject.builder()
                .userId(userId)
                .communityProjectId(requestDto.getCommunityProjectId())
                .description(requestDto.getDescription())
                .status(JoinStatus.REQUESTED)
                .joinedTeam(requestDto.getJoinedTeam())
                .build();

        JoinedProject savedRequest = joinRepository.save(joinRequest);

        log.info("Join request created for user {} to project {}", userId, requestDto.getCommunityProjectId());

        // Send email notification to project owner
        sendJoinRequestNotificationToOwner(project, user, savedRequest);

        return JoinProjectResponseDto.builder()
                .id(savedRequest.getId())
                .userId(savedRequest.getUserId())
                .communityProjectId(savedRequest.getCommunityProjectId())
                .description(savedRequest.getDescription())
                .status(savedRequest.getStatus())
                .joinedTeam(savedRequest.getJoinedTeam())
                .createdAt(LocalDateTime.now())
                .message("Join request submitted successfully")
                .build();
    }

    public List<JoinedMemberResponseDto> getJoinedMembersByProjectId(UUID projectId) {
        // Validate project exists
        CommunityProject project = communityProjectRepository.findById(projectId)
                .orElseThrow(() -> new EntityNotFoundException("Community project not found with id: " + projectId));

        List<JoinedProject> joinRequests = joinRepository.findByCommunityProjectId(projectId);

        return joinRequests.stream()
                .filter(joinRequest -> joinRequest.getStatus() == JoinStatus.ACCEPTED || joinRequest.getStatus() == JoinStatus.REQUESTED)
                .map(joinRequest -> {
                    Users user = userRepository.findById(joinRequest.getUserId())
                            .orElseThrow(() -> new EntityNotFoundException("User not found"));
                    
                    return JoinedMemberResponseDto.builder()
                            .joinId(joinRequest.getId())
                            .userId(joinRequest.getUserId())
                            .userName(user.getName())
                            .userEmail(user.getEmail())
                            .userPhone(user.getPhone())
                            .description(joinRequest.getDescription())
                            .status(joinRequest.getStatus())
                            .joinedTeam(joinRequest.getJoinedTeam())
                            .createdAt(LocalDateTime.now()) // You might want to add createdAt to JoinedProject model
                            .build();
                })
                .collect(Collectors.toList());
    }

    public JoinProjectResponseDto handleJoinRequest(JoinRequestActionDto actionDto) {
        JoinedProject joinRequest = joinRepository.findById(actionDto.getJoinId())
                .orElseThrow(() -> new EntityNotFoundException("Join request not found with id: " + actionDto.getJoinId()));

        if (joinRequest.getStatus() != JoinStatus.REQUESTED) {
            throw new RuntimeException("Join request has already been processed");
        }

        CommunityProject project = communityProjectRepository.findById(joinRequest.getCommunityProjectId())
                .orElseThrow(() -> new EntityNotFoundException("Community project not found"));

        JoinStatus originalStatus = joinRequest.getStatus();

        if (actionDto.getAction() == JoinStatus.ACCEPTED) {
            // Accept the request
            joinRequest.setStatus(JoinStatus.ACCEPTED);
            
            // Reduce team member count
            reduceTeamMemberCount(project, joinRequest.getJoinedTeam());
            
            // Send acceptance email to joiner
            sendJoinRequestResponseEmail(joinRequest, project, JoinStatus.ACCEPTED, actionDto.getReason());
            
            log.info("Join request accepted for user {} to project {}", joinRequest.getUserId(), joinRequest.getCommunityProjectId());
        } else if (actionDto.getAction() == JoinStatus.REJECTED) {
            // Reject the request
            joinRequest.setStatus(JoinStatus.REJECTED);
            
            // If the request was previously accepted, increase team member count back
            if (originalStatus == JoinStatus.ACCEPTED) {
                increaseTeamMemberCount(project, joinRequest.getJoinedTeam());
            }
            
            // Send rejection email to joiner
            sendJoinRequestResponseEmail(joinRequest, project, JoinStatus.REJECTED, actionDto.getReason());
            
            log.info("Join request rejected for user {} to project {}", joinRequest.getUserId(), joinRequest.getCommunityProjectId());
        } else {
            throw new RuntimeException("Invalid action. Only ACCEPTED or REJECTED are allowed");
        }

        JoinedProject updatedRequest = joinRepository.save(joinRequest);

        return JoinProjectResponseDto.builder()
                .id(updatedRequest.getId())
                .userId(updatedRequest.getUserId())
                .communityProjectId(updatedRequest.getCommunityProjectId())
                .description(updatedRequest.getDescription())
                .status(updatedRequest.getStatus())
                .joinedTeam(updatedRequest.getJoinedTeam())
                .createdAt(LocalDateTime.now())
                .message("Join request " + actionDto.getAction().toString().toLowerCase() + " successfully")
                .build();
    }

    private TeamMember validateAndGetTeam(CommunityProject project, String teamName) {
        List<TeamMember> teams = project.getTeam();
        if (teams == null || teams.isEmpty()) {
            throw new RuntimeException("No teams available for this project");
        }

        TeamMember selectedTeam = teams.stream()
                .filter(team -> team.getTitle().equals(teamName))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Team '" + teamName + "' not found in this project"));

        if (selectedTeam.getNumber() <= 0) {
            throw new RuntimeException("No available slots in team '" + teamName + "'");
        }

        return selectedTeam;
    }

    private void reduceTeamMemberCount(CommunityProject project, String teamName) {
        List<TeamMember> teams = project.getTeam();
        TeamMember team = teams.stream()
                .filter(t -> t.getTitle().equals(teamName))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Team not found"));

        if (team.getNumber() > 0) {
            team.setNumber(team.getNumber() - 1);
            communityProjectRepository.save(project);
            log.info("Reduced team member count for team '{}' in project {}", teamName, project.getId());
        }
    }

    public void increaseTeamMemberCount(CommunityProject project, String teamName) {
        List<TeamMember> teams = project.getTeam();
        TeamMember team = teams.stream()
                .filter(t -> t.getTitle().equals(teamName))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Team not found"));

        team.setNumber(team.getNumber() + 1);
        communityProjectRepository.save(project);
        log.info("Increased team member count for team '{}' in project {}", teamName, project.getId());
    }

    public List<JoinedMemberResponseDto> getMyJoinRequests(UUID userId) {
        List<JoinedProject> joinRequests = joinRepository.findByUserId(userId);

        return joinRequests.stream()
                .map(joinRequest -> {
                    Users user = userRepository.findById(joinRequest.getUserId())
                            .orElseThrow(() -> new EntityNotFoundException("User not found"));
                    
                    return JoinedMemberResponseDto.builder()
                            .joinId(joinRequest.getId())
                            .userId(joinRequest.getUserId())
                            .userName(user.getName())
                            .userEmail(user.getEmail())
                            .userPhone(user.getPhone())
                            .description(joinRequest.getDescription())
                            .status(joinRequest.getStatus())
                            .joinedTeam(joinRequest.getJoinedTeam())
                            .createdAt(LocalDateTime.now())
                            .build();
                })
                .collect(Collectors.toList());
    }

    public List<JoinedProjectWithDetailsDto> getJoinedProjects(UUID userId) {
        // Get all join requests for the user (including all statuses)
        List<JoinedProject> userJoins = joinRepository.findByUserId(userId);

        return userJoins.stream()
                .map(joinRequest -> {
                    // Get the community project for this join request
                    CommunityProject project = communityProjectRepository.findById(joinRequest.getCommunityProjectId())
                            .orElseThrow(() -> new EntityNotFoundException("Community project not found"));

                    // Build the combined DTO with both project and join details
                    return JoinedProjectWithDetailsDto.builder()
                            // Community Project Details
                            .projectId(project.getId())
                            .fullName(project.getFullName())
                            .profession(project.getProfession())
                            .email(project.getEmail())
                            .phone(project.getPhone())
                            .linkedIn(project.getLinkedIn())
                            .projectPhoto(project.getProjectPhoto())
                            .projectName(project.getProjectName())
                            .category(project.getCategory())
                            .location(project.getLocation())
                            .description(project.getDescription())
                            .projectStatus(project.getStatus())
                            .reason(project.getReason())
                            .projectCreatedAt(project.getCreatedAt())
                            .projectUpdatedOn(project.getUpdatedOn())
                            .team(project.getTeam())
                            .projectOwnerId(project.getUser().getId())
                            
                            // Join Details
                            .joinId(joinRequest.getId())
                            .joinDescription(joinRequest.getDescription())
                            .joinStatus(joinRequest.getStatus())
                            .joinedTeam(joinRequest.getJoinedTeam())
                            .joinCreatedAt(LocalDateTime.now()) // You might want to add createdAt to JoinedProject model
                            .build();
                })
                .collect(Collectors.toList());
    }

    private CommunityResponseDto convertToCommunityResponseDto(CommunityProject project) {
        return CommunityResponseDto.builder()
                .id(project.getId())
                .fullName(project.getFullName())
                .profession(project.getProfession())
                .email(project.getEmail())
                .phone(project.getPhone())
                .linkedIn(project.getLinkedIn())
                .projectPhoto(project.getProjectPhoto())
                .projectName(project.getProjectName())
                .category(project.getCategory())
                .location(project.getLocation())
                .description(project.getDescription())
                .status(project.getStatus())
                .reason(project.getReason())
                .createdAt(project.getCreatedAt())
                .updatedOn(project.getUpdatedOn())
                .team(project.getTeam())
                .userId(project.getUser().getId())
                .build();
    }

    // Email notification methods
    private void sendJoinRequestNotificationToOwner(CommunityProject project, Users joiner, JoinedProject joinRequest) {
        try {
            String subject = "New Join Request for Your Project: " + project.getProjectName();
            String htmlMessage = createJoinRequestNotificationEmail(project, joiner, joinRequest);
            
            emailService.sendVerificationEmail(project.getUser().getEmail(), subject, htmlMessage);
            log.info("Join request notification sent to project owner: {}", project.getUser().getEmail());
        } catch (MessagingException e) {
            log.error("Failed to send join request notification email to project owner", e);
        }
    }

    private void sendJoinRequestResponseEmail(JoinedProject joinRequest, CommunityProject project, JoinStatus status, String reason) {
        try {
            Users joiner = userRepository.findById(joinRequest.getUserId())
                    .orElseThrow(() -> new EntityNotFoundException("User not found"));
            
            String subject = status == JoinStatus.ACCEPTED ? 
                    "Your Join Request Has Been Accepted - " + project.getProjectName() :
                    "Your Join Request Has Been Declined - " + project.getProjectName();
            
            String htmlMessage = createJoinRequestResponseEmail(project, joiner, joinRequest, status, reason);
            
            emailService.sendVerificationEmail(joiner.getEmail(), subject, htmlMessage);
            log.info("Join request response email sent to joiner: {}", joiner.getEmail());
        } catch (MessagingException e) {
            log.error("Failed to send join request response email to joiner", e);
        }
    }

    private String createJoinRequestNotificationEmail(CommunityProject project, Users joiner, JoinedProject joinRequest) {
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "  <meta charset='UTF-8'>" +
                "  <style>" +
                "    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; margin: 0; padding: 0; }" +
                "    .email-container { max-width: 600px; margin: 0 auto; background-color: #f9f9f9; }" +
                "    .header { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 30px; text-align: center; }" +
                "    .header h1 { margin: 0; font-size: 24px; }" +
                "    .content { background: white; padding: 30px; }" +
                "    .project-info { background: #f8f9fa; padding: 20px; border-radius: 8px; margin: 20px 0; }" +
                "    .joiner-info { background: #e3f2fd; padding: 20px; border-radius: 8px; margin: 20px 0; }" +
                "    .cta-button { display: inline-block; background: #667eea; color: white; padding: 12px 30px; text-decoration: none; border-radius: 5px; margin: 20px 0; }" +
                "    .footer { background: #333; color: white; padding: 20px; text-align: center; font-size: 12px; }" +
                "    .status-badge { display: inline-block; padding: 5px 15px; border-radius: 20px; font-size: 12px; font-weight: bold; }" +
                "    .status-requested { background: #fff3cd; color: #856404; }" +
                "  </style>" +
                "</head>" +
                "<body>" +
                "<div class='email-container'>" +
                "  <div class='header'>" +
                "    <h1>New Join Request</h1>" +
                "    <p>Someone wants to join your community project!</p>" +
                "  </div>" +
                "  <div class='content'>" +
                "    <h2>Project Details</h2>" +
                "    <div class='project-info'>" +
                "      <h3>" + project.getProjectName() + "</h3>" +
                "      <p><strong>Category:</strong> " + project.getCategory() + "</p>" +
                "      <p><strong>Location:</strong> " + project.getLocation() + "</p>" +
                "    </div>" +
                "    <h2>Join Request Details</h2>" +
                "    <div class='joiner-info'>" +
                "      <p><strong>Name:</strong> " + joiner.getName() + "</p>" +
                "      <p><strong>Email:</strong> " + joiner.getEmail() + "</p>" +
                "      <p><strong>Phone:</strong> " + joiner.getPhone() + "</p>" +
                "      <p><strong>Status:</strong> <span class='status-badge status-requested'>REQUESTED</span></p>" +
                "    </div>" +
                "    <p>Please review this request and take action by visiting your project dashboard.</p>" +
                "    <a href='https://origin-client.orinest.rw/dashboard/project/my-projects' class='cta-button'>View Project Dashboard</a>" +
                "  </div>" +
                "  <div class='footer'>" +
                "    <p>Need help? <a href='mailto:origin@group.com' style='color: #667eea;'>Contact Support</a></p>" +
                "    <p>&copy; 2025 Origin Group. All rights reserved.</p>" +
                "  </div>" +
                "</div>" +
                "</body>" +
                "</html>";
    }

    private String createJoinRequestResponseEmail(CommunityProject project, Users joiner, JoinedProject joinRequest, JoinStatus status, String reason) {
        String statusColor = status == JoinStatus.ACCEPTED ? "#d4edda" : "#f8d7da";
        String statusTextColor = status == JoinStatus.ACCEPTED ? "#155724" : "#721c24";
        String statusText = status == JoinStatus.ACCEPTED ? "ACCEPTED" : "DECLINED";
        String actionText = status == JoinStatus.ACCEPTED ? 
                "Congratulations! Your join request has been accepted." : 
                "We regret to inform you that your join request has been declined.";
        
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "  <meta charset='UTF-8'>" +
                "  <style>" +
                "    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; margin: 0; padding: 0; }" +
                "    .email-container { max-width: 600px; margin: 0 auto; background-color: #f9f9f9; }" +
                "    .header { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 30px; text-align: center; }" +
                "    .header h1 { margin: 0; font-size: 24px; }" +
                "    .content { background: white; padding: 30px; }" +
                "    .project-info { background: #f8f9fa; padding: 20px; border-radius: 8px; margin: 20px 0; }" +
                "    .status-info { background: " + statusColor + "; padding: 20px; border-radius: 8px; margin: 20px 0; color: " + statusTextColor + "; }" +
                "    .cta-button { display: inline-block; background: #667eea; color: white; padding: 12px 30px; text-decoration: none; border-radius: 5px; margin: 20px 0; }" +
                "    .footer { background: #333; color: white; padding: 20px; text-align: center; font-size: 12px; }" +
                "    .status-badge { display: inline-block; padding: 5px 15px; border-radius: 20px; font-size: 12px; font-weight: bold; }" +
                "    .status-accepted { background: #d4edda; color: #155724; }" +
                "    .status-declined { background: #f8d7da; color: #721c24; }" +
                "  </style>" +
                "</head>" +
                "<body>" +
                "<div class='email-container'>" +
                "  <div class='header'>" +
                "    <h1>Join Request Update</h1>" +
                "    <p>Your join request status has been updated</p>" +
                "  </div>" +
                "  <div class='content'>" +
                "    <h2>Project Details</h2>" +
                "    <div class='project-info'>" +
                "      <h3>" + project.getProjectName() + "</h3>" +
                "      <p><strong>Category:</strong> " + project.getCategory() + "</p>" +
                "      <p><strong>Location:</strong> " + project.getLocation() + "</p>" +
                "      <p><strong>Team:</strong> " + joinRequest.getJoinedTeam() + "</p>" +
                "    </div>" +
                "    <div class='status-info'>" +
                "      <h3>Request Status: <span class='status-badge " + (status == JoinStatus.ACCEPTED ? "status-accepted" : "status-declined") + "'>" + statusText + "</span></h3>" +
                "      <p>" + actionText + "</p>" +
                (reason != null && !reason.trim().isEmpty() ? "<p><strong>Reason:</strong> " + reason + "</p>" : "") +
                "    </div>" +
                (status == JoinStatus.ACCEPTED ? 
                    "<p>You can now start collaborating on this project. Check your dashboard for more details.</p>" +
                    "<a href='https://origin-client.orinest.rw/dashboard/project/my-projects' class='cta-button'>View My Projects</a>" :
                    "<p>Don't worry! There are many other exciting projects you can join. Keep exploring our platform.</p>" +
                    "<a href='https://origin-client.orinest.rw/dashboard/project/my-projects' class='cta-button'>Explore More Projects</a>") +
                "  </div>" +
                "  <div class='footer'>" +
                "    <p>Need help? <a href='mailto:origin@group.com' style='color: #667eea;'>Contact Support</a></p>" +
                "    <p>&copy; 2025 Origin Group. All rights reserved.</p>" +
                "  </div>" +
                "</div>" +
                "</body>" +
                "</html>";
    }
}
