package com.org.group.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private Users user; // Link to the Users entity

    @OneToOne
    @JoinColumn(name = "subscription_id", nullable = false)
    private UserSubscription subscription; // Link to the UserSubscription entity

    @Column(nullable = false)
    private BigDecimal amount; // Amount paid

    @Column(nullable = false)
    private String paymentMethod; // e.g., CARD, MOBILE_MONEY, AIRTEL_MONEY

    @Column(nullable = false, unique = true)
    private String transactionId; // Unique transaction ID from the payment gateway

    @Column(nullable = false)
    private String status; // e.g., SUCCESS, FAILED

    @Column(nullable = false)
    private LocalDateTime paymentDate; // Timestamp of the payment
}