package com.org.group.repository.project;

import com.org.group.model.project.Bookmark;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BookmarkRepository extends JpaRepository<Bookmark, UUID> {
    boolean existsByUserIdAndProjectProjectId(UUID userId, UUID projectId);
    void deleteByUserIdAndProjectProjectId(UUID userId, UUID projectId);
    Optional<Bookmark> findByUserIdAndProjectProjectId(UUID userId, UUID projectId);


}
