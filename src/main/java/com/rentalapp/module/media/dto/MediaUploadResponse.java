package com.rentalapp.module.media.dto;

import com.rentalapp.module.media.entity.MediaType;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MediaUploadResponse {
    private String mediaUrl;
    private MediaType mediaType;
    private String fileName;
    private long fileSize;
}
