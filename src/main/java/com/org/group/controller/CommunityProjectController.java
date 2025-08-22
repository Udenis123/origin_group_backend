package com.org.group.controller;

import com.org.group.dto.LaunchProject.AnalyticStatus;
import com.org.group.dto.community.CommunityDto;
import com.org.group.model.project.CommunityProject;
import com.org.group.model.project.TeamMember;
import com.org.group.services.CommunityProjectService;
import com.org.group.services.UploadFileServices.CloudinaryService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/community/project")
@RequiredArgsConstructor
public class CommunityProjectController {

    private final CommunityProjectService communityProjectService;
    private final CloudinaryService cloudinaryService;

  @Operation(summary = "Create a new community project", description = "Allows users to submit a new community project with photo upload")
    @PostMapping
    public ResponseEntity<CommunityProject> createProject(
            @RequestParam UUID userId,
            @RequestBody CommunityDto project,
            @RequestPart(value = "projectPhoto") MultipartFile projectPhoto) throws IOException {
         String PhotoUrl = cloudinaryService.uploadProjectPhoto(projectPhoto);
         if (PhotoUrl == null) {
             return  ResponseEntity.badRequest().build();
         }
        CommunityProject savedProject = communityProjectService.createProject(userId ,project,PhotoUrl);
        return ResponseEntity.ok(savedProject);
    }

    @Operation(summary = "Get project by ID", description = "Retrieves detailed information about a specific community project")
    @GetMapping("/{id}")
    public ResponseEntity<CommunityProject> getProject(@PathVariable UUID id) {
        CommunityProject project = communityProjectService.getProjectById(id);
        return ResponseEntity.ok(project);
    }

    @Operation(summary = "Get all projects", description = "Retrieves a list of all community projects in the system")
    @GetMapping
    public ResponseEntity<List<CommunityProject>> getAllProjects() {
        List<CommunityProject> projects = communityProjectService.getAllProjects();
        return ResponseEntity.ok(projects);
    }

    @Operation(summary = "Update project", description = "Allows project creators to modify their project information")
    @PutMapping("/{id}")
    public ResponseEntity<CommunityProject> updateProject(@PathVariable UUID id, @RequestBody CommunityProject projectDetails) {
        CommunityProject updatedProject = communityProjectService.updateProject(id, projectDetails);
        return ResponseEntity.ok(updatedProject);
    }

    @Operation(summary = "Delete project", description = "Allows project creators to remove their project from the system")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProject(@PathVariable UUID id) {
        communityProjectService.deleteProject(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Add team member", description = "Allows project creators to add new team members to their project")
    @PostMapping("/{id}/team-members")
    public ResponseEntity<CommunityProject> addTeamMember(@PathVariable UUID id, @RequestBody TeamMember teamMember) {
        CommunityProject updatedProject = communityProjectService.addTeamMember(id, teamMember);
        return ResponseEntity.ok(updatedProject);
    }

    @Operation(summary = "Get projects by user", description = "Retrieves all projects created by a specific user")
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<CommunityProject>> getProjectsByUser(@PathVariable UUID userId) {
        try {
            List<CommunityProject> projects = communityProjectService.getProjectsByUser(userId);
            return ResponseEntity.ok(projects);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @Operation(summary = "Get pending projects", description = "Retrieves all community projects with PENDING status")
    @GetMapping("/pending")
    public ResponseEntity<List<CommunityProject>> getPendingProjects() {
        List<CommunityProject> pendingProjects = communityProjectService.getProjectsByStatus(AnalyticStatus.PENDING);
        return ResponseEntity.ok(pendingProjects);
    }

    @Operation(summary = "Update team member", description = "Updates a specific team member at the given index in a project")
    @PutMapping("/{id}/team-members/{index}")
    public ResponseEntity<CommunityProject> updateTeamMember(
            @PathVariable UUID id, 
            @PathVariable int index, 
            @RequestBody TeamMember teamMember) {
        try {
            CommunityProject updatedProject = communityProjectService.updateTeamMember(id, index, teamMember);
            return ResponseEntity.ok(updatedProject);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @Operation(summary = "Delete team member", description = "Deletes a specific team member at the given index from a project")
    @DeleteMapping("/{id}/team-members/{index}")
    public ResponseEntity<CommunityProject> deleteTeamMember(
            @PathVariable UUID id, 
            @PathVariable int index) {
        try {
            CommunityProject updatedProject = communityProjectService.deleteTeamMember(id, index);
            return ResponseEntity.ok(updatedProject);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }
}
