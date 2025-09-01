package com.org.group.repository.analytics;

import com.org.group.model.analyzer.AnalyticProject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository

public interface AnalyticProjectRepository extends JpaRepository<AnalyticProject, Long> {
    Optional<AnalyticProject> findByLaunchProject_ProjectId(UUID projectProjectId);
    List<AnalyticProject> findByAnalyzerId(UUID analyzerId);
}
