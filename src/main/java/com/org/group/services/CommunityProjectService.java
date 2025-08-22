package com.org.group.services;

import com.org.group.dto.LaunchProject.AnalyticStatus;
import com.org.group.dto.community.CommunityDto;
import com.org.group.model.Users;
import com.org.group.model.project.CommunityProject;
import com.org.group.model.project.TeamMember;
import com.org.group.repository.UserRepository;
import com.org.group.repository.project.CommunityProjectRepository;
import com.org.group.services.UploadFileServices.CloudinaryService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CommunityProjectService {


    private final CommunityProjectRepository communityProjectRepository;
    private final UserRepository userRepository;
    private final CloudinaryService cloudinaryService;

    public CommunityProject createProject(UUID userId, CommunityDto project, String photoUrl) throws IOException {
        Users users = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("User not found"));


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

        return communityProjectRepository.save(communityProject);
    }


    public CommunityProject getProjectById(UUID id) {
        return communityProjectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found"));
    }

    public List<CommunityProject> getAllProjects() {
        return communityProjectRepository.findAll();
    }

    public CommunityProject updateProject(UUID id, CommunityProject projectDetails) {
        CommunityProject project = getProjectById(id);
        
        // Update fields
        project.setFullName(projectDetails.getFullName());
        project.setProfession(projectDetails.getProfession());
        project.setEmail(projectDetails.getEmail());
        project.setPhone(projectDetails.getPhone());
        project.setLinkedIn(projectDetails.getLinkedIn());
        project.setProjectPhoto(projectDetails.getProjectPhoto());
        project.setProjectName(projectDetails.getProjectName());
        project.setCategory(projectDetails.getCategory());
        project.setLocation(projectDetails.getLocation());
        project.setDescription(projectDetails.getDescription());
        project.setTeam(projectDetails.getTeam()); // This will be stored as JSON
        
        return communityProjectRepository.save(project);
    }

    public void deleteProject(UUID id) {
        communityProjectRepository.deleteById(id);
    }

    // Example method to add a team member to an existing project
    public CommunityProject addTeamMember(UUID projectId, TeamMember teamMember) {
        CommunityProject project = getProjectById(projectId);
        List<TeamMember> team = project.getTeam();
        team.add(teamMember);
        project.setTeam(team);
        return communityProjectRepository.save(project);
    }

    // Get projects by user
    public List<CommunityProject> getProjectsByUser(UUID userId) {
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
        return communityProjectRepository.findByUser(user);
    }

    // Get projects by status
    public List<CommunityProject> getProjectsByStatus(AnalyticStatus status) {
        return communityProjectRepository.findByStatus(status);
    }

    // Update team member
    public CommunityProject updateTeamMember(UUID projectId, int teamMemberIndex, TeamMember updatedMember) {
        CommunityProject project = getProjectById(projectId);
        List<TeamMember> team = project.getTeam();
        
        if (teamMemberIndex >= 0 && teamMemberIndex < team.size()) {
            team.set(teamMemberIndex, updatedMember);
            project.setTeam(team);
            return communityProjectRepository.save(project);
        } else {
            throw new IllegalArgumentException("Invalid team member index: " + teamMemberIndex);
        }
    }

    // Delete team member
    public CommunityProject deleteTeamMember(UUID projectId, int teamMemberIndex) {
        CommunityProject project = getProjectById(projectId);
        List<TeamMember> team = project.getTeam();
        
        if (teamMemberIndex >= 0 && teamMemberIndex < team.size()) {
            team.remove(teamMemberIndex);
            project.setTeam(team);
            return communityProjectRepository.save(project);
        } else {
            throw new IllegalArgumentException("Invalid team member index: " + teamMemberIndex);
        }
    }

    // Approve community project
    public CommunityProject approveProject(UUID projectId) {
        CommunityProject project = getProjectById(projectId);
        project.setStatus(AnalyticStatus.APPROVED);
        return communityProjectRepository.save(project);
    }

    // Cancel/Reject community project with reason
    public CommunityProject cancelProject(UUID projectId, String reason) {
        CommunityProject project = getProjectById(projectId);
        project.setStatus(AnalyticStatus.DECLINED);
        // You might want to add a reason field to the CommunityProject entity
        // For now, we'll just set the status to DECLINED
        return communityProjectRepository.save(project);
    }

}
