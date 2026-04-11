package com.rentalapp.module.listings.dto;

import com.rentalapp.module.listings.entity.AvailabilityStatus;
import com.rentalapp.module.listings.entity.HouseType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ListingUpsertRequest {
    @NotBlank(message = "Title is required.")
    @Size(max = 200, message = "Title must not exceed 200 characters.")
    private String title;

    @NotBlank(message = "Description is required.")
    private String description;

    @NotNull(message = "Rent amount is required.")
    @DecimalMin(value = "0.0", inclusive = false, message = "Rent amount must be greater than zero.")
    private BigDecimal rentAmount;

    @DecimalMin(value = "0.0", inclusive = true, message = "Deposit amount cannot be negative.")
    private BigDecimal depositAmount;

    @DecimalMin(value = "0.0", inclusive = true, message = "Agent fee amount cannot be negative.")
    private BigDecimal agentFeeAmount;

    @NotBlank(message = "City is required.")
    private String city;

    @NotBlank(message = "Area is required.")
    private String area;

    @NotNull(message = "Bedrooms is required.")
    @Min(value = 0, message = "Bedrooms cannot be negative.")
    private Integer bedrooms;

    @NotNull(message = "Bathrooms is required.")
    @Min(value = 0, message = "Bathrooms cannot be negative.")
    private Integer bathrooms;

    @NotNull(message = "House type is required.")
    private HouseType houseType;

    @NotNull(message = "Furnished status is required.")
    private Boolean furnished;

    @NotNull(message = "Availability status is required.")
    private AvailabilityStatus availabilityStatus;

    @NotEmpty(message = "At least one amenity should be selected.")
    private List<String> amenityIds;
}
