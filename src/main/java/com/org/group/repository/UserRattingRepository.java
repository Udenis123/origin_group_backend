package com.org.group.repository;

import com.org.group.model.RattingStatus;
import com.org.group.model.UserRatting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRattingRepository extends JpaRepository<UserRatting,Long> {
    List<UserRatting> findByStatus(RattingStatus status);
    Optional<UserRatting> findByUsers_Id(UUID userId);
}
