package com.example.example.photo;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.imageio.ImageIO;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class PhotoStorageService {
    private static final Map<String, String> EXTENSIONS = Map.of(
            "image/jpeg", ".jpg",
            "image/png", ".png",
            "image/webp", ".webp");

    private static final Set<String> ALLOWED_TYPES = EXTENSIONS.keySet();

    private final Path uploadDirectory;

    public PhotoStorageService(@Value("${app.upload-dir}") String uploadDir) throws IOException {
        this.uploadDirectory = Path.of(uploadDir).toAbsolutePath().normalize();
        Files.createDirectories(this.uploadDirectory);
    }

    public StoredPhoto store(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Photo is required.");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("Only JPG, PNG, or WebP images are supported.");
        }

        validateImage(file, contentType);

        String filename = UUID.randomUUID() + EXTENSIONS.get(contentType);
        Path destination = uploadDirectory.resolve(filename).normalize();

        if (!destination.startsWith(uploadDirectory)) {
            throw new IllegalArgumentException("Invalid storage path.");
        }

        try (InputStream input = file.getInputStream()) {
            Files.copy(input, destination, StandardCopyOption.REPLACE_EXISTING);
        }

        return new StoredPhoto(filename, contentType, file.getSize());
    }

    public Path resolve(String filename) {
        return uploadDirectory.resolve(filename).normalize();
    }

    private static void validateImage(MultipartFile file, String contentType) throws IOException {
        if ("image/webp".equals(contentType)) {
            byte[] header = file.getInputStream().readNBytes(12);
            boolean validWebp = header.length == 12
                    && Arrays.equals(Arrays.copyOfRange(header, 0, 4), new byte[] { 'R', 'I', 'F', 'F' })
                    && Arrays.equals(Arrays.copyOfRange(header, 8, 12), new byte[] { 'W', 'E', 'B', 'P' });
            if (!validWebp) {
                throw new IllegalArgumentException("File is not a valid WebP image.");
            }
            return;
        }

        try (InputStream input = file.getInputStream()) {
            BufferedImage image = ImageIO.read(input);
            if (image == null) {
                throw new IllegalArgumentException("File is not a valid image.");
            }
        }
    }

    public record StoredPhoto(
            String filename,
            String contentType,
            long size
    ) {
    }
}
