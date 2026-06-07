package com.sihaniwala.foundationbackend.controller;

import com.sihaniwala.foundationbackend.dto.ApiResponse;
import com.sihaniwala.foundationbackend.entity.GalleryImage;
import com.sihaniwala.foundationbackend.repository.GalleryImageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/gallery")
@RequiredArgsConstructor
public class GalleryController {

    private final GalleryImageRepository galleryRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<List<GalleryImage>>> getGallery(
            @RequestParam(required = false) String category) {
        List<GalleryImage> images;
        if (category != null && !category.isEmpty()) {
            images = galleryRepository.findByCategory(category);
        } else {
            images = galleryRepository.findAll();
        }
        return ResponseEntity.ok(ApiResponse.ok(images));
    }
}
