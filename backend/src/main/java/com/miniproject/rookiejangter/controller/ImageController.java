package com.miniproject.rookiejangter.controller;

import com.miniproject.rookiejangter.controller.dto.ImageDTO;
import com.miniproject.rookiejangter.exception.BusinessException;
import com.miniproject.rookiejangter.exception.ErrorCode;
import com.miniproject.rookiejangter.service.ImageService;
import com.miniproject.rookiejangter.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/images")
public class ImageController {

    private final ImageService imageService;
    private final FileStorageService fileStorageService;

    @PostMapping
    public ResponseEntity<List<ImageDTO.Response>> uploadImages(
            @RequestParam("productId") Long productId,
            @RequestPart("images") List<MultipartFile> files
    ) {
        if (files == null || files.isEmpty() || files.stream().allMatch(MultipartFile::isEmpty)) {
            return ResponseEntity.badRequest().body(new ArrayList<>());
        }

        List<ImageDTO.Response> uploadedImages = new ArrayList<>();

        for (MultipartFile file : files) {
            if (file.isEmpty()) {
                continue;
            }
            String imageUrl = fileStorageService.uploadFile(file);
            uploadedImages.add(imageService.createImage(productId, imageUrl));
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(uploadedImages);
    }

    @GetMapping("/id/{imageId}")
    public ResponseEntity<ImageDTO.Response> getImageByImageId(@PathVariable Long imageId) {
        return ResponseEntity.ok(imageService.getImageByImageId(imageId));
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<List<ImageDTO.Response>> getImagesByProductId(@PathVariable Long productId) {
        return ResponseEntity.ok(imageService.getImagesByProductId(productId));
    }

    @DeleteMapping("/product/{productId}")
    public ResponseEntity<Void> deleteImagesByProductId(@PathVariable Long productId) {
        try {
            List<ImageDTO.Response> imagesToDelete = imageService.getImagesByProductId(productId);
            if (imagesToDelete.isEmpty()) {
                return ResponseEntity.noContent().build();
            }

            for (ImageDTO.Response image : imagesToDelete) {
                try {
                    fileStorageService.deleteFile(image.getImageUrl());
                } catch (BusinessException e) {
                    System.err.println("Failed to delete image file from storage: " + image.getImageUrl() + ", Error: " + e.getMessage() + " Code: " + e.getErrorCode());
                }
            }

            imageService.deleteImagesByProductId(productId);

            return ResponseEntity.noContent().build();
        } catch (BusinessException e) {
            System.err.println("Business Exception during product images deletion: " + e.getMessage() + " Code: " + e.getErrorCode());
            if (e.getErrorCode() == ErrorCode.PRODUCT_NOT_FOUND) {
                return ResponseEntity.notFound().build();
            }
            throw e;
        } catch (Exception e) {
            System.err.println("Unexpected error during product images deletion: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}