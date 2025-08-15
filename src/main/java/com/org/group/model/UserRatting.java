package com.org.group.model;


import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "user_ratting")
@AllArgsConstructor
@Setter
@Getter
@Builder
public class UserRatting {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    private String message;
    private int starNumber;
    private boolean isRated;
    private RattingStatus status;
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Users users;
    
    public UserRatting() {
        // Default constructor for JPA
    }
}

