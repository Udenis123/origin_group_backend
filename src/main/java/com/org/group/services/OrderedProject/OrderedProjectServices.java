package com.org.group.services.OrderedProject;

import com.org.group.dto.OrderedProject.OrderedProjectDto;
import com.org.group.model.Users;
import com.org.group.model.project.OrderedProject;
import com.org.group.repository.UserRepository;
import com.org.group.repository.project.OrderedProjectRepository;
import com.org.group.responses.project.OrderedProjectResponse;
import com.org.group.services.UploadFileServices.CloudinaryService;
import com.org.group.services.UploadFileServices.FileStorageService;
import com.org.group.dto.LaunchProject.AnalyticStatus;
import com.org.group.services.emailAndJwt.PlanFilterServices;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderedProjectServices {

    private final OrderedProjectRepository orderedProjectRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;
    private final CloudinaryService cloudinaryService;
    private final PlanFilterServices planFilterServices;

    public ResponseEntity<String> createOrderedProject(UUID userId, OrderedProjectDto dto) throws IOException {
        Users user = userRepository.findById(userId).orElseThrow(()-> new EntityNotFoundException("User not found"));
        String plan = planFilterServices.getPlanFiltered(user);
        if (plan.equals("FREE") || plan.equals("BASIC")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("You are not authorized to Order project. Upgrade your plan.");
        }


        OrderedProject project = new OrderedProject(); 
        // Map fields from dto to entity
        project.setClientName(dto.getClientName());
        project.setCompanyName(dto.getCompanyName());
        project.setProfessionalStatus(dto.getProfessionalStatus());
        project.setEmail(dto.getEmail());
        project.setPhone(dto.getPhone());
        project.setLinkedIn(dto.getLinkedIn());
        project.setProjectTitle(dto.getProjectTitle());
        project.setProjectType(dto.getProjectType());
        project.setProjectDescription(dto.getProjectDescription());
        project.setTargetAudience(dto.getTargetAudience());
        project.setReferences(dto.getReferences());
        project.setProjectLocation(dto.getProjectLocation());
        project.setSpecialityOfProject(dto.getSpecialityOfProject());
        project.setDoYouHaveSponsorship(dto.getDoYouHaveSponsorship());
        project.setSponsorName(dto.getSponsorName());
        project.setDoYouNeedIntellectualProject(dto.getDoYouNeedIntellectualProject());
        project.setDoYouNeedBusinessPlan(dto.getDoYouNeedBusinessPlan());
        project.setBusinessIdea(dto.getBusinessIdea());
        project.setStatus(AnalyticStatus.PENDING);
        // Set user
        project.setUser(user);
        // Handle file upload
        MultipartFile file1 = dto.getBusinessIdeaDocument();
        MultipartFile file2 = dto.getBusinessPlanDocument();
        
        try {
            if (file1 != null && !file1.isEmpty()) {
                String url = cloudinaryService.uploadProjectIdea(file1);
                project.setBusinessIdeaDocumentUrl(url);
            }
        } catch (Exception e) {
            throw new IOException("Failed to upload business idea document", e);
        }
        
        try {
            if (file2 != null && !file2.isEmpty()) {
                String url = cloudinaryService.uploadProjectPlan(file2);
                project.setBusinessPlanUrl(url);
            }
        } catch (Exception e) {
            throw new IOException("Failed to upload business plan document", e);
        }
        OrderedProject saved = orderedProjectRepository.save(project);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved.toString());
    }

    public List<OrderedProjectResponse> getAllOrderedProjects() {
        return orderedProjectRepository.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    public OrderedProjectResponse getOrderedProjectById(UUID id) {
        Optional<OrderedProject> project = orderedProjectRepository.findById(id);
        return project.map(this::toDto).orElse(null);
    }

    public OrderedProjectResponse updateOrderedProject(UUID projectId, OrderedProjectDto dto) throws IOException {
        OrderedProject project = orderedProjectRepository.findById(projectId).orElseThrow();
        // Update fields
        project.setClientName(dto.getClientName());
        project.setCompanyName(dto.getCompanyName());
        project.setProfessionalStatus(dto.getProfessionalStatus());
        project.setEmail(dto.getEmail());
        project.setPhone(dto.getPhone());
        project.setLinkedIn(dto.getLinkedIn());
        project.setProjectTitle(dto.getProjectTitle());
        project.setProjectType(dto.getProjectType());
        project.setProjectDescription(dto.getProjectDescription());
        project.setTargetAudience(dto.getTargetAudience());
        project.setReferences(dto.getReferences());
        project.setProjectLocation(dto.getProjectLocation());
        project.setSpecialityOfProject(dto.getSpecialityOfProject());
        project.setDoYouHaveSponsorship(dto.getDoYouHaveSponsorship());
        project.setSponsorName(dto.getSponsorName());
        project.setDoYouNeedIntellectualProject(dto.getDoYouNeedIntellectualProject());
        project.setDoYouNeedBusinessPlan(dto.getDoYouNeedBusinessPlan());
        project.setBusinessIdea(dto.getBusinessIdea());
        // Optionally update file
        MultipartFile file1 = dto.getBusinessIdeaDocument();
        MultipartFile file2 = dto.getBusinessPlanDocument();
        
        try {
            if (file1 != null && !file1.isEmpty()) {
                // Delete old file if exists
                if (project.getBusinessIdeaDocumentUrl() != null && !project.getBusinessIdeaDocumentUrl().isEmpty()) {
                    cloudinaryService.deleteFile(project.getBusinessIdeaDocumentUrl());
                }
                String url = cloudinaryService.uploadProjectIdea(file1);
                project.setBusinessIdeaDocumentUrl(url);
            }
        } catch (Exception e) {
            throw new IOException("Failed to update business idea document", e);
        }
        
        try {
            if (file2 != null && !file2.isEmpty()) {
                // Delete old file if exists
                if (project.getBusinessPlanUrl() != null && !project.getBusinessPlanUrl().isEmpty()) {
                    cloudinaryService.deleteFile(project.getBusinessPlanUrl());
                }
                String url = cloudinaryService.uploadProjectPlan(file2);
                project.setBusinessPlanUrl(url);
            }
        } catch (Exception e) {
            throw new IOException("Failed to update business plan document", e);
        }
        OrderedProject saved = orderedProjectRepository.save(project);
        return toDto(saved);
    }

    public void deleteOrderedProject(UUID id) {
        orderedProjectRepository.deleteById(id);
    }

    private OrderedProjectResponse toDto(OrderedProject project) {
        OrderedProjectResponse dto = new OrderedProjectResponse();
        dto.setProjectId(project.getProjectId());
        dto.setClientName(project.getClientName());
        dto.setCompanyName(project.getCompanyName());
        dto.setProfessionalStatus(project.getProfessionalStatus());
        dto.setEmail(project.getEmail());
        dto.setPhone(project.getPhone());
        dto.setLinkedIn(project.getLinkedIn());
        dto.setProjectTitle(project.getProjectTitle());
        dto.setProjectType(project.getProjectType());
        dto.setProjectDescription(project.getProjectDescription());
        dto.setTargetAudience(project.getTargetAudience());
        dto.setReferences(project.getReferences());
        dto.setProjectLocation(project.getProjectLocation());
        dto.setSpecialityOfProject(project.getSpecialityOfProject());
        dto.setDoYouHaveSponsorship(project.getDoYouHaveSponsorship());
        dto.setSponsorName(project.getSponsorName());
        dto.setDoYouNeedIntellectualProject(project.getDoYouNeedIntellectualProject());
        dto.setDoYouNeedBusinessPlan(project.getDoYouNeedBusinessPlan());
        dto.setBusinessIdea(project.getBusinessIdea());
        dto.setStatus(project.getStatus());
        dto.setBusinessPlanDocumentUrl(project.getBusinessIdeaDocumentUrl());
        dto.setBusinessIdeaDocumentUrl(project.getBusinessPlanUrl());
        dto.setUserId(project.getUser().getId());
        return dto;
    }

    //admin endpoint
    public List<OrderedProjectResponse> getAllOrderedProjectsPending() {
        return orderedProjectRepository.findAll().stream().map(this::toDto)
                .filter(OrderedProjectResponse->
                        OrderedProjectResponse.getStatus().equals(AnalyticStatus.PENDING))
                .collect(Collectors.toList());
    }
    public List<OrderedProjectResponse> getAllOrderedProjectsApproved() {
        return orderedProjectRepository.findAll().stream().map(this::toDto)
                .filter(OrderedProjectResponse->
                        OrderedProjectResponse.getStatus().equals(AnalyticStatus.APPROVED))
                .collect(Collectors.toList());
    }

    public boolean approveOrderedProject(UUID id) {
        OrderedProject project = orderedProjectRepository.findById(id).orElseThrow(()->new RuntimeException("Could not find project with id: "+id));
        project.setStatus(AnalyticStatus.APPROVED);
        orderedProjectRepository.save(project);
        return true;
    }
    public boolean rejectOrderedProject(UUID id , String reason) {
        OrderedProject project = orderedProjectRepository.findById(id).orElseThrow(()->new RuntimeException("Could not find project with id: "+id));
        project.setStatus(AnalyticStatus.DECLINED);
        project.setReasons(reason);
        orderedProjectRepository.save(project);
        return true;
    }

    public List<OrderedProjectResponse> getOrderedProjectsByUserId(UUID userId) {
        List<OrderedProject> projects = orderedProjectRepository.findByUser_Id(userId);
        return projects.stream().map(this::toDto).collect(Collectors.toList());
    }

    public String updateOrderedProjectStatus(UUID projectId, AnalyticStatus status, String reason) {
        OrderedProject project = orderedProjectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Could not find project with id: " + projectId));

        project.setStatus(status);
        project.setReasons(reason);
        orderedProjectRepository.save(project);

        return "Ordered project status updated successfully.";
    }
}
