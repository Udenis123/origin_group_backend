package com.org.group.repository.analytics;

import com.org.group.model.analyzer.AnalyticsFeedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AnalyticsFeedbackRepository extends JpaRepository<AnalyticsFeedback, Long> {
    List<AnalyticsFeedback> findByProjectId(UUID projectId);
    
    @Query("SELECT af FROM AnalyticsFeedback af WHERE af.projectId = :projectId ORDER BY af.id DESC")
    List<AnalyticsFeedback> findLatestFeedbacksByProjectId(@Param("projectId") UUID projectId);
    
    default Optional<AnalyticsFeedback> findLatestByProjectId(UUID projectId) {
        List<AnalyticsFeedback> feedbacks = findLatestFeedbacksByProjectId(projectId);
        return feedbacks.isEmpty() ? Optional.empty() : Optional.of(feedbacks.get(0));
    }
}
