package com.oakpay.auth.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class KycDocumentStorageService {
    private static final long MAX_FILE_SIZE = 10L * 1024L * 1024L;
    private static final Set<String> ALLOWED_TYPES = Set.of("image/jpeg", "image/png", "image/webp");

    private final Path root;

    public KycDocumentStorageService(
            @Value("${oakpay.kyc.upload-dir:./data/kyc}") String uploadDir) {
        this.root = Path.of(uploadDir).toAbsolutePath().normalize();
    }

    public String store(UUID userId, UUID documentId, String side, MultipartFile file) {
        if (file == null || file.isEmpty()) throw new IllegalArgumentException("Document image is required");
        if (file.getSize() > MAX_FILE_SIZE) throw new IllegalArgumentException("Document image must be 10 MB or smaller");
        String contentType = file.getContentType() == null ? "" : file.getContentType().toLowerCase(Locale.ROOT);
        if (!ALLOWED_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("Only JPEG, PNG or WEBP document images are supported");
        }
        String normalizedSide = side == null ? "" : side.trim().toLowerCase(Locale.ROOT);
        if (!Set.of("front", "back").contains(normalizedSide)) throw new IllegalArgumentException("Document side must be front or back");
        String extension = switch (contentType) {
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            default -> ".jpg";
        };
        Path targetDir = root.resolve(userId.toString()).normalize();
        Path target = targetDir.resolve(documentId + "-" + normalizedSide + extension).normalize();
        if (!target.startsWith(root)) throw new IllegalArgumentException("Invalid document storage path");
        try {
            Files.createDirectories(targetDir);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            return target.toString();
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to store identity document", ex);
        }
    }
}
