package com.org.group.repository;

import com.org.group.model.analyzer.Analyzer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AnalyzerRepository extends JpaRepository<Analyzer, UUID> {

    Optional<Analyzer> findByEmail(String email);
    Optional<Analyzer> findByVerificationCode(String verificationCode);
    
    @Query("SELECT DISTINCT a FROM Analyzer a LEFT JOIN FETCH a.assignment WHERE a.id = :id")
    Optional<Analyzer> findByIdWithAssignments(@Param("id") UUID id);
    
    // Alternative query to debug
    @Query(value = "SELECT COUNT(*) FROM assigment_analyser WHERE analyzer_id = :analyzerId", nativeQuery = true)
    Long countAssignmentsByAnalyzerId(@Param("analyzerId") UUID analyzerId);
}
