package com.sihaniwala.foundationbackend.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.sihaniwala.foundationbackend.exception.BadRequestException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.util.Map;

@Service
@Slf4j
public class CloudinaryGalleryService {

    private final String cloudName;
    private final String apiKey;
    private final String apiSecret;
    private final String folder;

    public CloudinaryGalleryService(
            @Value("${cloudinary.cloud-name:}") String cloudName,
            @Value("${cloudinary.api-key:}") String apiKey,
            @Value("${cloudinary.api-secret:}") String apiSecret,
            @Value("${cloudinary.folder:sihaniwala/gallery}") String folder) {
        this.cloudName = cloudName;
        this.apiKey = apiKey;
        this.apiSecret = apiSecret;
        this.folder = folder;
    }

    public CloudinaryUpload upload(MultipartFile file) {
        validateImage(file);
        Cloudinary cloudinary = cloudinaryClient();

        try {
            Map<?, ?> result = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                    "resource_type", "image",
                    "folder", folder));
            String secureUrl = String.valueOf(result.get("secure_url"));
            String publicId = String.valueOf(result.get("public_id"));
            if (secureUrl.isBlank() || publicId.isBlank()
                    || "null".equals(secureUrl) || "null".equals(publicId)) {
                throw new IOException("Cloudinary returned an incomplete upload result");
            }
            return new CloudinaryUpload(secureUrl, publicId);
        } catch (Exception ex) {
            log.warn("Gallery image upload to Cloudinary failed");
            throw new BadRequestException("Gallery image upload failed");
        }
    }

    public void delete(String publicId) {
        if (publicId == null || publicId.isBlank()) {
            return;
        }

        try {
            Map<?, ?> result = cloudinaryClient().uploader().destroy(publicId, ObjectUtils.asMap(
                    "resource_type", "image",
                    "invalidate", true));
            if (!"ok".equals(result.get("result")) && !"not found".equals(result.get("result"))) {
                throw new IOException("Cloudinary did not confirm deletion");
            }
        } catch (Exception ex) {
            log.warn("Gallery image deletion from Cloudinary failed");
            throw new BadRequestException("Gallery image deletion failed");
        }
    }

    private Cloudinary cloudinaryClient() {
        if (cloudName.isBlank() || apiKey.isBlank() || apiSecret.isBlank()) {
            throw new BadRequestException("Cloudinary is not configured for gallery uploads");
        }
        return new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret));
    }

    private void validateImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Cannot upload an empty image");
        }
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new BadRequestException("File size exceeds 5MB limit");
        }
        String contentType = file.getContentType();
        if (contentType == null || (!contentType.equals("image/jpeg")
                && !contentType.equals("image/png")
                && !contentType.equals("image/jpg"))) {
            throw new BadRequestException("Only JPG and PNG images are allowed");
        }
        try {
            if (ImageIO.read(file.getInputStream()) == null) {
                throw new BadRequestException("Uploaded file is not a valid image");
            }
        } catch (IOException ex) {
            throw new BadRequestException("Unable to validate uploaded image");
        }
    }

    public record CloudinaryUpload(String secureUrl, String publicId) {}
}