package com.rentalapp.module.media.dto;

import com.rentalapp.module.media.entity.MediaType;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ListingMediaResponse {
    private final String id;
    private final MediaType mediaType;
    private final String mediaUrl;
    private final String caption;
    private final Integer displayOrder;
}
