package com.org.group.services;

import com.org.group.dto.userAuth.ChangePasswordDto;
import com.org.group.dto.userAuth.ProfileUpdateDto;
import com.org.group.dto.userAuth.UserRattingDto;
import com.org.group.dto.userResponse.UserRatingResponse;
import com.org.group.model.*;
import com.org.group.model.analyzer.Analyzer;
import com.org.group.repository.AnalyzerRepository;
import com.org.group.repository.UserRepository;
import com.org.group.repository.UserSubscriptionRepository;
import com.org.group.repository.project.LaunchProjectRepository;
import com.org.group.repository.project.CommunityProjectRepository;
import com.org.group.repository.project.BookmarkRepository;
import com.org.group.services.UploadFileServices.CloudinaryService;
import com.org.group.services.emailAndJwt.EmailService;
import com.org.group.services.emailAndJwt.PlanFilterServices;
import jakarta.mail.MessagingException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.org.group.model.UserRatting;
import com.org.group.repository.UserRattingRepository;
import com.org.group.dto.userResponse.UserDetailResponseDto;
import com.org.group.dto.userResponse.UserBasicInfoDto;
import com.org.group.dto.userResponse.LaunchedProjectDto;
import com.org.group.dto.userResponse.CommunityProjectDto;
import com.org.group.dto.userResponse.BookmarkDto;
import com.org.group.model.project.LaunchProject;
import com.org.group.model.project.CommunityProject;
import com.org.group.model.project.Bookmark;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

import com.org.group.subscription.SubscriptionStatus;
import org.springframework.web.multipart.MultipartFile;

@Service
@EnableScheduling
public class UserService {
    private final UserRepository userRepository;
    private final UserSubscriptionRepository userSubscriptionRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final AnalyzerRepository analyzerRepository;
    private final UserRattingRepository userRattingRepository;
    private final PlanFilterServices planFilterServices;
    private final CloudinaryService cloudinaryService;
    private final LaunchProjectRepository launchProjectRepository;
    private final CommunityProjectRepository communityProjectRepository;
    private final BookmarkRepository bookmarkRepository;

    public UserService(UserRepository userRepository, EmailService emailService, UserSubscriptionRepository userSubscriptionRepository, PasswordEncoder passwordEncoder, AnalyzerRepository analyzerRepository, UserRattingRepository userRattingRepository, PlanFilterServices planFilterServices, CloudinaryService cloudinaryService, LaunchProjectRepository launchProjectRepository, CommunityProjectRepository communityProjectRepository, BookmarkRepository bookmarkRepository) {
        this.userRepository = userRepository;
        this.userSubscriptionRepository = userSubscriptionRepository;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
        this.analyzerRepository = analyzerRepository;
        this.userRattingRepository = userRattingRepository;
        this.planFilterServices = planFilterServices;
        this.cloudinaryService = cloudinaryService;
        this.launchProjectRepository = launchProjectRepository;
        this.communityProjectRepository = communityProjectRepository;
        this.bookmarkRepository = bookmarkRepository;
    }
    public Users getUserById(UUID userId){
        return  userRepository.findById(userId).orElseThrow(()-> new EntityNotFoundException("user Not found"));
    }



