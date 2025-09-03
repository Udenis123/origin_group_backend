package com.org.group.dto.userResponse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDetailResponseDto {
    private UserBasicInfoDto userInfo;
    private String currentSubscription;
    private List<LaunchedProjectDto> launchedProjects;
    private List<CommunityProjectDto> communityProjects;
    private List<BookmarkDto> bookmarks;
}
