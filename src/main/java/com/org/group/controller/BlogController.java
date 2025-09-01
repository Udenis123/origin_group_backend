package com.org.group.controller;

import com.org.group.dto.blog.BlogResponseDto;
import com.org.group.dto.blog.CreateBlogDto;
import com.org.group.dto.blog.UpdateBlogDto;
import com.org.group.model.BlogStatus;
import com.org.group.services.BlogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@Tag(name = "Blog Controller", description = "Blog management APIs")
@RequestMapping("/api/blogs")
@CrossOrigin("http://localhost:4201")
@RequiredArgsConstructor
public class BlogController {
    
    private static final Logger logger = LoggerFactory.getLogger(BlogController.class);
    private final BlogService blogService;
    
    @Operation(
            summary = "Create a new blog",
            description = "Create a new blog post with title, description, status, and photo in a single request"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Blog created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input or title already exists"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BlogResponseDto> createBlog(
            @RequestPart("blog") String blogJson,
            @RequestPart(value = "photo", required = false) MultipartFile photo) throws IOException {
        
        logger.info("Received blog creation request with JSON: {}", blogJson);
        logger.info("Photo file: {}", photo != null ? photo.getOriginalFilename() : "null");
        
        // Parse the JSON string to CreateBlogDto
        ObjectMapper objectMapper = new ObjectMapper();
        CreateBlogDto createBlogDto;
        try {
            createBlogDto = objectMapper.readValue(blogJson, CreateBlogDto.class);
            logger.info("Parsed blog DTO: title={}, description={}, status={}", 
                createBlogDto.getTitle(), createBlogDto.getDescription(), createBlogDto.getStatus());
        } catch (Exception e) {
            logger.error("Failed to parse blog JSON: {}", e.getMessage());
            throw new IllegalArgumentException("Invalid JSON format for blog data: " + e.getMessage());
        }
        
        // Validate the parsed DTO
        if (createBlogDto.getTitle() == null || createBlogDto.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Blog title is required");
        }
        if (createBlogDto.getDescription() == null || createBlogDto.getDescription().trim().isEmpty()) {
            throw new IllegalArgumentException("Blog description is required");
        }
        
        BlogResponseDto createdBlog = blogService.createBlog(createBlogDto, photo);
        logger.info("Blog created successfully with ID: {}", createdBlog.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(createdBlog);
    }
    
    @Operation(
            summary = "Create a new blog (JSON only)",
            description = "Create a new blog post with title, description, and status (no photo)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Blog created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input or title already exists"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping(value = "/json", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BlogResponseDto> createBlogJson(@Valid @RequestBody CreateBlogDto createBlogDto) throws IOException {
        BlogResponseDto createdBlog = blogService.createBlog(createBlogDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdBlog);
    }
    

    
    @Operation(
            summary = "Get all blogs",
            description = "Retrieve all blogs regardless of status"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Blogs retrieved successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping
    public ResponseEntity<List<BlogResponseDto>> getAllBlogs() {
        List<BlogResponseDto> blogs = blogService.getAllBlogs();
        return ResponseEntity.ok(blogs);
    }
    
    @Operation(
            summary = "Get blog by ID",
            description = "Retrieve a specific blog by its ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Blog retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Blog not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/{blogId}")
    public ResponseEntity<BlogResponseDto> getBlogById(@PathVariable UUID blogId) {
        BlogResponseDto blog = blogService.getBlogById(blogId);
        return ResponseEntity.ok(blog);
    }
    
    @Operation(
            summary = "Get published blogs",
            description = "Retrieve only published blogs ordered by publication date"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Published blogs retrieved successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/published")
    public ResponseEntity<List<BlogResponseDto>> getPublishedBlogs() {
        List<BlogResponseDto> blogs = blogService.getPublishedBlogs();
        return ResponseEntity.ok(blogs);
    }
    
    @Operation(
            summary = "Get blogs by status",
            description = "Retrieve blogs filtered by status (DRAFT or PUBLISHED)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Blogs retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid status"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/status/{status}")
    public ResponseEntity<List<BlogResponseDto>> getBlogsByStatus(@PathVariable BlogStatus status) {
        List<BlogResponseDto> blogs = blogService.getBlogsByStatus(status);
        return ResponseEntity.ok(blogs);
    }
    
    @Operation(
            summary = "Update blog",
            description = "Update an existing blog's title, description, or status"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Blog updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input or title already exists"),
            @ApiResponse(responseCode = "404", description = "Blog not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PutMapping("/{blogId}")
    public ResponseEntity<BlogResponseDto> updateBlog(
            @PathVariable UUID blogId,
            @Valid @RequestBody UpdateBlogDto updateBlogDto) {
        BlogResponseDto updatedBlog = blogService.updateBlog(blogId, updateBlogDto);
        return ResponseEntity.ok(updatedBlog);
    }
    
    @Operation(
            summary = "Upload blog photo",
            description = "Upload a photo for a blog. If a photo already exists, it will be replaced."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Photo uploaded successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid file"),
            @ApiResponse(responseCode = "404", description = "Blog not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping(value = "/{blogId}/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BlogResponseDto> uploadBlogPhoto(
            @PathVariable UUID blogId,
            @RequestParam("photo") MultipartFile photo) throws IOException {
        BlogResponseDto updatedBlog = blogService.uploadBlogPhoto(blogId, photo);
        return ResponseEntity.ok(updatedBlog);
    }
    
    @Operation(
            summary = "Update blog status",
            description = "Change the status of a blog (DRAFT to PUBLISHED or vice versa)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Status updated successfully"),
            @ApiResponse(responseCode = "404", description = "Blog not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PatchMapping("/{blogId}/status")
    public ResponseEntity<BlogResponseDto> updateBlogStatus(
            @PathVariable UUID blogId,
            @RequestParam BlogStatus status) {
        BlogResponseDto updatedBlog = blogService.updateBlogStatus(blogId, status);
        return ResponseEntity.ok(updatedBlog);
    }
    
    @Operation(
            summary = "Delete blog",
            description = "Delete a blog and its associated photo from Cloudinary"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Blog deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Blog not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @DeleteMapping("/{blogId}")
    public ResponseEntity<Void> deleteBlog(@PathVariable UUID blogId) {
        blogService.deleteBlog(blogId);
        return ResponseEntity.noContent().build();
    }
}