    public UserDetailResponseDto getUserByIdDetails(UUID userId) {
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User Not Found"));
        
        // Get current subscription using PlanFilterServices
        String currentSubscription = planFilterServices.getPlanFiltered(user);
        
        // Get launched projects (only status and project name) - simplified for now
        List<LaunchedProjectDto> launchedProjects = new ArrayList<>();
        try {
            List<LaunchProject> projects = launchProjectRepository.findByUserId(userId);
            launchedProjects = projects.stream()
                    .map(project -> LaunchedProjectDto.builder()
                            .id(project.getProjectId())
                            .projectName(project.getProjectName())
                            .status(project.getStatus() != null ? project.getStatus().toString() : "UNKNOWN")
                            .build())
                    .toList();
        } catch (Exception e) {
            // Log the error but continue with empty list
            System.err.println("Error fetching launched projects: " + e.getMessage());
            launchedProjects = new ArrayList<>();
        }
        
        // Get community projects (only status and project name) - simplified for now
        List<CommunityProjectDto> communityProjects = new ArrayList<>();
        try {
            List<CommunityProject> projects = communityProjectRepository.findByUser(user);
            communityProjects = projects.stream()
                    .map(project -> CommunityProjectDto.builder()
                            .id(project.getId())
                            .projectName(project.getProjectName())
                            .status(project.getStatus() != null ? project.getStatus().toString() : "UNKNOWN")
                            .build())
                    .toList();
        } catch (Exception e) {
            // Log the error but continue with empty list
            System.err.println("Error fetching community projects: " + e.getMessage());
            communityProjects = new ArrayList<>();
        }
        
        // Get bookmarks - simplified for now
        List<BookmarkDto> bookmarks = new ArrayList<>();
        try {
            Set<Bookmark> userBookmarks = user.getBookmarks();
            if (userBookmarks != null) {
                bookmarks = userBookmarks.stream()
                        .map(bookmark -> BookmarkDto.builder()
                                .id(bookmark.getId())
                                .projectName(bookmark.getProject().getProjectName())
                                .projectType("LAUNCHED") // Since bookmarks are only for LaunchProject
                                .build())
                        .toList();
            }
        } catch (Exception e) {
            // Log the error but continue with empty list
            System.err.println("Error fetching bookmarks: " + e.getMessage());
            bookmarks = new ArrayList<>();
        }
        
        // Build user basic info
        UserBasicInfoDto userInfo = UserBasicInfoDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .professional(user.getProfessional())
                .photoUrl(user.getPhotoUrl())
                .nationalId(user.getNationalId())
                .isActive(user.isActive())
                .isSubscribed(user.getSubscribed())
                .build();
        
        return UserDetailResponseDto.builder()
                .userInfo(userInfo)
                .currentSubscription(currentSubscription)
                .launchedProjects(launchedProjects)
                .communityProjects(communityProjects)
                .bookmarks(bookmarks)
                .build();
    }
    
    // Method for backward compatibility - returns the original Users object
    public Users getUserByIdOriginal(UUID userId) {
        return userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User Not Found"));
    }
    public Users getUserByHomeId(UUID userId) {
        return userRepository.findById(userId).orElse(null);
    }

    public Analyzer getAnalyzerById(UUID userId) {

        return analyzerRepository.findById(userId).orElse(null);
    }

    public List<Users> allUsers() {
        List<Users> users = new ArrayList<>();
        userRepository.findAll().forEach(users::add);
        return users;
    }

    public String updateUserPhoto(UUID userId, MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IOException("File is empty or null");
        }
        
