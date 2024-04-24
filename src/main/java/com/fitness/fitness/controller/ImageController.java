package com.fitness.fitness.controller;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class ImageController {

    @GetMapping("/images/{imageName:.+}")
    public ResponseEntity<Resource> getImage(@PathVariable String imageName) {
        try {
            Resource image = new ClassPathResource("images/" + imageName);
            if (!image.exists()) {
                return ResponseEntity.notFound().build();  // Return 404 if not found
            }
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_JPEG)  // Assuming all images are JPEG
                    .body(image);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();  // Return 500 in case of error
        }
    }
}
