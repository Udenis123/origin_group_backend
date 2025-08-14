package com.org.group.controller.analyzer;


import com.org.group.dto.admin.AnalyzerDto;
import com.org.group.dto.admin.AnalyzerInfoDto;
import com.org.group.dto.analytics.AnalyticsDto;
import com.org.group.dto.analytics.AnalyticsResponseDto;
import com.org.group.responses.project.LaunchProjectResponse;
import com.org.group.services.Analyzer.AnalyzerServices;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@Tag(name = "Analyzer Port to add analytics and all can see", description = "Analyzer management APIs")
@RequestMapping("/analyzer")
@RequiredArgsConstructor
@CrossOrigin("http://localhost:4203")
public class AnalyzerController {

    private final AnalyzerServices analyzerServices;

    @GetMapping("/pending/project")
    public ResponseEntity<List<LaunchProjectResponse>> getAllPendingProjects() {
        return  analyzerServices.getAllPendingProject();
    }
    @PostMapping("/project/assigment")
    public ResponseEntity<?> assignProject(@RequestParam("projectId")  UUID projectId,@RequestParam("analyzerId") UUID analyzerId){
        analyzerServices.assignProject(projectId, analyzerId);
        return ResponseEntity.ok().build();
     }
     @GetMapping("/project/assigment")
    public ResponseEntity<List<LaunchProjectResponse>> getAllAssignedProjects(@RequestParam("analyzerId") UUID analyzerId){
        return analyzerServices.getAllAssignedProject(analyzerId);
     }
     @PostMapping("/project/add/analytics")
     public ResponseEntity<?> putAnalyticsOnProject(@Valid @RequestPart("analyticsDetails") AnalyticsDto analyticsDto,@RequestPart(value = "analyticDocument", required = false)MultipartFile analyticsDocument) throws IOException {
        analyzerServices.putAnalytics(analyticsDto,analyticsDocument);
        return ResponseEntity.ok().build();

     }
    @GetMapping("/project/get/analytics")
    public ResponseEntity<AnalyticsResponseDto> getAllAnalytics(@RequestParam("projectId") UUID projectId) {
        AnalyticsResponseDto analytics = analyzerServices.getAnalyticsOfProject(projectId);
        return ResponseEntity.ok(analytics);
    }

    @PutMapping("/project/update/analytics")
    public ResponseEntity<?> updateAnalytics(
                                              @Valid @RequestPart("analytics") AnalyticsDto analyticsDto,
                                              @RequestPart(value = "analyticsDoc", required = false) MultipartFile analyticsDocument) throws IOException {
        return  ResponseEntity.ok(analyzerServices.updateAnalytics(analyticsDto,analyticsDocument));
    }

    @PostMapping("/project/decline")
    public  ResponseEntity<?> declineProject(@RequestParam UUID projectId, @RequestParam UUID analyzerId, @RequestParam String feedback) {
        return  ResponseEntity.ok(analyzerServices.declineProject(projectId,analyzerId,feedback));
    }
    @PostMapping("/project/completed")
    public  ResponseEntity<?> enableAnalyticsOfProject(@RequestParam UUID projectId){
        return  ResponseEntity.ok(analyzerServices.enableAnalyticsOfProject(projectId));
    }

    @GetMapping("/project/analysts")
    public ResponseEntity<List<AnalyzerInfoDto>> getProjectAnalysts(@RequestParam UUID projectId) {
        return analyzerServices.getAnalyzersForProject(projectId);
    }

}
