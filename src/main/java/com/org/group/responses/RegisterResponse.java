package com.org.group.responses;

import com.org.group.subscription.SubscriptionPlan;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
public class RegisterResponse {
    private String email;
    private UUID id;
    private List<SubscriptionPlan> plan;
    private String message;
}
