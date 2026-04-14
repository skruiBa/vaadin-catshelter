package com.example.demo.service;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class ProfileImageStorageService {

    private static final long MAX_SIZE_BYTES = 2_000_000L;
    private static final Path BASE_DIR = Paths.get("uploads", "profile-images");

    public String storeProfileImage(String username, String originalFileName, byte[] bytes) throws IOException {
        if (bytes == null || bytes.length == 0) {
            throw new IOException("Tiedosto on tyhja.");
        }
        if (bytes.length > MAX_SIZE_BYTES) {
            throw new IOException("Tiedosto on liian suuri (max 2 MB).");
        }

        Files.createDirectories(BASE_DIR);

        String safeUser = sanitize(username);
        String extension = extractExtension(originalFileName);
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

        String fileName = safeUser + "-" + timestamp + extension;
        Path target = BASE_DIR.resolve(fileName);

        Files.write(target, bytes, StandardOpenOption.CREATE_NEW);
        return target.toString().replace('\\', '/');
    }

    private String sanitize(String input) {
        if (input == null || input.isBlank()) {
            return "user";
        }
        return input.toLowerCase().replaceAll("[^a-z0-9_-]", "-");
    }

    private String extractExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return ".bin";
        }
        String ext = fileName.substring(fileName.lastIndexOf('.')).toLowerCase();
        if (ext.equals(".png") || ext.equals(".jpg") || ext.equals(".jpeg")
                || ext.equals(".gif") || ext.equals(".webp")) {
            return ext;
        }
        return ".bin";
    }
}
