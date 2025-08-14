package com.org.group.repository;

import com.org.group.model.UserSubscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserSubscriptionRepository extends JpaRepository<UserSubscription,Long> {
    Optional<List<UserSubscription>> findByUser_Id(UUID userId);
}
