package com.org.group;

import com.org.group.model.analyzer.Analyzer;
import com.org.group.repository.AnalyzerRepository;
import com.org.group.role.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;

@SpringBootApplication
public class OrgGroupProjectApplication {

	public static void main(String[] args) {
		SpringApplication.run(OrgGroupProjectApplication.class, args);
	}
	
	@Bean
	public CommandLineRunner initializeAdmin(
		@Autowired AnalyzerRepository analyzerRepository,
		@Autowired PasswordEncoder passwordEncoder
	) {
		return args -> {
			String adminEmail = "origingroup299@gmail.com";
			Optional<Analyzer> existingAdmin = analyzerRepository.findByEmail(adminEmail);
			
			if (existingAdmin.isEmpty()) {
				Analyzer admin = Analyzer.builder()
					.name("Admin User")
					.email(adminEmail)
					.password(passwordEncoder.encode("Admin@123")) // Default password
					.phone("+250700000000")
					.expertise("System Administration")
					.profileUrl("")
					.nationality("Rwanda")
					.gender("Other")
					.nationalId("1234567890")
					.enabled(true)
					.roles(Set.of(Role.ADMIN))
					.build();
				
				analyzerRepository.save(admin);
				
				System.out.println("Admin user created successfully with email: " + adminEmail);
				System.out.println("Default password: Admin@123");
				System.out.println("Please change the password after first login.");
			} else {
				System.out.println("Admin user already exists.");
			}
		};
	}
}
