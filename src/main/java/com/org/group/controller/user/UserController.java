package com.org.group.controller.user;

import com.org.group.dto.userAuth.ChangePasswordDto;
import com.org.group.dto.userAuth.ProfileUpdateDto;
import com.org.group.dto.userAuth.UserRattingDto;
import com.org.group.dto.userResponse.SubscriptionResponse;
import com.org.group.dto.userResponse.UserDetailResponseDto;
import com.org.group.dto.userResponse.UserRatingResponse;
import com.org.group.model.UserRatting;
import com.org.group.model.UserSubscription;
import com.org.group.model.Users;
import com.org.group.model.analyzer.Analyzer;
import com.org.group.responses.ProfileResponse;
import com.org.group.services.*;
import com.org.group.services.UploadFileServices.FileStorageService;
import com.org.group.services.emailAndJwt.PlanFilterServices;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Tag(name = "User", description = "User management APIs")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/client")
public class UserController {
    private final FileStorageService fileStorageService;
    private final UserService userService;
    private final PlanFilterServices planFilterServices;

    public UserController(FileStorageService fileStorageService, UserService userService, PlanFilterServices planFilterServices) {
        this.fileStorageService = fileStorageService;
        this.userService = userService;
        this.planFilterServices = planFilterServices;
    }

    @Operation(summary = "Test endpoint")
    @GetMapping("/hello")
    public String denis(){
        return "denis";
    }

    @GetMapping("/profile")
    public ResponseEntity<ProfileResponse> getUserProfile(@RequestParam("userId") UUID userId) {

        Users user = userService.getUserByHomeId(userId);
        Analyzer analyzer = userService.getAnalyzerById(userId);
        if(user != null) {
            // Get the highest priority plan from active subscriptions
            String highestPriorityPlan = planFilterServices.getPlanFiltered(user);

            // Get the status of the highest priority subscription
            String status = user.getSubscriptions().stream()
                    .filter(sub -> sub.getPlan().toString().equals(highestPriorityPlan))
                    .findFirst()
                    .map(sub -> sub.getStatus().toString())
                    .orElse("EXPIRED");

            ProfileResponse profileResponse = ProfileResponse.builder()
                    .plan(highestPriorityPlan)
                    .status(status)
                    .fullName(user.getName())
                    .email(user.getEmail())
                    .emailStatus(user.isEnabled())
                    .phone(user.getPhone())
                    .idNumber(user.getNationalId())
                    .gender(user.getGender())
                    .nationality(user.getNationality())
                    .profession(user.getProfessional())
                    .profilePicture(user.getPhotoUrl())
                    .build();
            return ResponseEntity.ok(profileResponse);
        }else if(analyzer != null) {
            ProfileResponse profileResponse = ProfileResponse.builder()
                    .fullName(analyzer.getName())
                    .email(analyzer.getEmail())
                    .emailStatus(analyzer.isEnabled())
                    .phone(analyzer.getPhone())
                    .idNumber(analyzer.getNationalId())
                    .gender(analyzer.getGender())
                    .nationality(analyzer.getNationality())
                    .profession(analyzer.getExpertise())
                    .profilePicture(analyzer.getProfileUrl())
                    .build();
            return ResponseEntity.ok(profileResponse);

        }
        return ResponseEntity.notFound().build();

    }


