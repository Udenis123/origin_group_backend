package com.org.group.dto.community;

import com.org.group.model.JoinStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JoinRequestActionDto {
    @NotNull(message = "Join ID is required")
    private UUID joinId;
    
    @NotNull(message = "Action is required")
    private JoinStatus action; // ACCEPTED or REJECTED
    
    private String reason; // Optional reason for rejection
}
