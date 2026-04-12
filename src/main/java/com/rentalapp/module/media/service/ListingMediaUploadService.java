package com.rentalapp.module.media.service;

import com.rentalapp.module.media.dto.MediaUploadResponse;
import org.springframework.web.multipart.MultipartFile;

public interface ListingMediaUploadService {
    MediaUploadResponse uploadListingImage(MultipartFile file);
}
