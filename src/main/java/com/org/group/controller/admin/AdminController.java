package com.org.group.controller.admin;


import com.org.group.dto.OrderedProject.ProjectDeclineDto;
import com.org.group.dto.admin.AnalyzerDto;
import com.org.group.dto.admin.AnalyzerInfoDto;
import com.org.group.dto.analytics.AnalyticsResponseDto;
import com.org.group.dto.userAuth.LoginUserDto;
import com.org.group.dto.userResponse.UserRatingResponse;
import com.org.group.model.analyzer.Analyzer;
import com.org.group.responses.LoginResponseAn;
import com.org.group.responses.project.LaunchProjectResponse;
import com.org.group.responses.project.LaunchedProjectAnalyticsResponse;
import com.org.group.responses.project.OrderedProjectResponse;
import com.org.group.role.Role;
import com.org.group.services.Admin.AdminServices;
import com.org.group.services.Analyzer.AnalyzerServices;
import com.org.group.services.OrderedProject.OrderedProjectServices;
import com.org.group.services.emailAndJwt.JwtService;
import com.org.group.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@Tag(name = "Admin controller", description = "Admin management APIs")
@RequestMapping("/admin")
@CrossOrigin("http://localhost:4201")
public class AdminController {
    private final JwtService jwtService;
    private final AdminServices adminServices;
    private final UserService userService;
    private final OrderedProjectServices orderedProjectServices;
    private final AnalyzerServices analyzerServices;
    
    @Autowired
    public AdminController(JwtService jwtService, AdminServices adminServices, UserService userService, OrderedProjectServices orderedProjectServices, AnalyzerServices analyzerServices) {
        this.jwtService = jwtService;
        this.adminServices = adminServices;
        this.userService = userService;
        this.orderedProjectServices = orderedProjectServices;
        this.analyzerServices = analyzerServices;
    }


    @PostMapping("/register/analyzer")
    private ResponseEntity<?> RegisterAnalyzer( @Valid @RequestBody AnalyzerDto analyzerDto){
        adminServices.registerAnalyzer(analyzerDto);
       return ResponseEntity.accepted().build();
    }


