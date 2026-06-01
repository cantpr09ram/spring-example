package com.example.example.photo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/photos")
@Tag(name = "Photos", description = "Local product photo upload and delivery APIs")
public class PhotoController {
    private final PhotoStorageService photoStorageService;

    public PhotoController(PhotoStorageService photoStorageService) {
        this.photoStorageService = photoStorageService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Upload photo",
            description = "Stores one JPG, PNG, or WebP image and returns the URL to use in product imageUrls.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Photo uploaded"),
                    @ApiResponse(responseCode = "400", description = "Invalid or unsupported image"),
                    @ApiResponse(responseCode = "500", description = "Unable to store photo")
            })
    public Map<String, Object> upload(
            @Parameter(description = "Image file to upload. Supported types: JPG, PNG, WebP.")
            @RequestParam("file") MultipartFile file) {
        try {
            PhotoStorageService.StoredPhoto photo = photoStorageService.store(file);
            return Map.of(
                    "filename", photo.filename(),
                    "contentType", photo.contentType(),
                    "size", photo.size(),
                    "url", "/api/photos/" + photo.filename());
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
        } catch (IOException ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to store photo.", ex);
        }
    }

    @GetMapping("/{filename}")
    @Operation(
            summary = "Show photo",
            description = "Returns a previously uploaded photo file by generated filename.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Photo returned"),
                    @ApiResponse(responseCode = "404", description = "Photo not found")
            })
    public ResponseEntity<Resource> show(
            @Parameter(description = "Generated photo filename returned by the upload API.")
            @PathVariable String filename) {
        Path photo = photoStorageService.resolve(filename);
        if (!photo.startsWith(photoStorageService.resolve("")) || !Files.isRegularFile(photo)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Photo not found");
        }

        try {
            Resource resource = new UrlResource(photo.toUri());
            String contentType = Files.probeContentType(photo);
            MediaType mediaType = contentType == null
                    ? MediaType.APPLICATION_OCTET_STREAM
                    : MediaType.parseMediaType(contentType);

            return ResponseEntity.ok()
                    .contentType(mediaType)
                    .header(HttpHeaders.CACHE_CONTROL, "public, max-age=86400")
                    .body(resource);
        } catch (IOException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Photo not found", ex);
        }
    }
}