    @Operation(
        summary = "Update user email",
        description = "Updates user email after verify it through OTP"
    )
    @PostMapping("/setting/update/email")
    public ResponseEntity<?> updateEmail(@RequestParam("userId") UUID userId, @RequestParam("newEmail") String newEmail) {
        try {
            userService.emailSettings(userId, newEmail);
            return ResponseEntity.ok("Verification code sent to new email.");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    @Operation(
            summary = "Verify user email in settings",
            description = "Verify user email with verification code if done update email"
    )
    @PostMapping("/setting/verify/{code}")
    public ResponseEntity<?> verifyUser(@PathVariable("code") String code,@RequestParam(value = "userId") UUID userId) {
        try {

            return ResponseEntity.ok(userService.verifyUser(userId,code));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(
        summary = "Update user information by ID",
        description = "Updates user information (name, email, phone, gender, nationality, profession) by ID"
    )
    @PutMapping("/setting/profile/update")
    public ResponseEntity<?> updateClient(@Valid @RequestBody ProfileUpdateDto profileUpdateDto){
        try {

            return ResponseEntity.ok(userService.updateUserInformation(profileUpdateDto));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }


    }

    @Operation(
        summary = "Upload user photo",
        description = "Upload and store user profile photo"
    )
    @PostMapping("/upload-photo")
    public ResponseEntity<?> uploadPhoto(
            @RequestParam("userId") UUID userId,
            @RequestParam("file") MultipartFile file) {
        try {
            // Update user's photo URL
            return ResponseEntity.ok(userService.updateUserPhoto(userId,file));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());

    } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Operation(
            summary = "Get user ",
            description = "this end point it get all details of user from scratch"
    )
    @GetMapping("/id")
    public ResponseEntity<UserDetailResponseDto> getUserById(@RequestParam("userId") UUID userId) {

        return ResponseEntity.ok(userService.getUserByIdDetails(userId));
    }



    @Operation(
            summary = "Get  subscription ",
            description = "Retrieve user subscription have and also start and end time"
    )
    @GetMapping("/subscription")
    public ResponseEntity<List<SubscriptionResponse>> getUserSub(@RequestParam("userId") UUID id) {
        List<UserSubscription> userSubscriptions = userService.getSubscription(id);

        if (userSubscriptions.isEmpty()) {
            throw new RuntimeException("No Subscriptions Found");
        }

        List<SubscriptionResponse> subscriptionResponses = userSubscriptions.stream()
                .map(sub -> SubscriptionResponse.builder()
                        .id(sub.getId())
                        .userId(sub.getUser().getId())
                        .plan(sub.getPlan().toString())
                        .status(sub.getStatus().toString())
                        .endDate(sub.getEndDate())
                        .startDate(sub.getStartDate())
                        .build())
                .toList();

        return ResponseEntity.ok(subscriptionResponses);
    }

    @Operation(
            summary = "removes profile ",
            description = "Retrieve user and get profile photo remove it set back to default"
    )
    @DeleteMapping("/remove-photo")
    public ResponseEntity<?> deletePhoto(@RequestParam("userId") UUID userId) {
        try {
           // fileStorageService.deleteFilesByUserId(userId);
            return ResponseEntity.ok("Photos deleted and default photo set successfully.");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    @Operation(
            summary = "change user password  ",
            description = "change user password in setting page as well"
    )
    @PutMapping("/update-password")
    public ResponseEntity<?> updatePassword(@Valid @RequestBody ChangePasswordDto changePasswordDto){
        try {

            return ResponseEntity.ok(userService.changePassword(changePasswordDto));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(
            summary = "Submit system rating",
            description = "Allow users to rate the system with stars and a message"
    )
    @PostMapping("/ratting")
    public ResponseEntity<?> rattingSystem(@RequestBody UserRattingDto userRattingDto) {
        return ResponseEntity.ok(userService.rattingSystem(userRattingDto));
    }

    @Operation(
            summary = "Check if user has rated",
            description = "Check if the user has already submitted a rating for the system"
    )
    @GetMapping("/has-rated")
    public ResponseEntity<Boolean> hasUserRated(@RequestParam("userId") UUID userId) {
        boolean hasRated = userService.hasUserRatedSystem(userId);
        return ResponseEntity.ok(hasRated);
    }

    @Operation(
            summary = "Get user's rating",
            description = "Retrieve the user's existing rating if they have already rated the system"
    )
    @GetMapping("/user-rating")
    public ResponseEntity<?> getUserRating(@RequestParam("userId") UUID userId) {
        Optional<UserRatting> ratingOpt = userService.getUserRating(userId);
        
        if (ratingOpt.isPresent()) {
            UserRatting rating = ratingOpt.get();
            UserRatingResponse response = UserRatingResponse.builder()
                    .id(rating.getId())
                    .userId(rating.getUsers().getId())
                    .userName(rating.getUsers().getName())
                    .userPhoto(rating.getUsers().getPhotoUrl())
                    .message(rating.getMessage())
                    .starNumber(rating.getStarNumber())
                    .status(rating.getStatus())
                    .build();
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.ok(Map.of("message", "User has not rated the system yet"));
        }
    }



}
