package com.org.group.responses.project;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MyProjectResponse {
    private UUID projectId;
    private String description;
    private String title;
    private LocalDateTime submittedOn;
    private LocalDateTime updatedOn;
}
