package com.org.group.services;

import com.org.group.dto.blog.CreateBlogDto;
import com.org.group.dto.blog.UpdateBlogDto;
import com.org.group.model.Blog;
import com.org.group.model.BlogStatus;
import com.org.group.repository.BlogRepository;
import com.org.group.services.UploadFileServices.CloudinaryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BlogServiceTest {

    @Mock
    private BlogRepository blogRepository;

    @Mock
    private CloudinaryService cloudinaryService;

    @InjectMocks
    private BlogService blogService;

    private Blog testBlog;
    private CreateBlogDto createBlogDto;
    private UpdateBlogDto updateBlogDto;

    @BeforeEach
    void setUp() {
        testBlog = Blog.builder()
                .id(UUID.randomUUID())
                .title("Test Blog")
                .description("Test Description")
                .status(BlogStatus.DRAFT)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        createBlogDto = CreateBlogDto.builder()
                .title("Test Blog")
                .description("Test Description")
                .status(BlogStatus.DRAFT)
                .build();

        updateBlogDto = UpdateBlogDto.builder()
                .title("Updated Blog")
                .description("Updated Description")
                .status(BlogStatus.PUBLISHED)
                .build();
    }

    @Test
    void createBlog_Success() throws IOException {
        // Arrange
        when(blogRepository.existsByTitle(createBlogDto.getTitle())).thenReturn(false);
        when(blogRepository.save(any(Blog.class))).thenReturn(testBlog);

        // Act
        var result = blogService.createBlog(createBlogDto);

        // Assert
        assertNotNull(result);
        assertEquals(testBlog.getTitle(), result.getTitle());
        assertEquals(testBlog.getDescription(), result.getDescription());
        assertEquals(testBlog.getStatus(), result.getStatus());
        verify(blogRepository).existsByTitle(createBlogDto.getTitle());
        verify(blogRepository).save(any(Blog.class));
    }

    @Test
    void createBlog_TitleAlreadyExists_ThrowsException() throws IOException {
        // Arrange
        when(blogRepository.existsByTitle(createBlogDto.getTitle())).thenReturn(true);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> blogService.createBlog(createBlogDto));
        verify(blogRepository).existsByTitle(createBlogDto.getTitle());
        verify(blogRepository, never()).save(any(Blog.class));
    }

    @Test
    void createBlog_WithPublishedStatus_SetsPublishedAt() throws IOException {
        // Arrange
        createBlogDto.setStatus(BlogStatus.PUBLISHED);
        when(blogRepository.existsByTitle(createBlogDto.getTitle())).thenReturn(false);
        when(blogRepository.save(any(Blog.class))).thenReturn(testBlog);

        // Act
        blogService.createBlog(createBlogDto);

        // Assert
        verify(blogRepository).save(argThat(blog -> 
            blog.getStatus() == BlogStatus.PUBLISHED && blog.getPublishedAt() != null
        ));
    }
}
