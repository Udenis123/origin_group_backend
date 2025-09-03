package com.org.group.services.UploadFileServices;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@Service
public class CloudinaryService {
    private final Cloudinary cloudinary;

    public CloudinaryService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }


    public void deleteFile(String fileUrl) {
        try {
            if (fileUrl != null && !fileUrl.isEmpty()) {
                String publicId = extractPublicId(fileUrl);
                
                // Try to delete as image first (most common)
                try {
                    cloudinary.uploader().destroy(publicId, ObjectUtils.asMap("resource_type", "image"));
                } catch (Exception imageEx) {
                    // If image deletion fails, try video
                    try {
                        cloudinary.uploader().destroy(publicId, ObjectUtils.asMap("resource_type", "video"));
                    } catch (Exception videoEx) {
                        // If video deletion fails, try raw (documents)
                        try {
                            cloudinary.uploader().destroy(publicId, ObjectUtils.asMap("resource_type", "raw"));
                        } catch (Exception rawEx) {
                            // If all fail, try without resource type
                            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete file from Cloudinary", e);
        }
    }

    public String uploadFile(MultipartFile file, String oldImageUrl) throws IOException {

        BufferedImage image = ImageIO.read(file.getInputStream());
        if (image == null) {
            throw new RuntimeException("Invalid file format. Only image files are allowed.");
        }
        if (oldImageUrl != null && !oldImageUrl.isEmpty()) {
            deleteFile(oldImageUrl);
        }
        String uniqueFilename = "profile_" + UUID.randomUUID();
        Map<String, Object> uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                "public_id", uniqueFilename,
                "use_filename", true,
                "unique_filename", false,
                "overwrite", true
        ));
        return uploadResult.get("secure_url").toString();
    }

    private String extractPublicId(String imageUrl) {
        try {
            // Remove query parameters if they exist
            String cleanUrl = imageUrl.split("\\?")[0];
            
            // Split by '/' and get the filename
            String[] parts = cleanUrl.split("/");
            String filenameWithExtension = parts[parts.length - 1];
            
            // Remove file extension if it exists
            int lastDotIndex = filenameWithExtension.lastIndexOf('.');
            if (lastDotIndex > 0) {
                return filenameWithExtension.substring(0, lastDotIndex);
            } else {
                return filenameWithExtension; // No extension found
            }
        } catch (Exception e) {
            // Fallback: try to extract from the last part of URL
            String[] parts = imageUrl.split("/");
            return parts[parts.length - 1];
        }
    }

    public String uploadProjectPhoto(MultipartFile file) throws IOException {
        return uploadToCloudinary(file, "image");
    }

    public String uploadProjectPlan(MultipartFile file) throws IOException {
        return uploadToCloudinary(file, "auto"); // For PDFs, DOCX, etc.
    }

    public String uploadProjectIdea(MultipartFile file) throws IOException {
        return uploadToCloudinary(file, "auto"); // For text documents, mind maps, etc.
    }

    public String uploadAnalyticsDocs(MultipartFile file) throws IOException {
        return uploadToCloudinary(file, "auto"); // For text documents, mind maps, etc.
    }

    public String uploadProjectVideo(MultipartFile file) throws IOException {
        return uploadToCloudinary(file, "video"); // Ensure video is uploaded as a video
    }

    private String uploadToCloudinary(MultipartFile file, String resourceType) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("File is empty or null.");
        }

        String uniqueFilename = resourceType + "_" + UUID.randomUUID();
        Map<String, Object> uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                "resource_type", resourceType,
                "public_id", uniqueFilename,
                "use_filename", true,
                "unique_filename", false,
                "overwrite", true
        ));

        return uploadResult.get("secure_url").toString();
    }


}