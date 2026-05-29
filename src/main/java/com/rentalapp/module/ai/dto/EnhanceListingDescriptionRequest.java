package com.rentalapp.module.ai.dto;

import com.rentalapp.module.listings.entity.AvailabilityStatus;
import com.rentalapp.module.listings.entity.HouseType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EnhanceListingDescriptionRequest {
    @NotBlank(message = "Title is required.")
    @Size(max = 200, message = "Title must not exceed 200 characters.")
    private String title;

    @NotBlank(message = "Description is required.")
    private String description;

    @NotBlank(message = "City is required.")
    private String city;

    @NotBlank(message = "Area is required.")
    private String area;

    @NotNull(message = "House type is required.")
    private HouseType houseType;

    @NotNull(message = "Bedrooms are required.")
    private Integer bedrooms;

    @NotNull(message = "Bathrooms are required.")
    private Integer bathrooms;

    @NotNull(message = "Availability status is required.")
    private AvailabilityStatus availabilityStatus;

    @NotNull(message = "Furnished status is required.")
    private Boolean furnished;

    @NotNull(message = "Rent amount is required.")
    private BigDecimal rentAmount;

    private List<String> amenities;
}
