package com.rentalapp.module.media.dto;

import com.rentalapp.module.media.entity.MediaType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ListingMediaRequest {
    @NotNull(message = "Media type is required.")
    private MediaType mediaType;

    @NotBlank(message = "Media URL is required.")
    @Size(max = 1000, message = "Media URL must not exceed 1000 characters.")
    private String mediaUrl;

    @Size(max = 255, message = "Caption must not exceed 255 characters.")
    private String caption;
}
