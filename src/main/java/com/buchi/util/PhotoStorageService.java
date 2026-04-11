package com.buchi.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Component
@Slf4j
public class PhotoStorageService {

    @Value("${app.photo.upload-dir}")
    private String uploadDir;

    @Value("${app.photo.base-url}")
    private String baseUrl;

    public record StoredPhoto(String filePath, String url) {}

    public StoredPhoto store(MultipartFile file) throws IOException {
        Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        Files.createDirectories(uploadPath);

        String original = file.getOriginalFilename();
        String extension = (original != null && original.contains("."))
                ? original.substring(original.lastIndexOf("."))
                : ".jpg";

        String filename = UUID.randomUUID() + extension;
        Path target = uploadPath.resolve(filename);
        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

        String relativePath = uploadDir + "/" + filename;
        String url = baseUrl + "/photos/" + filename;

        log.info("Stored photo: {}", relativePath);
        return new StoredPhoto(relativePath, url);
    }

    public void delete(String filePath) {
        try {
            Path path = Paths.get(filePath).toAbsolutePath().normalize();
            Files.deleteIfExists(path);
        } catch (IOException e) {
            log.warn("Could not delete photo: {}", filePath);
        }
    }
}