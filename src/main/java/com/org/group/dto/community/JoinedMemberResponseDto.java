package com.org.group.dto.community;

import com.org.group.model.JoinStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JoinedMemberResponseDto {
    private UUID joinId;
    private UUID userId;
    private String userName;
    private String userEmail;
    private String userPhone;
    private String description;
    private JoinStatus status;
    private String joinedTeam;
    private LocalDateTime createdAt;
}
