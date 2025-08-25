package com.org.group.services;

import com.org.group.dto.LaunchProject.AnalyticStatus;
import com.org.group.dto.community.CommunityDto;
import com.org.group.dto.community.CommunityResponseDto;
import com.org.group.dto.community.CommunityUpdateDto;
import com.org.group.model.Users;
import com.org.group.model.project.CommunityProject;
import com.org.group.model.project.TeamMember;
import com.org.group.repository.UserRepository;
import com.org.group.repository.project.CommunityProjectRepository;
import com.org.group.services.UploadFileServices.CloudinaryService;
import com.org.group.services.emailAndJwt.PlanFilterServices;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommunityProjectService {


    private final CommunityProjectRepository communityProjectRepository;
    private final UserRepository userRepository;
    private final CloudinaryService cloudinaryService;
    private final PlanFilterServices planFilterServices;

    public CommunityResponseDto createProject(UUID userId, CommunityDto project, String photoUrl) throws IOException {
        Users users = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("User not found"));

        String userSubscription = planFilterServices.getPlanFiltered(users);
        if(userSubscription.equals("BASIC") || userSubscription.equals("FREE")) {
            throw new RuntimeException("Please upgrade your Subscription");
        }

        CommunityProject communityProject = CommunityProject.builder()
                .email(project.getEmail())
                .fullName(project.getFullName())
                .description(project.getDescription())
                .phone(project.getPhone())
                .projectPhoto(photoUrl)
                .category(project.getCategory())
                .linkedIn(project.getLinkedIn())
                .status(AnalyticStatus.PENDING)
                .profession(project.getProfession())
                .projectName(project.getProjectName())
                .user(users)
                .location(project.getLocation())
                .team(project.getTeam())
                .build();
        CommunityProject savedProject = communityProjectRepository.save(communityProject);

        return convertToResponseDto(savedProject);
    }

    // Convert CommunityProject entity to CommunityResponseDto
    private CommunityResponseDto convertToResponseDto(CommunityProject project) {
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
                .userId(project.getUser() != null ? project.getUser().getId() : null)
                .build();
    }

    public CommunityResponseDto getProjectById(UUID id) {
        CommunityProject project = communityProjectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found"));
        return convertToResponseDto(project);
    }

    public List<CommunityResponseDto> getAllProjects() {
        return communityProjectRepository.findAll()
                .stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }

    public CommunityResponseDto updateProject(UUID id, CommunityUpdateDto projectDetails) {
        CommunityProject project = communityProjectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found"));
        if (project.getStatus() == AnalyticStatus.APPROVED || project.getStatus() == AnalyticStatus.PRODUCTION || project.getStatus() == AnalyticStatus.DECLINED || project.getStatus() == AnalyticStatus.COMPLETED) {
            throw new RuntimeException("project must update while is pending, query, or pending_query");
        }
        
        // Update fields
        project.setFullName(projectDetails.getFullName());
        project.setProfession(projectDetails.getProfession());
        project.setEmail(projectDetails.getEmail());
        project.setPhone(projectDetails.getPhone());
        project.setLinkedIn(projectDetails.getLinkedIn());
        project.setProjectName(projectDetails.getProjectName());
        project.setCategory(projectDetails.getCategory());
        project.setLocation(projectDetails.getLocation());
        project.setDescription(projectDetails.getDescription());
        
        CommunityProject updatedProject = communityProjectRepository.save(project);
        return convertToResponseDto(updatedProject);
    }

    public CommunityResponseDto updateProjectPhoto(UUID projectId, MultipartFile newPhoto) throws IOException {
        CommunityProject project = communityProjectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));
        
        if (project.getStatus() == AnalyticStatus.APPROVED || project.getStatus() == AnalyticStatus.PRODUCTION || project.getStatus() == AnalyticStatus.DECLINED || project.getStatus() == AnalyticStatus.COMPLETED) {
            throw new RuntimeException("project must update while is pending, query, or pending_query");
        }

        // Delete existing photo if it exists
        if (project.getProjectPhoto() != null && !project.getProjectPhoto().isEmpty()) {
            cloudinaryService.deleteFile(project.getProjectPhoto());
        }

        // Upload new photo
        String newPhotoUrl = cloudinaryService.uploadProjectPhoto(newPhoto);
        if (newPhotoUrl == null) {
            throw new RuntimeException("Failed to upload new photo");
        }

        // Update project with new photo URL
        project.setProjectPhoto(newPhotoUrl);
        CommunityProject updatedProject = communityProjectRepository.save(project);
        
        return convertToResponseDto(updatedProject);
    }

    public void deleteProject(UUID id) {
        communityProjectRepository.deleteById(id);
    }

    // Example method to add a team member to an existing project
    public CommunityResponseDto addTeamMember(UUID projectId, TeamMember teamMember) {

        CommunityProject project = communityProjectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));
        if (project.getStatus() == AnalyticStatus.APPROVED || project.getStatus() == AnalyticStatus.PRODUCTION || project.getStatus() == AnalyticStatus.DECLINED || project.getStatus() == AnalyticStatus.COMPLETED) {
            throw new RuntimeException("project must update while is pending, query, or pending_query");
        }
        List<TeamMember> team = project.getTeam();
        team.add(teamMember);
        project.setTeam(team);
        CommunityProject savedProject = communityProjectRepository.save(project);
        return convertToResponseDto(savedProject);
    }

    // Get projects by user
    public List<CommunityResponseDto> getProjectsByUser(UUID userId) {
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
        return communityProjectRepository.findByUser(user)
                .stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }

    // Get projects by status
    public List<CommunityResponseDto> getProjectsByStatus(AnalyticStatus status) {
        return communityProjectRepository.findByStatus(status)
                .stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }

    // Update team member
    public CommunityResponseDto updateTeamMember(UUID projectId, int teamMemberIndex, TeamMember updatedMember) {

        CommunityProject project = communityProjectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        if (project.getStatus() == AnalyticStatus.APPROVED || project.getStatus() == AnalyticStatus.PRODUCTION || project.getStatus() == AnalyticStatus.DECLINED || project.getStatus() == AnalyticStatus.COMPLETED) {
            throw new RuntimeException("project must update while is pending, query, or pending_query");
        }
        List<TeamMember> team = project.getTeam();
        
        if (teamMemberIndex >= 0 && teamMemberIndex < team.size()) {
            team.set(teamMemberIndex, updatedMember);
            project.setTeam(team);
            CommunityProject savedProject = communityProjectRepository.save(project);
            return convertToResponseDto(savedProject);
        } else {
            throw new IllegalArgumentException("Invalid team member index: " + teamMemberIndex);
        }
    }

    // Delete team member
    public CommunityResponseDto deleteTeamMember(UUID projectId, int teamMemberIndex) {
        CommunityProject project = communityProjectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));
        if (project.getStatus() == AnalyticStatus.APPROVED || project.getStatus() == AnalyticStatus.PRODUCTION || project.getStatus() == AnalyticStatus.DECLINED || project.getStatus() == AnalyticStatus.COMPLETED) {
            throw new RuntimeException("project must update while is pending, query, or pending_query");
        }
        List<TeamMember> team = project.getTeam();
        
        if (teamMemberIndex >= 0 && teamMemberIndex < team.size()) {
            team.remove(teamMemberIndex);
            project.setTeam(team);
            CommunityProject savedProject = communityProjectRepository.save(project);
            return convertToResponseDto(savedProject);
        } else {
            throw new IllegalArgumentException("Invalid team member index: " + teamMemberIndex);
        }
    }

    // Approve community project
    public CommunityResponseDto approveProject(UUID projectId) {
        CommunityProject project = communityProjectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));
        project.setStatus(AnalyticStatus.APPROVED);
        CommunityProject savedProject = communityProjectRepository.save(project);
        return convertToResponseDto(savedProject);
    }

    // Cancel/Reject community project with reason
    public CommunityResponseDto cancelProject(UUID projectId, String reason) {
        CommunityProject project = communityProjectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));
        project.setStatus(AnalyticStatus.DECLINED);
        project.setReason(reason);
        CommunityProject savedProject = communityProjectRepository.save(project);
        return convertToResponseDto(savedProject);
    }

    // Set project to QUERY status (admin action)
    public CommunityResponseDto setProjectToQuery(UUID projectId, String reason) {
        CommunityProject project = communityProjectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));
        project.setStatus(AnalyticStatus.QUERY);
        project.setReason(reason);
        CommunityProject savedProject = communityProjectRepository.save(project);
        return convertToResponseDto(savedProject);
    }

    // Set project to PENDING_QUERY status (when user resubmits after query)
    public CommunityResponseDto setProjectToPendingQuery(UUID projectId) {

        CommunityProject project = communityProjectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));
        if (project.getStatus() == AnalyticStatus.APPROVED || project.getStatus() == AnalyticStatus.PRODUCTION || project.getStatus() == AnalyticStatus.DECLINED || project.getStatus() == AnalyticStatus.COMPLETED) {
            throw new RuntimeException("project must update while is pending, queried");
        }
        project.setStatus(AnalyticStatus.PENDING_QUERY);
        project.setReason(null); // Clear the reason when resubmitting
        CommunityProject savedProject = communityProjectRepository.save(project);
        return convertToResponseDto(savedProject);
    }

}
