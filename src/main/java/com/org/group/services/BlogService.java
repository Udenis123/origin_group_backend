package com.org.group.services;

import com.org.group.dto.blog.BlogResponseDto;
import com.org.group.dto.blog.CreateBlogDto;
import com.org.group.dto.blog.UpdateBlogDto;
import com.org.group.model.Blog;
import com.org.group.model.BlogStatus;
import com.org.group.repository.BlogRepository;
import com.org.group.services.UploadFileServices.CloudinaryService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BlogService {
    
    private final BlogRepository blogRepository;
    private final CloudinaryService cloudinaryService;
    
    public BlogResponseDto createBlog(CreateBlogDto createBlogDto, MultipartFile photo) throws IOException {
        // Check if blog with same title already exists
        if (blogRepository.existsByTitle(createBlogDto.getTitle())) {
            throw new IllegalArgumentException("Blog with title '" + createBlogDto.getTitle() + "' already exists");
        }
        
        Blog blog = Blog.builder()
                .title(createBlogDto.getTitle())
                .description(createBlogDto.getDescription())
                .status(createBlogDto.getStatus())
                .build();
        
        // Set publishedAt if status is PUBLISHED
        if (blog.getStatus() == BlogStatus.PUBLISHED) {
            blog.setPublishedAt(LocalDateTime.now());
        }
        
        // Save blog first to get the ID
        Blog savedBlog = blogRepository.save(blog);
        
        // Upload photo if provided
        if (photo != null && !photo.isEmpty()) {
            String photoUrl = cloudinaryService.uploadFile(photo, null);
            savedBlog.setPhotoUrl(photoUrl);
            savedBlog = blogRepository.save(savedBlog);
        }
        
        return mapToResponseDto(savedBlog);
    }
    
    public BlogResponseDto createBlog(CreateBlogDto createBlogDto) throws IOException {
        return createBlog(createBlogDto, null);
    }
    
    public BlogResponseDto updateBlog(UUID blogId, UpdateBlogDto updateBlogDto) {
        Blog existingBlog = blogRepository.findById(blogId)
                .orElseThrow(() -> new EntityNotFoundException("Blog with id " + blogId + " not found"));
        
        // Check if new title conflicts with existing blogs (excluding current blog)
        if (updateBlogDto.getTitle() != null && !updateBlogDto.getTitle().equals(existingBlog.getTitle())) {
            if (blogRepository.existsByTitle(updateBlogDto.getTitle())) {
                throw new IllegalArgumentException("Blog with title '" + updateBlogDto.getTitle() + "' already exists");
            }
        }
        
        // Update fields if provided
        if (updateBlogDto.getTitle() != null) {
            existingBlog.setTitle(updateBlogDto.getTitle());
        }
        if (updateBlogDto.getDescription() != null) {
            existingBlog.setDescription(updateBlogDto.getDescription());
        }
        if (updateBlogDto.getStatus() != null) {
            existingBlog.setStatus(updateBlogDto.getStatus());
            // Set publishedAt if status changes to PUBLISHED
            if (updateBlogDto.getStatus() == BlogStatus.PUBLISHED && existingBlog.getPublishedAt() == null) {
                existingBlog.setPublishedAt(LocalDateTime.now());
            }
        }
        
        Blog updatedBlog = blogRepository.save(existingBlog);
        return mapToResponseDto(updatedBlog);
    }
    
    public BlogResponseDto uploadBlogPhoto(UUID blogId, MultipartFile photo) throws IOException {
        Blog blog = blogRepository.findById(blogId)
                .orElseThrow(() -> new EntityNotFoundException("Blog with id " + blogId + " not found"));
        
        // Delete existing photo if exists
        if (blog.getPhotoUrl() != null && !blog.getPhotoUrl().isEmpty()) {
            cloudinaryService.deleteFile(blog.getPhotoUrl());
        }
        
        // Upload new photo
        String photoUrl = cloudinaryService.uploadFile(photo, null);
        blog.setPhotoUrl(photoUrl);
        
        Blog updatedBlog = blogRepository.save(blog);
        return mapToResponseDto(updatedBlog);
    }
    
    public void deleteBlog(UUID blogId) {
        Blog blog = blogRepository.findById(blogId)
                .orElseThrow(() -> new EntityNotFoundException("Blog with id " + blogId + " not found"));
        
        // Delete photo from Cloudinary if exists
        if (blog.getPhotoUrl() != null && !blog.getPhotoUrl().isEmpty()) {
            cloudinaryService.deleteFile(blog.getPhotoUrl());
        }
        
        blogRepository.delete(blog);
    }
    
    public BlogResponseDto getBlogById(UUID blogId) {
        Blog blog = blogRepository.findById(blogId)
                .orElseThrow(() -> new EntityNotFoundException("Blog with id " + blogId + " not found"));
        return mapToResponseDto(blog);
    }
    
    public List<BlogResponseDto> getAllBlogs() {
        List<Blog> blogs = blogRepository.findAll();
        return blogs.stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }
    
    public List<BlogResponseDto> getBlogsByStatus(BlogStatus status) {
        List<Blog> blogs = blogRepository.findByStatusOrderByPublishedAtDesc(status);
        return blogs.stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }
    
    public List<BlogResponseDto> getPublishedBlogs() {
        List<Blog> blogs = blogRepository.findPublishedBlogsOrderByPublishedDate(BlogStatus.PUBLISHED);
        return blogs.stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }
    
    public BlogResponseDto updateBlogStatus(UUID blogId, BlogStatus newStatus) {
        Blog blog = blogRepository.findById(blogId)
                .orElseThrow(() -> new EntityNotFoundException("Blog with id " + blogId + " not found"));
        
        blog.setStatus(newStatus);
        
        // Set publishedAt if status changes to PUBLISHED
        if (newStatus == BlogStatus.PUBLISHED && blog.getPublishedAt() == null) {
            blog.setPublishedAt(LocalDateTime.now());
        }
        
        Blog updatedBlog = blogRepository.save(blog);
        return mapToResponseDto(updatedBlog);
    }
    
    private BlogResponseDto mapToResponseDto(Blog blog) {
        return BlogResponseDto.builder()
                .id(blog.getId())
                .title(blog.getTitle())
                .description(blog.getDescription())
                .photoUrl(blog.getPhotoUrl())
                .status(blog.getStatus())
                .createdAt(blog.getCreatedAt())
                .updatedAt(blog.getUpdatedAt())
                .publishedAt(blog.getPublishedAt())
                .build();
    }
}
