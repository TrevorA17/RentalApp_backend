package com.rentalapp.module.media.service;

import com.rentalapp.module.media.dto.MediaUploadResponse;
import org.springframework.web.multipart.MultipartFile;

public interface IListingMediaUploadService {
    MediaUploadResponse uploadListingImage(MultipartFile file);
}
