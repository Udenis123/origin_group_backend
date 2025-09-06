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
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class JoinProjectService {

    private final JoinRepository joinRepository;
    private final CommunityProjectRepository communityProjectRepository;
    private final UserRepository userRepository;

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
                .filter(joinRequest -> joinRequest.getStatus() == JoinStatus.REJECTED)
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
            
            log.info("Join request accepted for user {} to project {}", joinRequest.getUserId(), joinRequest.getCommunityProjectId());
        } else if (actionDto.getAction() == JoinStatus.REJECTED) {
            // Reject the request
            joinRequest.setStatus(JoinStatus.REJECTED);
            
            // If the request was previously accepted, increase team member count back
            if (originalStatus == JoinStatus.ACCEPTED) {
                increaseTeamMemberCount(project, joinRequest.getJoinedTeam());
            }
            
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
}
