package com.wealthpro.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
public class FileStorageService {

    @Value("${file.upload.dir}")
    private String uploadDir;

    /**
     * Saves the uploaded image to local folder
     * Returns the file path saved in DB
     */
    public String saveFile(MultipartFile file, Long clientId, String documentType) {
        try {
            // Create folder if it doesn't exist

            Path clientFolder = Paths.get(uploadDir, "client_" + clientId);
            Files.createDirectories(clientFolder);

            // Build unique filename
            // e.g. PAN_1741234567890.jpg
            String originalFilename = file.getOriginalFilename();

            String extension;
            if (originalFilename != null) {
                int dotIndex = originalFilename.lastIndexOf('.');
                if (dotIndex >= 0 && dotIndex < originalFilename.length() - 1) {
                    extension = originalFilename.substring(dotIndex);
                } else {
                    extension = ".jpg";
                }
            } else {
                extension = ".jpg";
            }

            String fileName = documentType + "_" + System.currentTimeMillis() + extension;

            // Save file to local folder
            Path filePath = clientFolder.resolve(fileName);
            try (InputStream in = file.getInputStream()) {
                Files.copy(in, filePath, StandardCopyOption.REPLACE_EXISTING);
            }

            // Return the path string to save in DB
            // e.g C:/wealthpro/kyc-documents/client_1/PAN_1741234567890.jpg
            return filePath.toString();

        } catch (IOException e) {
            throw new RuntimeException("Failed to save file locally: " + e.getMessage());
        }
    }

    public void deleteFile(String filePath) {
        try {
            if (filePath != null) {
                Path path = Paths.get(filePath);
                Files.deleteIfExists(path);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete file: " + e.getMessage());
        }
    }

}