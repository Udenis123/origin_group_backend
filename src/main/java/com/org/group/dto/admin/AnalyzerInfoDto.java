package com.org.group.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalyzerInfoDto {
    private UUID analyzerId;
    private String name;
    private String email;
    private String phone;
    private String expertise;
} 