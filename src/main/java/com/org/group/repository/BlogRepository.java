package com.org.group.repository;

import com.org.group.model.Blog;
import com.org.group.model.BlogStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BlogRepository extends JpaRepository<Blog, UUID> {
    
    List<Blog> findByStatus(BlogStatus status);
    
    List<Blog> findByStatusOrderByPublishedAtDesc(BlogStatus status);
    
    @Query("SELECT b FROM Blog b WHERE b.status = :status ORDER BY b.publishedAt DESC")
    List<Blog> findPublishedBlogsOrderByPublishedDate(@Param("status") BlogStatus status);
    
    Optional<Blog> findByTitle(String title);
    
    boolean existsByTitle(String title);
}
