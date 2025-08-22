package com.org.group.model.project;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class TeamMember {
    @JsonProperty("title")
    private String title;
    
    @JsonProperty("number")
    private int number;
}
