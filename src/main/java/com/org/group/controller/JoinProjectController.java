package com.org.group.controller;

import com.org.group.dto.community.*;
import com.org.group.services.JoinProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/join-project")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Join Project", description = "APIs for joining community projects and managing join requests")
public class JoinProjectController {

    private final JoinProjectService joinProjectService;

    @PostMapping("/join/{userId}")
    @Operation(summary = "Join a community project", description = "Submit a request to join a community project with a specific team")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Join request submitted successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "404", description = "Project or user not found"),
            @ApiResponse(responseCode = "409", description = "User already has a join request for this project")
    })
    public ResponseEntity<JoinProjectResponseDto> joinProject(
            @PathVariable UUID userId,
            @Valid @RequestBody JoinProjectRequestDto requestDto) {
        
        log.info("User {} attempting to join project {}", userId, requestDto.getCommunityProjectId());
        
        JoinProjectResponseDto response = joinProjectService.joinProject(userId, requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/project/{projectId}/members")
    @Operation(summary = "Get joined members by project ID", description = "Retrieve all members who have joined or requested to join a specific project")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Members retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Project not found")
    })
    public ResponseEntity<List<JoinedMemberResponseDto>> getJoinedMembersByProjectId(
            @Parameter(description = "Community project ID") @PathVariable UUID projectId) {
        
        log.info("Retrieving joined members for project {}", projectId);
        List<JoinedMemberResponseDto> members = joinProjectService.getJoinedMembersByProjectId(projectId);
        return ResponseEntity.ok(members);
    }

    @PutMapping("/handle-request")
    @Operation(summary = "Accept or decline join request", description = "Accept or decline a join request for a community project")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Request handled successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "404", description = "Join request not found"),
            @ApiResponse(responseCode = "409", description = "Request already processed")
    })
    public ResponseEntity<JoinProjectResponseDto> handleJoinRequest(
            @Valid @RequestBody JoinRequestActionDto actionDto) {
        
        log.info("Handling join request {} with action {}", actionDto.getJoinId(), actionDto.getAction());
        JoinProjectResponseDto response = joinProjectService.handleJoinRequest(actionDto);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/my-requests/{userId}")
    @Operation(summary = "Get user's join requests", description = "Retrieve all join requests made by a specific user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Requests retrieved successfully")
    })
    public ResponseEntity<List<JoinedMemberResponseDto>> getMyJoinRequests(
            @Parameter(description = "User ID") @PathVariable UUID userId) {
        log.info("Retrieving join requests for user {}", userId);
        
        List<JoinedMemberResponseDto> requests = joinProjectService.getMyJoinRequests(userId);
        return ResponseEntity.ok(requests);
    }

    @GetMapping("/joined-projects/{userId}")
    @Operation(summary = "Get community projects user has joined", description = "Retrieve all community projects that a user has joined with join details (team, description, status)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Joined projects retrieved successfully")
    })
    public ResponseEntity<List<JoinedProjectWithDetailsDto>> getJoinedProjects(
            @Parameter(description = "User ID") @PathVariable UUID userId) {
        log.info("Retrieving joined projects for user {}", userId);
        
        List<JoinedProjectWithDetailsDto> projects = joinProjectService.getJoinedProjects(userId);
        return ResponseEntity.ok(projects);
    }

}