        Optional<Users> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            Users user = userOpt.get();
            try {
                String fileUrl = cloudinaryService.uploadFile(file, user.getPhotoUrl());
                Optional<Analyzer> analyzerOpt = analyzerRepository.findByEmail(user.getEmail());
                if (analyzerOpt.isPresent() ) {
                    Analyzer analyzer = analyzerOpt.get();
                    cloudinaryService.deleteFile(analyzer.getProfileUrl());
                    analyzer.setProfileUrl(fileUrl);
                    analyzerRepository.save(analyzer);
                }
                user.setPhotoUrl(fileUrl);
                userRepository.save(user);
                return "Photo uploaded successfully";
            } catch (Exception e) {
                throw new IOException("Failed to upload user photo", e);
            }
        }

        Optional<Analyzer> analyzerOpt = analyzerRepository.findById(userId);
        if (analyzerOpt.isPresent()) {
            Analyzer analyzer = analyzerOpt.get();
            try {
                String photo = cloudinaryService.uploadFile(file, analyzer.getProfileUrl());
                Optional<Users> userOpts = userRepository.findByEmail(analyzer.getEmail());
                if (userOpts.isPresent()) {
                    Users user = userOpts.get();
                    cloudinaryService.deleteFile(user.getPhotoUrl());
                    user.setPhotoUrl(photo);
                    userRepository.save(user);
                }
                analyzer.setProfileUrl(photo);
                analyzerRepository.save(analyzer);
                return "Photo uploaded successfully";
            } catch (Exception e) {
                throw new IOException("Failed to upload analyzer photo", e);
            }
        }

        throw new RuntimeException("User not found");
    }

    public List<UserSubscription> getSubscription(UUID id) {
        return userSubscriptionRepository.findByUser_Id(id).orElse(null);
    }

    public void emailSettings(UUID userId, String newEmail) {
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Optional<Users> existingUser = userRepository.findByEmail(newEmail);
        if (existingUser.isPresent()) {
            throw new RuntimeException("Email is already in use.");
        }

        String verificationCode = generateVerificationCode();
        user.setVerificationCode(verificationCode);
        user.setCodeExpiryAt(LocalDateTime.now().plusMinutes(10));
        user.setTempEmail(newEmail);
        userRepository.save(user);
        sendVerificationEmail(user);
    }
    public ResponseEntity<Map<String,String>> verifyUser(UUID userId, String code) {
        Optional<Users> optionalUser = userRepository.findById(userId);
        Optional<Analyzer> optionalAnalyzer = analyzerRepository.findByEmail(optionalUser.get().getEmail());

        if (!optionalUser.get().getVerificationCode().equals(code)) {
            throw new RuntimeException("Invalid verification code.");
        }
        Users user = optionalUser.get();
        Analyzer analyzer = optionalAnalyzer.get();
        if (user.getCodeExpiryAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Verification code has expired. Please request a new one.");
        }
        if(user.getEmail().equals(analyzer.getEmail())) {
            analyzer.setEmail(user.getTempEmail());
            analyzerRepository.save(analyzer);
        }
        user.setVerificationCode(null);
        user.setCodeExpiryAt(null);
        user.setEmail(user.getTempEmail());
        user.setTempEmail(null);
        userRepository.save(user);

        return ResponseEntity.ok(Map.of("email", user.getEmail()));
    }
    public String updateUserInformation(ProfileUpdateDto profileUpdateDto) {
        Users user = userRepository.findById(profileUpdateDto.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setName(profileUpdateDto.getName());
        user.setPhone(profileUpdateDto.getPhone());
        user.setProfessional(profileUpdateDto.getProfessional());
        userRepository.save(user);
        
        Optional<Analyzer> analyzerOpt = analyzerRepository.findByEmail(user.getEmail());
        if (analyzerOpt.isPresent()) {
            Analyzer analyzer = analyzerOpt.get();
            analyzer.setName(profileUpdateDto.getName());
            analyzer.setPhone(profileUpdateDto.getPhone());
            analyzerRepository.save(analyzer);
        }
        return "User information updated successfully";

    }

    public String ActivateOrInactivate(UUID id) {
        Users users = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
        if(users.isActive()){
            users.setActive(false);
        }else {
            users.setActive(true);
        }
        userRepository.save(users);
        return "User information updated successfully";
    }

    public String changePassword(ChangePasswordDto changePasswordDto) {
        UUID userId = changePasswordDto.getUserId();
        String oldPassword = changePasswordDto.getOldPassword();
        String newPassword = changePasswordDto.getNewPassword();
    
        Users user = userRepository.findById(userId).orElse(null);
        Analyzer analyzer = analyzerRepository.findById(userId).orElse(null);
    
        if (user != null) {
            if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
                throw new RuntimeException("Old password is incorrect.");
            }
            
            // Update user password
            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);
            
            // Update corresponding analyzer if exists with same email
            Optional<Analyzer> analyzerOpt = analyzerRepository.findByEmail(user.getEmail());
            if (analyzerOpt.isPresent()) {
                Analyzer correspondingAnalyzer = analyzerOpt.get();
                correspondingAnalyzer.setPassword(passwordEncoder.encode(newPassword));
                analyzerRepository.save(correspondingAnalyzer);
            }
            
            return "User password changed successfully";
        } else if (analyzer != null) {
            if (!passwordEncoder.matches(oldPassword, analyzer.getPassword())) {
                throw new RuntimeException("Old password is incorrect.");
            }
            
            // Update analyzer password
            analyzer.setPassword(passwordEncoder.encode(newPassword));
            analyzerRepository.save(analyzer);
            
            // Update corresponding user if exists with same email
            Optional<Users> userOpt = userRepository.findByEmail(analyzer.getEmail());
            if (userOpt.isPresent()) {
                Users correspondingUser = userOpt.get();
                correspondingUser.setPassword(passwordEncoder.encode(newPassword));
                userRepository.save(correspondingUser);
            }
            
            return "Analyzer password changed successfully";
        } else {
            throw new RuntimeException("User or Analyzer not found with the given ID.");
        }
    }
    public String rattingSystem(UserRattingDto userRattingDto) {
        Users users = userRepository.findById(userRattingDto.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Check if user has already submitted a rating
        Optional<UserRatting> existingRating = userRattingRepository.findByUsers_Id(userRattingDto.getUserId());
        if (existingRating.isPresent()) {
            UserRatting rating = existingRating.get();
            rating.setMessage(userRattingDto.getMessage());
            rating.setStarNumber(userRattingDto.getStars());
            rating.setRated(true);
            rating.setStatus(RattingStatus.PENDING); // Reset approval status on update
            userRattingRepository.save(rating);
            return "Rating updated successfully";
        }
        
        UserRatting userRatting = UserRatting.builder()
                .users(users)
                .message(userRattingDto.getMessage())
                .status(RattingStatus.PENDING)
                .isRated(true)
                .starNumber(userRattingDto.getStars())
                .build();
                
        userRattingRepository.save(userRatting);
        return "Rating submitted successfully";
    }
    
    public List<UserRatingResponse> getAllPendingRatings() {
        List<UserRatting> ratings = userRattingRepository.findAll();
        // Get the filtered plan for each user individually
        return ratings.stream()
                .filter(u -> u.getStatus() == RattingStatus.PENDING)
                .map(rating -> {
                    // Get the filtered plan for each user individually
                    String filteredPlan = planFilterServices.getPlanFiltered(rating.getUsers());
                    return UserRatingResponse.builder()
                            .id(rating.getId())
                            .userId(rating.getUsers().getId())
                            .userName(rating.getUsers().getName())
                            .userPhoto(rating.getUsers().getPhotoUrl())
                            .message(rating.getMessage())
                            .starNumber(rating.getStarNumber())
                            .status(rating.getStatus())
                            .subscription(filteredPlan)
                            .build();
                })
                .toList();
    }
    
    public String approveRating(Long ratingId) {
        UserRatting rating = userRattingRepository.findById(ratingId)
                .orElseThrow(() -> new RuntimeException("Rating not found"));
        rating.setStatus(RattingStatus.APPROVED);
        userRattingRepository.save(rating);
        return "Rating approved successfully";
    }
    
    public String disapproveRating(Long ratingId) {
        UserRatting rating = userRattingRepository.findById(ratingId)
                .orElseThrow(() -> new RuntimeException("Rating not found"));
        rating.setStatus(RattingStatus.REJECTED);
        userRattingRepository.save(rating);
        return "Rating disapproved successfully";
    }
    
    public List<UserRatting> getApprovedRatings() {
        return userRattingRepository.findByStatus(RattingStatus.APPROVED);
    }

    public boolean hasUserRatedSystem(UUID userId) {
        Optional<UserRatting> existingRating = userRattingRepository.findByUsers_Id(userId);
        return existingRating.isPresent();
    }

    public Optional<UserRatting> getUserRating(UUID userId) {
        return userRattingRepository.findByUsers_Id(userId);
    }

    private void sendVerificationEmail(Users user) {
        String subject = "Account Verification";
        String verificationCode = "VERIFICATION CODE " + user.getVerificationCode();
        String htmlMessage = "<html>"
                + "<head>"
                + "<style>"
                + "  body { font-family: 'Arial', sans-serif; background-color: #f7f7f7; margin: 0; padding: 0; }"
                + "  .email-container { max-width: 600px; margin: 0 auto; background-color: #ffffff; border-radius: 8px; overflow: hidden; box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1); }"
                + "  .header { background-color: #131b5a; color: #ffffff; text-align: center; padding: 20px; }"
                + "  .header h1 { margin: 0; font-size: 24px; font-weight: bold; }"
                + "  .content { padding: 30px; text-align: center; }"
                + "  .content h2 { color: #333333; font-size: 20px; margin-bottom: 20px; }"
                + "  .verification-code { background-color: #f0f0f0; padding: 15px; border-radius: 6px; display: inline-block; margin: 20px 0; }"
                + "  .verification-code p { margin: 0; font-size: 24px; font-weight: bold; color: #007bff; }"
                + "  .footer { text-align: center; padding: 20px; font-size: 14px; color: #666666; background-color: #fbdfb8; }"
                + "  .footer a { color: #007bff; text-decoration: none; }"
                + "</style>"
                + "</head>"
                + "<body>"
                + "<div class='email-container'>"
                + "  <div class='header'>"
                + "    <h1>ORIGIN GROUP VERIFICATION CODE </h1>"
                + "  </div>"
                + "  <div class='content'>"
                + "    <h2>Please enter the verification code below to update email:</h2>"
                + "    <div class='verification-code'>"
                + "      <p>" + verificationCode + "</p>"
                + "    </div>"
                + "    <p>If you did not request this code, please ignore this email.</p>"
                + "  </div>"
                + "  <div class='footer'>"
                + "    <p>Need help? <a href='mailto:origin@group.com'>Contact Support</a></p>"
                + "    <p>&copy; 2025 Your Company. All rights reserved.</p>"
                + "  </div>"
                + "</div>"
                + "</body>"
                + "</html>";

        try {
            emailService.sendVerificationEmail(user.getTempEmail(), subject, htmlMessage);
        } catch (MessagingException e) {
            // Handle email sending exception
            e.printStackTrace();
        }
    }

    private String generateVerificationCode() {
        Random random = new Random();
        int code = random.nextInt(900000) + 100000;
        return String.valueOf(code);
    }


    @Scheduled(cron = "0 0 0 * * *")// Every midnight minutes
    @Transactional
    public void checkAndExpireSubscriptions() {
        LocalDateTime now = LocalDateTime.now();
        userRepository.findAll().forEach(user -> {
            user.getSubscriptions().forEach(subscription -> {
                if (subscription.getEndDate() != null && 
                    subscription.getEndDate().isBefore(now) && 
                    !subscription.getStatus().equals(SubscriptionStatus.EXPIRED)) {
                    subscription.setStatus(SubscriptionStatus.EXPIRED);
                    user.setSubscribed(false);
                }
            });
            userRepository.save(user);
        });
    }


    @Scheduled(cron = "0 0 8 * * ?") // Runs every day at 8:00 AM
    @Transactional
    public void sendSubscriptionExpirationReminders() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime warningTime = now.plusHours(24); // 24 hours from now
        
        List<Users> users = userRepository.findAll();
        
        users.forEach(user -> {
            user.getSubscriptions().forEach(subscription -> {
                if (subscription.getEndDate() != null && 
                    subscription.getEndDate().isAfter(now) &&
                    subscription.getEndDate().isBefore(warningTime) &&
                    !subscription.getStatus().equals(SubscriptionStatus.EXPIRED)) {
                    
                    // Create HTML email content
                    String subject = "Subscription Expiration Reminder";
                    String htmlMessage = createReminderEmailContent(subscription);
                    
                    try {
                        emailService.sendVerificationEmail(user.getEmail(), subject, htmlMessage);
                    } catch (Exception e) {
                        // Handle email sending error
                        e.printStackTrace();
                    }
                }
            });
        });
    }

    private String createReminderEmailContent(UserSubscription subscription) {
        return "<html>" +
                "<head>" +
                "<style>" +
                "  body { font-family: Arial, sans-serif; background-color: #f7f7f7; margin: 0; padding: 0; }" +
                "  .email-container { max-width: 600px; margin: 0 auto; background-color: #ffffff; border-radius: 8px; overflow: hidden; box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1); }" +
                "  .header { background-color: #131b5a; color: #ffffff; text-align: center; padding: 20px; }" +
                "  .header h1 { margin: 0; font-size: 24px; font-weight: bold; }" +
                "  .content { padding: 30px; text-align: center; }" +
                "  .content h2 { color: #333333; font-size: 20px; margin-bottom: 20px; }" +
                "  .details { background-color: #f0f0f0; padding: 15px; border-radius: 6px; display: inline-block; margin: 20px 0; }" +
                "  .details p { margin: 0; font-size: 16px; }" +
                "  .footer { text-align: center; padding: 20px; font-size: 14px; color: #666666; background-color: #fbdfb8; }" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<div class='email-container'>" +
                "  <div class='header'>" +
                "    <h1>Subscription Reminder</h1>" +
                "  </div>" +
                "  <div class='content'>" +
                "    <h2>Your subscription is about to expire</h2>" +
                "    <div class='details'>" +
                "      <p><strong>Plan:</strong> " + subscription.getPlan() + "</p>" +
                "      <p><strong>Expiration Date:</strong> " + subscription.getEndDate() + "</p>" +
                "    </div>" +
                "    <p>Please renew your subscription to continue enjoying our services.</p>" +
                "  </div>" +
                "  <div class='footer'>" +
                "    <p>Need help? <a href='mailto:support@orgroup.com'>Contact Support</a></p>" +
                "    <p>&copy; 2025 ORGroup. All rights reserved.</p>" +
                "  </div>" +
                "</div>" +
                "</body>" +
                "</html>";
    }

    public void updateClientNationalId(UUID clientId, String nationalId) {
        Users user = userRepository.findById(clientId)
                .orElseThrow(() -> new RuntimeException("Client with id " + clientId + " not found"));
        
        if (nationalId == null || nationalId.trim().isEmpty()) {
            throw new RuntimeException("National ID cannot be null or empty");
        }
        
        user.setNationalId(nationalId.trim());
        userRepository.save(user);
    }

}



























