package com.org.group.controller.user;

import com.org.group.dto.LaunchProject.LaunchProjectDto;
import com.org.group.dto.analytics.AnalyticsResponseDto;
import com.org.group.dto.analytics.UserAnalyticsResponse;
import com.org.group.model.project.LaunchProject;
import com.org.group.model.Users;
import com.org.group.responses.project.BookmarkedProjectResponse;
import com.org.group.responses.project.LaunchProjectResponse;
import com.org.group.services.LaunchProject.BookmarkServices;
import com.org.group.services.LaunchProject.LaunchProjectServices;
import com.org.group.services.emailAndJwt.PlanFilterServices;
import com.org.group.services.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/client/project")
public class UserProjectController {

    private final LaunchProjectServices launchProjectService;
    private final UserService userService;
    private final PlanFilterServices planFilterServices;
    private final BookmarkServices bookmarkServices;

    public UserProjectController(LaunchProjectServices launchProjectService, UserService userService, PlanFilterServices planFilterServices, BookmarkServices bookmarkServices) {
        this.launchProjectService = launchProjectService;
        this.userService = userService;
        this.planFilterServices = planFilterServices;
        this.bookmarkServices = bookmarkServices;
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadProjectDetails(
            @RequestParam("userId") UUID userId,
            @Valid @RequestPart("projectDetails") LaunchProjectDto projectDto,
            @RequestPart(value = "businessPlan", required = false) MultipartFile businessPlan,
            @RequestPart(value = "businessIdeaDocument", required = false) MultipartFile businessIdeaDocument,
            @RequestPart(value = "projectPhoto", required = false) MultipartFile projectPhoto,
            @RequestPart(value = "incomeStatement", required = false) MultipartFile incomeStatement,
            @RequestPart(value = "cashFlow", required = false) MultipartFile cashFlow,
            @RequestPart(value = "balanceSheet" ,required = false) MultipartFile balanceSheet,
            @RequestPart(value = "pitchingVideo", required = false) MultipartFile pitchingVideo) {
        try {

            Users user = userService.getUserById(userId);
            if (user.getLaunchProjects().stream().anyMatch(project->projectDto.getProjectName().equals(project.getProjectName()))) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Project Name already exists.");
            }
            String highestPriorityPlan = planFilterServices.getPlanFiltered(user);

            if (highestPriorityPlan.equals("FREE")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("You are not authorized to Launch project. Upgrade your plan.");
            }

            launchProjectService.saveProjectWithFiles(
                    userId,
                    projectDto,
                    businessPlan,
                    businessIdeaDocument,
                    projectPhoto,
                    cashFlow,
                    incomeStatement,
                    balanceSheet,
                    pitchingVideo );
            String message = "Project uploaded successfully";
            return ResponseEntity.status(HttpStatus.CREATED).body(message);

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to upload files");
        }
    }

    @PutMapping("/update")
    public ResponseEntity<String> updateProjectDetails(
            @RequestParam("projectId") UUID projectId,
            @Valid @RequestPart("projectDetails") LaunchProjectDto projectDto,
            @RequestPart(value = "businessPlan", required = false) MultipartFile businessPlan,
            @RequestPart(value = "businessIdeaDocument", required = false) MultipartFile businessIdeaDocument,
            @RequestPart(value = "projectPhoto", required = false) MultipartFile projectPhoto,
            @RequestPart(value = "incomeStatement", required = false) MultipartFile incomeStatement,
            @RequestPart(value = "cashFlow", required = false) MultipartFile cashFlow,
            @RequestPart(value = "balanceSheet", required = false) MultipartFile balanceSheet,
            @RequestPart(value = "pitchingVideo", required = false) MultipartFile pitchingVideo) {
        try {
            launchProjectService.updateProjectWithFiles(
                    projectId,
                    projectDto,
                    businessPlan,
                    businessIdeaDocument,
                    projectPhoto,
                    pitchingVideo,
                    incomeStatement,
                    cashFlow,
                    balanceSheet
            );
            return ResponseEntity.ok("Project updated successfully");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to update files");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }




    //List of project user launched
    @GetMapping("/userId")
    public ResponseEntity<List<LaunchProjectResponse>> getUserProjects(@RequestParam("user_id") UUID userId) {
        List<LaunchProjectResponse> userProjects = launchProjectService.getProjectsByUserId(userId);
        return ResponseEntity.ok(userProjects);
    }
    //get project by id
    @GetMapping("/{projectId}")
    public ResponseEntity<LaunchProjectResponse> getProjectById(@PathVariable UUID projectId) {
        return ResponseEntity.ok(launchProjectService.getProjectById(projectId));
    }

    //all project launched
    @PreAuthorize("hasAnyRole('ADMIN','ANALYZER')")
    @GetMapping("/all")
    public ResponseEntity<List<LaunchProject>> getAllProjects() {
        return ResponseEntity.ok(launchProjectService.getAllProject());
    }


    //home display project for customer view


    //book marking project

    @PostMapping("/bookmark")
    public ResponseEntity<?> bookmarkProject(@RequestParam UUID userId, @RequestParam  UUID projectId) {
        bookmarkServices.bookmarkProject(userId, projectId);
        return ResponseEntity.ok("Project bookmarked successfully");

    }
    @DeleteMapping("/bookmark/remove")
    public ResponseEntity<?> removeBookmarkProject(@RequestParam UUID userId,@RequestParam UUID projectId) {
        bookmarkServices.deleteBookmark(userId, projectId);
        return ResponseEntity.ok("Project bookmarked removed");
    }
    @GetMapping("/bookmark")
    public ResponseEntity<List<BookmarkedProjectResponse>> getBookmarkedProjects(@RequestParam UUID userId){
       return bookmarkServices.getBookmarkedProjects(userId);
    }

    @GetMapping("/view/analytics")
    public ResponseEntity<ResponseEntity<UserAnalyticsResponse>> getAnalytics(@RequestParam UUID userId , @RequestParam UUID projectId){
        return ResponseEntity.ok(launchProjectService.getAnalyticsOfProject(userId,projectId));

    }

    //delete hole project







}