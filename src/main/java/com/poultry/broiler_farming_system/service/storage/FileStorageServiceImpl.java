package com.poultry.broiler_farming_system.service.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Service
public class FileStorageServiceImpl implements FileStorageService {

    @Value("${app.upload.dir}")
    private String uploadDir;

    @Override
    public String store(MultipartFile file, String subDirectory, String filenamePrefix) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("file is required.");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Only image files are allowed.");
        }

        return write(file, subDirectory, filenamePrefix, extensionFor(contentType));
    }

    @Override
    public String storeDocument(MultipartFile file, String subDirectory, String filenamePrefix) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("file is required.");
        }
        String contentType = file.getContentType();
        if (!"application/pdf".equals(contentType)) {
            throw new IllegalArgumentException("Only PDF files are allowed.");
        }

        return write(file, subDirectory, filenamePrefix, ".pdf");
    }

    private String write(MultipartFile file, String subDirectory, String filenamePrefix, String extension) {
        String filename = filenamePrefix + "-" + UUID.randomUUID() + extension;

        try {
            Path targetDir = Path.of(uploadDir, subDirectory);
            Files.createDirectories(targetDir);
            Files.copy(file.getInputStream(), targetDir.resolve(filename));
        } catch (IOException ex) {
            throw new UncheckedIOException("Failed to store uploaded file.", ex);
        }

        return "/uploads/" + subDirectory + "/" + filename;
    }

    @Override
    public void delete(String publicUrl) {
        if (publicUrl == null || !publicUrl.startsWith("/uploads/")) {
            return;
        }
        try {
            Path path = Path.of(uploadDir, publicUrl.substring("/uploads/".length()));
            Files.deleteIfExists(path);
        } catch (IOException ignored) {
            // Best-effort cleanup; a leftover orphaned file is not worth failing the request over.
        }
    }

    private String extensionFor(String contentType) {
        return switch (contentType) {
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            case "image/gif" -> ".gif";
            default -> ".jpg";
        };
    }
}
