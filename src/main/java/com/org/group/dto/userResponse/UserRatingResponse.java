package com.org.group.dto.userResponse;

import com.org.group.model.RattingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRatingResponse {
    private Long id;
    private UUID userId;
    private String userName;
    private String userPhoto;
    private String message;
    private int starNumber;
    private RattingStatus status;
    private String subscription; // Changed to String to store filtered plan name
} 