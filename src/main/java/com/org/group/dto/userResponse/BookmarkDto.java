package com.org.group.dto.userResponse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookmarkDto {
    private UUID id;
    private String projectName;
    private String projectType; // "LAUNCHED" or "COMMUNITY"
}
