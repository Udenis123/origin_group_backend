package com.org.group.model.analyzer;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "analytics_feedback")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsFeedback {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    private UUID projectId;
    private UUID analyzerId;
    private String Feedback;
}
