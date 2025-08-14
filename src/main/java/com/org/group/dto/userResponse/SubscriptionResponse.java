package com.org.group.dto.userResponse;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
public class SubscriptionResponse {
    private long id;
    private UUID userId;
    private String plan;
    private String status;
    private LocalDateTime startDate;
    private LocalDateTime endDate;

}
