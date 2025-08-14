package com.org.group.repository.analytics;

import com.org.group.model.analyzer.Assignment;
import com.org.group.model.project.LaunchProject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AssignmentRepository extends JpaRepository<Assignment, Long> {
    Optional<Assignment> findByProjectProjectId(UUID projectId);
    boolean existsByProject_ProjectIdAndAnalyzer_Id(UUID projectId, UUID analyzerId);
    @Query("SELECT a.project FROM Assignment a WHERE a.analyzer.id = :analyzerId AND a.project.status = com.org.group.dto.LaunchProject.AnalyticStatus.PENDING")
    List<LaunchProject> findPendingProjectsByAnalyzerId(@Param("analyzerId") UUID analyzerId);

    @Query("SELECT a FROM Assignment a WHERE a.project.projectId = :projectId")
    List<Assignment> findAssignmentsByProjectId(@Param("projectId") UUID projectId);
}
