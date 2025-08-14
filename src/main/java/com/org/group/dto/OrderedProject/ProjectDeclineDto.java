package com.org.group.dto.OrderedProject;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class ProjectDeclineDto {

    private UUID projectId;
    private String reason;

}
