package com.rentalapp.module.listings.dto;

import org.springframework.data.domain.Sort;

public enum ListingSortOption {
    PUBLISHED_AT_DESC,
    RENT_AMOUNT_ASC,
    RENT_AMOUNT_DESC,
    CREATED_AT_DESC;

    public Sort toSort() {
        return switch (this) {
            case PUBLISHED_AT_DESC -> Sort.by(Sort.Order.desc("publishedAt"), Sort.Order.desc("createdAt"));
            case RENT_AMOUNT_ASC -> Sort.by(Sort.Order.asc("rentAmount"), Sort.Order.desc("publishedAt"));
            case RENT_AMOUNT_DESC -> Sort.by(Sort.Order.desc("rentAmount"), Sort.Order.desc("publishedAt"));
            case CREATED_AT_DESC -> Sort.by(Sort.Order.desc("createdAt"));
        };
    }
}
