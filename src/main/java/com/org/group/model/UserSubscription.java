package com.org.group.model;

import com.org.group.subscription.SubscriptionPlan;
import com.org.group.subscription.SubscriptionStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "user_subscriptions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSubscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false) // Ensure one user has only one subscription
    private Users user; // Link to the Users entity

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubscriptionPlan plan; // Link to the SubscriptionPlan entity

    @Column(nullable = false)
    private LocalDateTime startDate; // Start date of the subscription

    @Column(nullable = true, updatable = true)
    private LocalDateTime endDate; // End date of the subscription

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubscriptionStatus status = SubscriptionStatus.ACTIVE; // e.g., ACTIVE, INACTIVE, EXPIRED

    @OneToOne(mappedBy = "subscription", cascade = CascadeType.ALL, orphanRemoval = true)
    private Payment payments;
}