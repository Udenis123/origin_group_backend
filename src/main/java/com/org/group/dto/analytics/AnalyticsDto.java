package com.org.group.dto.analytics;


import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class AnalyticsDto{

    private UUID projectId;
    @PositiveOrZero(message = "feasibility must be positive or zero")
    private float feasibility;
    @PositiveOrZero(message = "monthly income must be positive or zero")
    private double monthlyIncome;
    @Size( max =3000, message = "must not exceed 3000 characters")
    private String feasibilityReason;
    @PositiveOrZero(message =  "annualIncome zero or positive not negative")
    private double annualIncome;
    @PositiveOrZero(message = "Rate of Interest must be positive or zero")
    private float roi;
    @Size( max = 3000 , message ="must not exceed 3000 characters" )
    private String incomeDescription;

    private double price;
    private double costOfDevelopment;

}
