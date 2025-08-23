package com.org.group.controller;

import com.org.group.dto.LaunchProject.AnalyticStatus;
import com.org.group.dto.community.CommunityDto;
import com.org.group.dto.community.CommunityResponseDto;
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
    @PostMapping(consumes = "multipart/form-data")
     public ResponseEntity<CommunityResponseDto> createProject(
             @RequestParam("userId") UUID userId,
             @RequestPart("project") CommunityDto project,
             @RequestPart("projectPhoto") MultipartFile projectPhoto) throws IOException {
          String PhotoUrl = cloudinaryService.uploadProjectPhoto(projectPhoto);
          if (PhotoUrl == null) {
              return  ResponseEntity.badRequest().build();
          }
         CommunityResponseDto savedProject = communityProjectService.createProject(userId ,project,PhotoUrl);
         return ResponseEntity.status(HttpStatus.CREATED).body(savedProject);
     }

    @Operation(summary = "Get project by ID", description = "Retrieves detailed information about a specific community project")
    @GetMapping("/{id}")
    public ResponseEntity<CommunityResponseDto> getProject(@PathVariable UUID id) {
        CommunityResponseDto project = communityProjectService.getProjectById(id);
        return ResponseEntity.ok(project);
    }

    @Operation(summary = "Get all projects", description = "Retrieves a list of all community projects in the system")
    @GetMapping
    public ResponseEntity<List<CommunityResponseDto>> getAllProjects() {
        List<CommunityResponseDto> projects = communityProjectService.getAllProjects();
        return ResponseEntity.ok(projects);
    }

    @Operation(summary = "Update project", description = "Allows project creators to modify their project information")
    @PutMapping("/{id}")
    public ResponseEntity<CommunityResponseDto> updateProject(@PathVariable UUID id, @RequestBody CommunityProject projectDetails) {
        CommunityResponseDto updatedProject = communityProjectService.updateProject(id, projectDetails);
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
    public ResponseEntity<CommunityResponseDto> addTeamMember(@PathVariable UUID id, @RequestBody TeamMember teamMember) {
        CommunityResponseDto updatedProject = communityProjectService.addTeamMember(id, teamMember);
        return ResponseEntity.ok(updatedProject);
    }

    @Operation(summary = "Get projects by user", description = "Retrieves all projects created by a specific user")
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<CommunityResponseDto>> getProjectsByUser(@PathVariable UUID userId) {
        try {
            List<CommunityResponseDto> projects = communityProjectService.getProjectsByUser(userId);
            return ResponseEntity.ok(projects);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @Operation(summary = "Get pending projects", description = "Retrieves all community projects with PENDING status")
    @GetMapping("/pending")
    public ResponseEntity<List<CommunityResponseDto>> getPendingProjects() {
        List<CommunityResponseDto> pendingProjects = communityProjectService.getProjectsByStatus(AnalyticStatus.PENDING);
        return ResponseEntity.ok(pendingProjects);
    }

    @Operation(summary = "Update team member", description = "Updates a specific team member at the given index in a project")
    @PutMapping("/{id}/team-members/{index}")
    public ResponseEntity<CommunityResponseDto> updateTeamMember(
            @PathVariable UUID id, 
            @PathVariable int index, 
            @RequestBody TeamMember teamMember) {
        try {
            CommunityResponseDto updatedProject = communityProjectService.updateTeamMember(id, index, teamMember);
            return ResponseEntity.ok(updatedProject);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @Operation(summary = "Delete team member", description = "Deletes a specific team member at the given index from a project")
    @DeleteMapping("/{id}/team-members/{index}")
    public ResponseEntity<CommunityResponseDto> deleteTeamMember(
            @PathVariable UUID id, 
            @PathVariable int index) {
        try {
            CommunityResponseDto updatedProject = communityProjectService.deleteTeamMember(id, index);
            return ResponseEntity.ok(updatedProject);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }
}
