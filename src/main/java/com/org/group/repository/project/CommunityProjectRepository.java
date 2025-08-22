package com.org.group.repository.project;

import com.org.group.dto.LaunchProject.AnalyticStatus;
import com.org.group.model.Users;
import com.org.group.model.project.CommunityProject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CommunityProjectRepository extends JpaRepository<CommunityProject, UUID> {
    
    // Find projects by user
    List<CommunityProject> findByUser(Users user);
    
    // Find projects by status
    List<CommunityProject> findByStatus(AnalyticStatus status);
}
