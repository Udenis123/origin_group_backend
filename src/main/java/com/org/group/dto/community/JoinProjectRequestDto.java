package com.org.group.dto.community;

import jakarta.validation.constraints.NotBlank;
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
public class JoinProjectRequestDto {
    @NotNull(message = "Community Project ID is required")
    private UUID communityProjectId;
    
    @NotBlank(message = "Description is required")
    private String description;
    
    @NotBlank(message = "Team name is required")
    private String joinedTeam;
}
