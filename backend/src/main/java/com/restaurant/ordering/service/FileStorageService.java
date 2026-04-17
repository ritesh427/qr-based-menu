package com.restaurant.ordering.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;

import com.restaurant.ordering.exception.BadRequestException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileStorageService {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp");

    private final Path uploadRoot;

    public FileStorageService(@Value("${app.upload-dir:uploads}") String uploadDir) {
        this.uploadRoot = Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    public String storeMenuImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Image file is required");
        }

        String originalName = StringUtils.cleanPath(file.getOriginalFilename() == null ? "menu-image" : file.getOriginalFilename());
        String extension = getExtension(originalName);
        if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new BadRequestException("Only jpg, jpeg, png, and webp images are supported");
        }

        try {
            Path targetDirectory = uploadRoot.resolve("menu-images");
            Files.createDirectories(targetDirectory);
            String fileName = UUID.randomUUID() + "." + extension;
            Path targetFile = targetDirectory.resolve(fileName);

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, targetFile, StandardCopyOption.REPLACE_EXISTING);
            }

            return "menu-images/" + fileName;
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to store image", ex);
        }
    }

    public Path getUploadRoot() {
        return uploadRoot;
    }

    private String getExtension(String fileName) {
        int index = fileName.lastIndexOf('.');
        if (index < 0 || index == fileName.length() - 1) {
            throw new BadRequestException("Image file must have an extension");
        }
        return fileName.substring(index + 1);
    }
}
