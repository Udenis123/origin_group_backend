package com.org.group.services;

import com.org.group.dto.userAuth.LoginUserDto;
import com.org.group.dto.userAuth.RegisterUserDto;
import com.org.group.dto.userAuth.ResetPasswordDto;
import com.org.group.exceptionHandling.UnauthorizedException;
import com.org.group.model.UserSubscription;
import com.org.group.model.Users;
import com.org.group.model.analyzer.Analyzer;
import com.org.group.repository.AnalyzerRepository;
import com.org.group.repository.UserRepository;
import com.org.group.role.Role;
import com.org.group.services.emailAndJwt.EmailService;
import com.org.group.subscription.SubscriptionPlan;
import com.org.group.subscription.SubscriptionStatus;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AuthenticationServices {
    private final UserRepository userRepository;
    private final AnalyzerRepository analyzerRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;


    public Users signup(RegisterUserDto input) {
        Optional<Users> existingUserByEmail = userRepository.findByEmail(input.getEmail());
        Optional<Users> existingUserByPhone = userRepository.findByPhone(input.getPhone());
        Optional<Analyzer> existingAnalyzerByEmail = analyzerRepository.findByEmail(input.getEmail());


        if (existingUserByEmail.isPresent()) {
            Users user = existingUserByEmail.get();
            if (user.isEnabled()) {
                throw new IllegalStateException("User is already signed up.");
            } else {
                throw new IllegalStateException("User already registered but not verified. Please check your email.");
            }
        }

        if (existingUserByPhone.isPresent()) {
            throw new IllegalStateException("Phone number is already registered.");
        }
        if(existingAnalyzerByEmail.isPresent()) {
            throw new IllegalStateException("Email is already registered as analyzer.");
        }


        Users newUser = Users.builder()
                .name(input.getName())
                .email(input.getEmail())
                .phone(input.getPhone())
                .password(passwordEncoder.encode(input.getPassword()))
                .enabled(false)
                .photoUrl("https://static.vecteezy.com/system/resources/thumbnails/010/260/479/small_2x/default-avatar-profile-icon-of-social-media-user-in-clipart-style-vector.jpg")
                .verificationCode(generateVerificationCode())
                .codeExpiryAt(LocalDateTime.now().plusMinutes(10))
                .roles(Set.of(Role.CLIENT))
                .isActive(true)
                .subscribed(false)
                .build();

        UserSubscription defaultSubscription = UserSubscription.builder()
                .user(newUser)
                .plan(SubscriptionPlan.FREE)
                .startDate(LocalDateTime.now())
                .endDate(null)
                .status(SubscriptionStatus.ACTIVE)
                .build();

        newUser.setSubscriptions(Set.of(defaultSubscription));

        Users savedUser = userRepository.save(newUser);
        Users loadedUser = userRepository.findById(savedUser.getId())
                .orElseThrow(() -> new RuntimeException("User not found after registration"));

        if (savedUser.getId() != null) {
            sendVerificationEmail(savedUser);
        }

        return loadedUser;
    }


    public Users authenticate(LoginUserDto input) {
        Optional<Users> userOpt = userRepository.findByEmail(input.getEmail());

        if (!userOpt.isPresent()) {
            throw new UnauthorizedException("User with email " + input.getEmail() + " not found.");
        }

        Users user = userOpt.get();

        if (!user.isEnabled()) {
            throw new UnauthorizedException("User account not verified.");
        }
        if(!user.isActive()) {
            throw new UnauthorizedException("User account is disable by origin group contact support.");
        }

        // Perform authentication
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(input.getEmail(), input.getPassword())
            );
        } catch (Exception ex) {
            throw new UnauthorizedException("Invalid email or password.");
        }

        return user;
    }



    public void verifyUser(String code) {
        Optional<Users> optionalUser = userRepository.findByVerificationCode(code);

        if (optionalUser.isEmpty()) {
            throw new RuntimeException("Invalid verification code.");
        }

        Users user = optionalUser.get();

        if (user.getCodeExpiryAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Verification code has expired. Please request a new one.");
        }

        // Mark user as verified
        user.setEnabled(true);
        user.setVerificationCode(null);
        user.setCodeExpiryAt(null);

        userRepository.save(user);
    }


    public void resendVerificationCode(String email) {
        Optional<Users> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isPresent()) {
            Users user = optionalUser.get();
            if (user.isEnabled()) {
                throw new RuntimeException("Account is already verified");
            }
            user.setVerificationCode(generateVerificationCode());
            user.setCodeExpiryAt(LocalDateTime.now().plusHours(1));
            sendVerificationEmail(user);
            userRepository.save(user);
        } else {
            throw new RuntimeException("User not found");
        }
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
                + "    <h1>WELCOME TO ORIGIN GROUP!</h1>"
                + "  </div>"
                + "  <div class='content'>"
                + "    <h2>Please enter the verification code below to activate your account:</h2>"
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
            emailService.sendVerificationEmail(user.getEmail(), subject, htmlMessage);
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
//forgot password section
    public void sendPasswordResetCode(String email) {
        Optional<Users> optionalUser = userRepository.findByEmail(email);
        Optional<Analyzer> optionalAnalyzer = analyzerRepository.findByEmail(email);
        
        if (optionalUser.isEmpty() && optionalAnalyzer.isEmpty()) {
            throw new RuntimeException("User not found");
        }
        
        String resetCode = generateVerificationCode();
        LocalDateTime expiryTime = LocalDateTime.now().plusMinutes(15); // Code valid for 15 minutes
        
        // If user exists in Users table
        if (optionalUser.isPresent()) {
            Users user = optionalUser.get();
            user.setVerificationCode(resetCode);
            user.setCodeExpiryAt(expiryTime);
            userRepository.save(user);
            sendPasswordResetEmail(user, resetCode);
        }
        
        // If user exists in Analyzer table
        if (optionalAnalyzer.isPresent()) {
            Analyzer analyzer = optionalAnalyzer.get();
            analyzer.setVerificationCode(resetCode);
            analyzer.setCodeExpiryAt(expiryTime);
            analyzerRepository.save(analyzer);
            sendPasswordResetEmailAnalyzer(analyzer, resetCode);
        }
    }

    public void resetPassword(ResetPasswordDto resetPasswordDto) {
        Optional<Users> optionalUser = userRepository.findByEmail(resetPasswordDto.getEmail());
        Optional<Analyzer> optionalAnalyzer = analyzerRepository.findByEmail(resetPasswordDto.getEmail());

        // User must exist in either Users or Analyzer
        if (optionalUser.isEmpty() && optionalAnalyzer.isEmpty()) {
            throw new RuntimeException("User not found. Please register first.");
        }

        String encodedPassword = passwordEncoder.encode(resetPasswordDto.getNewPassword());

        // If it's a user
        if (optionalUser.isPresent()) {
            Users user = optionalUser.get();
            if (user.getVerificationCode() == null) {
                throw new RuntimeException("Request verification code first.");
            }
            if (user.getCodeExpiryAt().isBefore(LocalDateTime.now())) {
                throw new RuntimeException("Verification code has expired. Please request a new one.");
            }

            user.setPassword(encodedPassword);
            user.setVerificationCode(null);
            user.setCodeExpiryAt(null);
            userRepository.save(user);
        }

        // If it's an analyzer
        if (optionalAnalyzer.isPresent()) {
            Analyzer analyzer = optionalAnalyzer.get();
            if (analyzer.getVerificationCode() == null) {
                throw new RuntimeException("Request verification code first.");
            }
            if (analyzer.getCodeExpiryAt().isBefore(LocalDateTime.now())) {
                throw new RuntimeException("Verification code has expired. Please request a new one.");
            }

            analyzer.setPassword(encodedPassword);
            analyzer.setVerificationCode(null);
            analyzer.setCodeExpiryAt(null);
            analyzerRepository.save(analyzer);
        }
    }


    private void sendPasswordResetEmail(Users user, String resetCode) {
        String subject = "Password Reset Request";
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
                + "    <h1>Password Reset Request</h1>"
                + "  </div>"
                + "  <div class='content'>"
                + "    <h2>Please enter the verification code below to reset your password:</h2>"
                + "    <div class='verification-code'>"
                + "      <p>" + resetCode + "</p>"
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
            emailService.sendVerificationEmail(user.getEmail(), subject, htmlMessage);
        } catch (MessagingException e) {
            // Handle email sending exception
            e.printStackTrace();
        }
    }
    private void sendPasswordResetEmailAnalyzer(Analyzer analyzer, String resetCode) {
        String subject = "Password Reset Request";
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
                + "    <h1>Password Reset Request</h1>"
                + "  </div>"
                + "  <div class='content'>"
                + "    <h2>Please enter the verification code below to reset your password:</h2>"
                + "    <div class='verification-code'>"
                + "      <p>" + resetCode + "</p>"
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
            emailService.sendVerificationEmail(analyzer.getEmail(), subject, htmlMessage);
        } catch (MessagingException e) {
            // Handle email sending exception
            e.printStackTrace();
        }
    }
    public void verifyForgot(String code) {
        Optional<Users> optionalUser = userRepository.findByVerificationCode(code);
        Optional<Analyzer> optionalAnalyzer = analyzerRepository.findByVerificationCode(code);

        // Code must exist in at least one entity
        if (optionalUser.isEmpty() && optionalAnalyzer.isEmpty()) {
            throw new RuntimeException("Invalid verification code.");
        }

        // Check expiration if user exists
        if (optionalUser.isPresent()) {
            Users user = optionalUser.get();
            if (user.getCodeExpiryAt().isBefore(LocalDateTime.now())) {
                throw new RuntimeException("Verification code has expired. Please request a new one.");
            }
        }

        // Check expiration if analyzer exists
        if (optionalAnalyzer.isPresent()) {
            Analyzer analyzer = optionalAnalyzer.get();
            if (analyzer.getCodeExpiryAt().isBefore(LocalDateTime.now())) {
                throw new RuntimeException("Verification code has expired. Please request a new one.");
            }
        }

    }


}
