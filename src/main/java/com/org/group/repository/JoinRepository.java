package com.org.group.repository;

import com.org.group.model.project.JoinedProject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface JoinRepository extends JpaRepository<JoinedProject, UUID> {
    List<JoinedProject> findByCommunityProjectId(UUID communityProjectId);
    boolean existsByUserIdAndCommunityProjectId(UUID userId, UUID communityProjectId);
    List<JoinedProject> findByUserId(UUID userId);
}
