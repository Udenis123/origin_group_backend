package com.org.group.dto.blog;

import com.org.group.model.BlogStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateBlogDto {
    
    private String title;
    private String description;
    private BlogStatus status;
}
