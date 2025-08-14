package com.org.group.services.LaunchProject;

import com.org.group.dto.LaunchProject.AnalyticStatus;
import com.org.group.model.Users;
import com.org.group.model.project.Bookmark;
import com.org.group.model.project.LaunchProject;
import com.org.group.repository.UserRepository;
import com.org.group.repository.project.BookmarkRepository;
import com.org.group.repository.project.LaunchProjectRepository;
import com.org.group.responses.project.BookmarkedProjectResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class BookmarkServices {

    private final BookmarkRepository bookmarkRepository;
    private final UserRepository userRepository;
    private final LaunchProjectRepository launchProjectRepository;

    public BookmarkServices(BookmarkRepository bookmarkRepository, UserRepository userRepository, LaunchProjectRepository launchProjectRepository) {
        this.bookmarkRepository = bookmarkRepository;
        this.userRepository = userRepository;
        this.launchProjectRepository = launchProjectRepository;
    }

    public void bookmarkProject(UUID userId, UUID projectId) {
        if (bookmarkRepository.existsByUserIdAndProjectProjectId(userId, projectId)) {
            throw new RuntimeException("Project already bookmarked");
        }

        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        LaunchProject project = launchProjectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));
        project.setCountBookmark(project.getCountBookmark() + 1);

        launchProjectRepository.save(project);

        Bookmark bookmark = Bookmark.builder()
                .user(user)
                .project(project)
                .bookmarkedAt(LocalDateTime.now())
                .build();

        bookmarkRepository.save(bookmark);
    }
    public ResponseEntity<List<BookmarkedProjectResponse>> getBookmarkedProjects(UUID userId) {
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found for bookmarked projects"));

        List<BookmarkedProjectResponse> bookmarkedProjects = user.getBookmarks().stream()
                .filter(bookmark -> bookmark.getProject().getStatus() != AnalyticStatus.DECLINED)
                .map(bookmark -> {
                    LaunchProject project = bookmark.getProject();
                    return BookmarkedProjectResponse.builder()
                            .projectId(project.getProjectId())
                            .clientName(project.getClientName())
                            .analyticStatus(project.getStatus())
                            .projectDescription(project.getDescription())
                            .projectUrl(project.getProjectPhotoUrl())
                            .category(project.getCategory())
                            .projectName(project.getProjectName())
                            .bookmarkedDate(bookmark.getBookmarkedAt())
                            .projectPurpose(project.getProjectPurpose())
                            .build();
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(bookmarkedProjects);
    }


    public void deleteBookmark(UUID userId, UUID projectId) {
        if (userId == null || projectId == null) {
            throw new RuntimeException("User ID and Project ID cannot be null");
        }

        Optional<Bookmark> bookmark = bookmarkRepository.findByUserIdAndProjectProjectId(userId, projectId);
        
        if (bookmark.isEmpty()) {
            throw new RuntimeException("Bookmark not found for the given user and project");
        }

        LaunchProject project = bookmark.get().getProject();
        if (project.getCountBookmark() > 0) {
            project.setCountBookmark(project.getCountBookmark() - 1);
            launchProjectRepository.save(project);
        }

        bookmarkRepository.delete(bookmark.get());
    }

}
