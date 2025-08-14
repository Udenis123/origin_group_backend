package com.org.group.model.analyzer;

import com.org.group.model.project.LaunchProject;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnalyticProject {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long analyticId;
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
    private boolean analyticsEnabled;
    private double price;
    private double costOfDevelopment;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private LaunchProject launchProject;


}
