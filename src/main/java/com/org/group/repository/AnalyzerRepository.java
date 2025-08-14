package com.org.group.repository;

import com.org.group.model.analyzer.Analyzer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AnalyzerRepository extends JpaRepository<Analyzer, UUID> {

    Optional<Analyzer> findByEmail(String email);
    Optional<Analyzer> findByVerificationCode(String verificationCode);
}
