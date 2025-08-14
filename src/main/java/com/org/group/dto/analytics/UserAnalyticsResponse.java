package com.org.group.dto.analytics;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class UserAnalyticsResponse {
    private Long analyticsId;
    private float feasibility;
    private double monthlyIncome;
    private String feasibilityReason;
    private double annualIncome;
    private float roi;
    private String incomeDescription;
    private int totalView;
    private int bookmarks;
    private int interested;
    private String analyticsDocumentUrl;
    private double price;
    private double costOfDevelopment;
    private UUID projectId;
}