    @Operation(
            summary = "Authenticate admin or analyzer ",
            description = "Login with email and password to get JWT token"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully authenticated"),
            @ApiResponse(responseCode = "500", description = "Invalid credentials")
    })
    @PostMapping("/login")
    public ResponseEntity<LoginResponseAn> login(@RequestBody LoginUserDto input) {
        Analyzer authenticated = adminServices.authenticate(input);
        String jwtToken;
        UUID userId;
        jwtToken = jwtService.generateToken(authenticated);
        userId = authenticated.getId();
        return ResponseEntity.ok(LoginResponseAn.builder()
                .token(jwtToken)
                .id(userId)
                .expiresIn(jwtService.getExpirationTime())
                .build());
    }

    @PostMapping("/enable/analyzer")
    public String enableOrDisableAnalyzerAccount(@RequestPart("analyzerId") UUID analyzerId) {
        return adminServices.enableOrDisableAnalyzer(analyzerId);
    }


    @Operation(
            summary = "Authenticate admin only not other user",
            description = "Login with email and password to get JWT token"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully authenticated"),
            @ApiResponse(responseCode = "500", description = "Invalid credentials")
    })
    @PostMapping("/login/admin")
    public ResponseEntity<?> loginAdmin(@RequestBody LoginUserDto input) {
        Analyzer authenticated = adminServices.authenticate(input);
        // Check if the authenticated user has the ADMIN role
        if (authenticated.getRoles() == null || !authenticated.getRoles().contains(Role.ADMIN)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied: Not an admin");
        }
        String jwtToken = jwtService.generateToken(authenticated);
        UUID userId = authenticated.getId();
        return ResponseEntity.ok(LoginResponseAn.builder()
                .token(jwtToken)
                .id(userId)
                .expiresIn(jwtService.getExpirationTime())
                .build());
    }
    


    @GetMapping("/all/launch/project")
    public ResponseEntity<List<LaunchProjectResponse>> getAllPendingProjects() {
        return  adminServices.getAllProject();
    }

    @GetMapping("/project/see/analytics")
    public ResponseEntity<AnalyticsResponseDto> getAllAnalytics(@RequestParam("projectId") UUID projectId) {
        AnalyticsResponseDto analytics = adminServices.getAnalyticsOfProject(projectId);
        return ResponseEntity.ok(analytics);
    }

    @PostMapping("/project/approve")
    public  ResponseEntity<?> approveProject(@RequestParam("projectId") UUID projectId) {
        return ResponseEntity.ok(adminServices.approveProject(projectId));
    }

    @GetMapping("/project/pending/analytics")
    public ResponseEntity<List<LaunchedProjectAnalyticsResponse>> getAnalyticsMarkedUsComplete() {
        return ResponseEntity.ok(adminServices.getAllPendingAndHaveAnalytics().getBody());
    }
    @GetMapping("/pending/project")
    public ResponseEntity<List<LaunchProjectResponse>> getPendingProjects() {
        return ResponseEntity.ok(adminServices.getAllPendingProject().getBody());
    }

    @Operation(
            summary = "Get all ratings",
            description = "Retrieve all user ratings for admin review"
    )
    @GetMapping("/ratings")
    public ResponseEntity<List<UserRatingResponse>> getAllRatings() {
        List<UserRatingResponse> response = userService.getAllPendingRatings();

        return ResponseEntity.ok(response);
    }
    
    @Operation(
            summary = "Approve rating",
            description = "Approve a user rating for display on the site"
    )
    @PostMapping("/ratings/approve/{ratingId}")
    public ResponseEntity<String> approveRating(@PathVariable Long ratingId) {
        return ResponseEntity.ok(userService.approveRating(ratingId));
    }
    
    @Operation(
            summary = "Disapprove rating",
            description = "Disapprove or hide a user rating from display"
    )
    @PostMapping("/ratings/disapprove/{ratingId}")
    public ResponseEntity<String> disapproveRating(@PathVariable Long ratingId) {
        return ResponseEntity.ok(userService.disapproveRating(ratingId));
    }

    // ordered project review

    @GetMapping("/ordered_project/pending")
    public ResponseEntity<List<OrderedProjectResponse>> getOrderedProjectsPending() {
       return ResponseEntity.ok(orderedProjectServices.getAllOrderedProjectsPending());
    }
    @GetMapping("/ordered_project/approved")
    public ResponseEntity<List<OrderedProjectResponse>> getOrderedProjectsApproved() {
        return ResponseEntity.ok(orderedProjectServices.getAllOrderedProjectsApproved());
    }
    @PostMapping("/approve/ordered_project")
    public ResponseEntity<?> approveOrderedProject (@RequestParam("projectId") UUID projectId) {
        return ResponseEntity.ok(orderedProjectServices.approveOrderedProject(projectId));
    }
    @PostMapping("/reject/ordered_project")
    public ResponseEntity<?> approveOrderedProject (@RequestBody ProjectDeclineDto input) {
        return ResponseEntity.ok(orderedProjectServices.rejectOrderedProject(input.getProjectId(),input.getReason()));
    }

    @Operation(
            summary = "Get all analyzers",
            description = "Fetch all analyzer personal information for admin review"
    )
    @GetMapping("/analyzers")
    public ResponseEntity<List<AnalyzerInfoDto>> getAllAnalyzers() {
        List<AnalyzerInfoDto> analyzers = adminServices.getAllAnalyzersInfo();
        return ResponseEntity.ok(analyzers);
    }

    @Operation(
            summary = "Get analyzer by ID",
            description = "Fetch specific analyzer details by their ID"
    )
    @GetMapping("/analyzer/{analyzerId}")
    public ResponseEntity<Analyzer> getAnalyzerById(@PathVariable UUID analyzerId) {
        Analyzer analyzer = adminServices.getAnalyzerById(analyzerId);
        return ResponseEntity.ok(analyzer);
    }

    @Operation(
            summary = "Assign project to analyzer",
            description = "Assign a specific project to an analyzer for analysis"
    )
    @PostMapping("/assign-project")
    public ResponseEntity<String> assignProjectToAnalyzer(
            @RequestParam("projectId") UUID projectId,
            @RequestParam("analyzerId") UUID analyzerId) {
        try {
            analyzerServices.assignProject(projectId, analyzerId);
            return ResponseEntity.ok("Project assigned to analyzer successfully");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to assign project: " + e.getMessage());
        }
    }

}
