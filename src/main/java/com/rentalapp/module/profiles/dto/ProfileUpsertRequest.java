package com.rentalapp.module.profiles.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class ProfileUpsertRequest {
    @NotBlank(message = "Full name is required.")
    @Size(max = 150, message = "Full name must not exceed 150 characters.")
    private String fullName;

    @Size(max = 30, message = "Phone number must not exceed 30 characters.")
    private String phoneNumber;

    private String bio;
    private String profilePhotoUrl;
    private String city;
    private List<String> serviceAreas;
    private String companyName;
    private String feeStructure;
}
