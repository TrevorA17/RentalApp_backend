package com.rentalapp.module.media.service.impl;

import com.rentalapp.config.AppMediaProperties;
import com.rentalapp.exception.ForbiddenException;
import com.rentalapp.exception.ValidationException;
import com.rentalapp.module.auth.entity.Role;
import com.rentalapp.module.auth.entity.User;
import com.rentalapp.module.auth.repository.UserRepository;
import com.rentalapp.module.media.dto.MediaUploadResponse;
import com.rentalapp.module.media.entity.MediaType;
import com.rentalapp.module.media.service.ListingMediaUploadService;
import com.rentalapp.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ListingMediaUploadServiceImpl implements ListingMediaUploadService {
    private static final Set<String> SUPPORTED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp");

    private final AppMediaProperties appMediaProperties;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public MediaUploadResponse uploadListingImage(MultipartFile file) {
        User actor = requirePoster();
        validateFile(file);

        String extension = extractExtension(file.getOriginalFilename());
        String generatedFileName = UUID.randomUUID() + "." + extension;
        Path storageRoot = Path.of(appMediaProperties.storagePath()).toAbsolutePath().normalize();
        Path listingMediaDirectory = storageRoot.resolve(Path.of("listings", actor.getId())).normalize();
        Path targetFile = listingMediaDirectory.resolve(generatedFileName).normalize();

        if (!targetFile.startsWith(storageRoot)) {
            throw new ValidationException("Invalid upload target.");
        }

        try {
            Files.createDirectories(listingMediaDirectory);
            Files.copy(file.getInputStream(), targetFile, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException exception) {
            throw new ValidationException("Failed to store uploaded media.");
        }

        String publicBaseUrl = appMediaProperties.publicBaseUrl().endsWith("/")
                ? appMediaProperties.publicBaseUrl().substring(0, appMediaProperties.publicBaseUrl().length() - 1)
                : appMediaProperties.publicBaseUrl();

        return MediaUploadResponse.builder()
                .mediaUrl(publicBaseUrl + "/media/listings/" + actor.getId() + "/" + generatedFileName)
                .mediaType(MediaType.IMAGE)
                .fileName(generatedFileName)
                .fileSize(file.getSize())
                .build();
    }

    private User requirePoster() {
        String userId = SecurityUtils.requireCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ValidationException("User not found."));

        if (user.getRole() != Role.AGENT && user.getRole() != Role.LANDLORD) {
            throw new ForbiddenException("Only agents and landlords can upload listing media.");
        }

        return user;
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ValidationException("Select an image to upload.");
        }

        if (file.getSize() > appMediaProperties.maxFileSizeBytes()) {
            throw new ValidationException("Image exceeds the allowed upload size.");
        }

        String contentType = file.getContentType();
        if (contentType == null || !appMediaProperties.allowedContentTypes().contains(contentType)) {
            throw new ValidationException("Only JPG, PNG, and WebP images are allowed.");
        }

        String extension = extractExtension(file.getOriginalFilename());
        if (!SUPPORTED_EXTENSIONS.contains(extension)) {
            throw new ValidationException("Unsupported image file extension.");
        }
    }

    private String extractExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            throw new ValidationException("Uploaded image must have a valid file extension.");
        }

        return fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
    }
}
