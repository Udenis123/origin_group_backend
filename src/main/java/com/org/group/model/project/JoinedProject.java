package com.org.group.model.project;


import com.org.group.model.JoinStatus;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class JoinedProject {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "join_id", updatable = false, nullable = false)
    private UUID id;

    @Column(nullable = false)
    private UUID userId;
    @Column(nullable = false)
    private UUID communityProjectId;
    @Column(nullable = false ,length = 3000)
    private String description;
    @Column(nullable = false)
    private JoinStatus status;
    @Column(nullable = false)
    private  String joinedTeam;
}
