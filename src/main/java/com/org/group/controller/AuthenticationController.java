package com.org.group.controller;

import com.org.group.dto.community.CommunityResponseDto;
import com.org.group.dto.userAuth.LoginUserDto;
import com.org.group.responses.RegisterResponse;
import com.org.group.dto.userAuth.RegisterUserDto;
import com.org.group.dto.userAuth.ResetPasswordDto;
import com.org.group.model.Users;
import com.org.group.responses.LoginResponse;
import com.org.group.responses.project.HomeProjectResponse;
import com.org.group.services.Admin.AdminServices;
import com.org.group.services.AuthenticationServices;
import com.org.group.services.CommunityProjectService;
import com.org.group.services.LaunchProject.LaunchProjectServices;

import com.org.group.services.emailAndJwt.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import com.org.group.subscription.SubscriptionPlan;
import com.org.group.dto.userResponse.UserRatingResponse;
import com.org.group.model.UserRatting;
import com.org.group.services.UserService;

import java.util.List;
import java.util.UUID;

@Tag(name = "Authentication", description = "Authentication management APIs")
@RequestMapping("/auth")
@RestController
@RequiredArgsConstructor
@CrossOrigin("http://localhost:4202")
public class AuthenticationController {
    private  JwtService jwtService;
    private  AuthenticationServices authenticationService;
    private final LaunchProjectServices launchProjectService;
    private final AdminServices adminServices;
    private final UserService userService;
    private final CommunityProjectService communityProjectService;

    @Autowired
    public AuthenticationController(AuthenticationServices authenticationService, JwtService jwtService, LaunchProjectServices launchProjectService, AdminServices adminServices, UserService userService, CommunityProjectService communityProjectService) {
        this.authenticationService = authenticationService;
        this.jwtService = jwtService;

        this.launchProjectService = launchProjectService;
        this.adminServices = adminServices;
        this.userService = userService;
        this.communityProjectService = communityProjectService;
    }

    @Operation(
        summary = "Register new user",
        description = "Register a new user with the provided details"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully registered"),
        @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @PostMapping("/signup")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterUserDto registerUserDto) {
        Users registeredUser = authenticationService.signup(registerUserDto);
        RegisterResponse registerResponse = RegisterResponse.builder().id(registeredUser.getId())
                .email(registeredUser.getEmail())
                .plan(List.of(registeredUser.getSubscriptions().stream()
                    .findFirst()
                    .map(sub -> sub.getPlan())
                    .orElse(SubscriptionPlan.FREE)))
                .message("Registration success full !!")
                .build();
        return ResponseEntity.ok(registerResponse);
    }

    @Operation(
        summary = "Authenticate user",
        description = "Login with email and password to get JWT token"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully authenticated"),
        @ApiResponse(responseCode = "500", description = "Invalid credentials")
    })
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginUserDto input) {
        Users authenticated = authenticationService.authenticate(input);
        String jwtToken;
        UUID userId = null;
            jwtToken = jwtService.generateToken(authenticated);
            userId = authenticated.getId();
        return ResponseEntity.ok(LoginResponse.builder()
                .token(jwtToken)
                .id(userId)
                .expiresIn(jwtService.getExpirationTime())
                .build());
    }
    @GetMapping("/test")
    public String Hello(){
        return "Hello World";
    }


    @Scheduled(fixedRate = 40000) // Run every 30 seconds (30000 milliseconds)
    public void test() {
        System.out.println("Scheduled task running: Hello World - " + java.time.LocalDateTime.now());
    }



    @Operation(
        summary = "Verify user account",
        description = "Verify user account with verification code"
    )
    @PostMapping("/verify/{code}")
    public ResponseEntity<?> verifyUser(@PathVariable("code") String code) {
        try {
            authenticationService.verifyUser(code);
            return ResponseEntity.ok("Account verified successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(
        summary = "Resend verification code",
        description = "Resend verification code to user's email"
    )
    @PostMapping("/resend/{email}")
    public ResponseEntity<?> resendVerificationCode(@PathVariable("email") String email) {
        try {
            authenticationService.resendVerificationCode(email);
            return ResponseEntity.ok("Verification code sent");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    //forgot password
    @Operation(
            summary = "Request password reset",
            description = "Send verification code to user's email for password reset"
    )
    @PostMapping("/forgot-password/{email}")
    public ResponseEntity<?> forgotPassword(@PathVariable("email") String email) {
        try {
            authenticationService.sendPasswordResetCode(email);
            return ResponseEntity.ok("Password reset code sent");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(
            summary = "Reset password",
            description = "Reset user's password with verification code and new password"
    )
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordDto resetPasswordDto) {
        try {

            authenticationService.resetPassword(resetPasswordDto);
            return ResponseEntity.ok("Password reset successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    @GetMapping("/forgot/check/{code}")
    public ResponseEntity<?> VerifyRequest(@PathVariable("code") String code) {
        try {
            authenticationService.verifyForgot(code);
            return ResponseEntity.ok("Password reset code sent");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    @GetMapping("/launch/project")
    public ResponseEntity<List<HomeProjectResponse>> getHomeProjects() {
        List<HomeProjectResponse> homeProjects = launchProjectService.getHomeProjects();
        return ResponseEntity.ok(homeProjects);
    }

    @Operation(
            summary = "Get approved ratings",
            description = "Retrieve all user ratings that have been approved by admins for display"
    )
    @GetMapping("/ratings")
    public ResponseEntity<List<UserRatingResponse>> getApprovedRatings() {
        List<UserRatting> approvedRatings = userService.getApprovedRatings();
        List<UserRatingResponse> response = approvedRatings.stream()
                .map(rating -> UserRatingResponse.builder()
                        .userName(rating.getUsers().getName())
                        .userPhoto(rating.getUsers().getPhotoUrl())
                        .message(rating.getMessage())
                        .starNumber(rating.getStarNumber())
                        .build())
                .toList();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get all community projects", description = "Retrieves a list of all approved community projects in the system")
    @GetMapping
    public ResponseEntity<List<CommunityResponseDto>> getAllProjects() {
        List<CommunityResponseDto> projects = communityProjectService.getAllProjectsApproved();
        return ResponseEntity.ok(projects);
    }



}