package com.org.group.controller.user;

import com.org.group.dto.OrderedProject.OrderedProjectDto;
import com.org.group.responses.project.OrderedProjectResponse;
import com.org.group.services.OrderedProject.OrderedProjectServices;
import jakarta.validation.Valid;
import jakarta.websocket.server.PathParam;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/ordered-projects")
public class OrderedProjectController {
    @Autowired
    private OrderedProjectServices orderedProjectServices;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> createOrderedProject(
            @RequestPart("dto") OrderedProjectDto dto,
            @RequestPart(value = "businessIdeaDocument", required = false) MultipartFile businessIdeaDocument,
            @RequestPart(value="businessPlanDocument",required =false) MultipartFile businessPlanDocument
    ) throws IOException {
        dto.setBusinessIdeaDocument(businessIdeaDocument);
        dto.setBusinessPlanDocument(businessPlanDocument);
        return orderedProjectServices.createOrderedProject(dto);
    }

    @GetMapping("/")
    public ResponseEntity<List<OrderedProjectResponse>> getAllOrderedProjects() {
        return ResponseEntity.ok(orderedProjectServices.getAllOrderedProjects());
    }

    @GetMapping("/{projectId}")
    public ResponseEntity<OrderedProjectResponse> getOrderedProjectById(@PathVariable UUID projectId) {
        OrderedProjectResponse dto = orderedProjectServices.getOrderedProjectById(projectId);
        if (dto == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(dto);
    }

    @PutMapping(value = "/{projectId}", consumes = {"multipart/form-data"})
    public ResponseEntity<OrderedProjectResponse> updateOrderedProject(
            @PathVariable UUID projectId,
            @RequestPart("dto") OrderedProjectDto dto,
            @RequestPart(value = "businessIdeaDocument", required = false) MultipartFile businessIdeaDocument,
            @RequestPart(value = "businessPlanDocument",required = false) MultipartFile businessPlanDocument
    ) throws IOException {
        dto.setBusinessIdeaDocument(businessIdeaDocument);
        dto.setBusinessPlanDocument(businessPlanDocument);
        OrderedProjectResponse updated = orderedProjectServices.updateOrderedProject(projectId, dto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrderedProject(@PathVariable UUID id) {
        orderedProjectServices.deleteOrderedProject(id);
        return ResponseEntity.noContent().build();
    }
}
