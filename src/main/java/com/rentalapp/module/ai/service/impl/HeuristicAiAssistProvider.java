package com.rentalapp.module.ai.service.impl;

import com.rentalapp.module.ai.dto.EnhanceListingDescriptionRequest;
import com.rentalapp.module.ai.dto.EnhanceListingDescriptionResponse;
import com.rentalapp.module.ai.dto.InterpretListingSearchRequest;
import com.rentalapp.module.ai.dto.InterpretListingSearchResponse;
import com.rentalapp.module.listings.entity.Amenity;
import com.rentalapp.module.listings.entity.HouseType;
import com.rentalapp.module.ai.service.AiAssistProvider;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class HeuristicAiAssistProvider implements AiAssistProvider {
    private static final Map<String, HouseType> HOUSE_TYPE_KEYWORDS = Map.of(
            "apartment", HouseType.APARTMENT,
            "bedsitter", HouseType.BEDSITTER,
            "bed sitter", HouseType.BEDSITTER,
            "studio", HouseType.STUDIO,
            "maisonette", HouseType.MAISONETTE,
            "bungalow", HouseType.BUNGALOW,
            "house", HouseType.HOUSE,
            "townhouse", HouseType.TOWNHOUSE,
            "town house", HouseType.TOWNHOUSE
    );
    private static final List<String> KNOWN_CITIES = List.of("nairobi", "mombasa", "kisumu", "nakuru", "eldoret", "thika", "kiambu");
    private static final Pattern BEDROOM_PATTERN = Pattern.compile("(\\d+)\\s*(bed(room)?|br)");
    private static final Pattern BATHROOM_PATTERN = Pattern.compile("(\\d+)\\s*(bath(room)?|ba)");
    private static final Pattern PRICE_PATTERN = Pattern.compile("(under|max|below|upto|up to)\\s*(kes\\s*)?(\\d+(?:\\.\\d+)?)\\s*(k)?");
    private static final Pattern AREA_PATTERN = Pattern.compile("\\bin\\s+([a-zA-Z][a-zA-Z\\s-]{1,40})");

    @Override
    public EnhanceListingDescriptionResponse enhanceListingDescription(EnhanceListingDescriptionRequest request) {
        List<String> suggestions = new ArrayList<>();
        suggestions.add("Confirm exact viewing availability and move-in timeline.");
        suggestions.add("Highlight nearby landmarks, transit access, or shopping options.");

        if (request.getAmenities() == null || request.getAmenities().isEmpty()) {
            suggestions.add("Add amenities to improve listing credibility and search relevance.");
        }

        if (request.getDescription().trim().length() < 120) {
            suggestions.add("Add more detail about space, light, security, and overall condition.");
        }

        String amenitySummary = summarizeAmenities(request.getAmenities());
        String availabilityPhrase = toAvailabilityPhrase(request.getAvailabilityStatus().name());
        String rentPhrase = formatCurrency(request.getRentAmount());

        String enhancedDescription = String.format(
                "%s %d-bedroom %s in %s, %s, %s at %s. %s %s %s",
                startsWithArticle(request.getHouseType().name()) ? "An" : "A",
                request.getBedrooms(),
                request.getHouseType().name().toLowerCase(Locale.ROOT).replace('_', ' '),
                request.getArea().trim(),
                request.getCity().trim(),
                availabilityPhrase,
                rentPhrase,
                request.getFurnished() ? "This furnished option is positioned for renters who want a faster move-in." : "This unfurnished home gives renters room to make the space their own.",
                amenitySummary,
                normalizeSentence(request.getDescription())
        ).trim();

        return EnhanceListingDescriptionResponse.builder()
                .enhancedDescription(enhancedDescription)
                .suggestions(suggestions.stream().distinct().toList())
                .provider("heuristic-fallback")
                .build();
    }

    @Override
    public InterpretListingSearchResponse interpretListingSearch(InterpretListingSearchRequest request, List<Amenity> amenities) {
        String normalized = request.getQuery().trim().toLowerCase(Locale.ROOT);
        List<String> matchedSignals = new ArrayList<>();
        List<String> notes = new ArrayList<>();
        List<InterpretListingSearchResponse.AmenityMatch> matchedAmenities = new ArrayList<>();

        Integer bedrooms = extractInteger(normalized, BEDROOM_PATTERN);
        if (bedrooms != null) {
            matchedSignals.add("bedrooms");
        }

        Integer bathrooms = extractInteger(normalized, BATHROOM_PATTERN);
        if (bathrooms != null) {
            matchedSignals.add("bathrooms");
        }

        BigDecimal maxPrice = extractPrice(normalized);
        if (maxPrice != null) {
            matchedSignals.add("maxPrice");
        }

        HouseType houseType = extractHouseType(normalized);
        if (houseType != null) {
            matchedSignals.add("houseType");
        }

        Boolean furnished = extractFurnished(normalized);
        if (furnished != null) {
            matchedSignals.add("furnished");
        }

        String city = extractCity(normalized);
        if (city != null) {
            matchedSignals.add("city");
        }

        String area = extractArea(normalized, city);
        if (area != null) {
            matchedSignals.add("area");
        }

        for (Amenity amenity : amenities) {
            String amenityName = amenity.getName().toLowerCase(Locale.ROOT);
            String amenitySlug = amenity.getSlug().toLowerCase(Locale.ROOT);
            if (normalized.contains(amenityName) || normalized.contains(amenitySlug.replace('-', ' ')) || normalized.contains(amenitySlug)) {
                matchedAmenities.add(InterpretListingSearchResponse.AmenityMatch.builder()
                        .id(amenity.getId())
                        .name(amenity.getName())
                        .build());
            }
        }

        if (!matchedAmenities.isEmpty()) {
            matchedSignals.add("amenities");
            if (matchedAmenities.size() > 1) {
                notes.add("Multiple amenities matched. Apply the top match first if your current UI only supports one amenity filter.");
            }
        }

        if (matchedSignals.isEmpty()) {
            notes.add("No structured filters were confidently extracted. Continue with manual filters.");
        }

        return InterpretListingSearchResponse.builder()
                .normalizedQuery(request.getQuery().trim())
                .interpreted(!matchedSignals.isEmpty())
                .provider("heuristic-fallback")
                .matchedSignals(matchedSignals)
                .notes(notes)
                .filters(InterpretListingSearchResponse.Filters.builder()
                        .city(city)
                        .area(area)
                        .minPrice(null)
                        .maxPrice(maxPrice)
                        .bedrooms(bedrooms)
                        .bathrooms(bathrooms)
                        .houseType(houseType)
                        .furnished(furnished)
                        .amenities(matchedAmenities)
                        .build())
                .build();
    }

    private Integer extractInteger(String input, Pattern pattern) {
        Matcher matcher = pattern.matcher(input);
        return matcher.find() ? Integer.parseInt(matcher.group(1)) : null;
    }

    private BigDecimal extractPrice(String input) {
        Matcher matcher = PRICE_PATTERN.matcher(input);
        if (!matcher.find()) {
            return null;
        }

        BigDecimal value = new BigDecimal(matcher.group(3));
        if (matcher.group(4) != null) {
            value = value.multiply(BigDecimal.valueOf(1000));
        }

        return value;
    }

    private HouseType extractHouseType(String input) {
        return HOUSE_TYPE_KEYWORDS.entrySet().stream()
                .filter(entry -> input.contains(entry.getKey()))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(null);
    }

    private Boolean extractFurnished(String input) {
        if (input.contains("unfurnished")) {
            return false;
        }
        if (input.contains("furnished")) {
            return true;
        }
        return null;
    }

    private String extractCity(String input) {
        return KNOWN_CITIES.stream()
                .filter(input::contains)
                .map(this::toTitleCase)
                .findFirst()
                .orElse(null);
    }

    private String extractArea(String input, String city) {
        Matcher matcher = AREA_PATTERN.matcher(input);
        if (!matcher.find()) {
            return null;
        }

        String raw = matcher.group(1).trim();
        if (city != null && raw.equalsIgnoreCase(city)) {
            return null;
        }

        String cleaned = raw
                .replaceAll("\\bunder\\b.*$", "")
                .replaceAll("\\bwith\\b.*$", "")
                .replaceAll("\\bnear\\b.*$", "")
                .replaceAll("\\bfor\\b.*$", "")
                .trim();

        return cleaned.isBlank() ? null : toTitleCase(cleaned);
    }

    private String toTitleCase(String value) {
        return java.util.Arrays.stream(value.trim().split("\\s+"))
                .filter(part -> !part.isBlank())
                .map(part -> part.substring(0, 1).toUpperCase(Locale.ROOT) + part.substring(1).toLowerCase(Locale.ROOT))
                .reduce((left, right) -> left + " " + right)
                .orElse(value);
    }

    private String summarizeAmenities(List<String> amenities) {
        if (amenities == null || amenities.isEmpty()) {
            return "Amenities can be added to strengthen the listing.";
        }

        String joined = amenities.stream()
                .filter(item -> item != null && !item.isBlank())
                .map(String::trim)
                .limit(4)
                .reduce((left, right) -> left + ", " + right)
                .orElse("");

        return joined.isBlank() ? "Amenities can be added to strengthen the listing." : "Highlights include " + joined + ".";
    }

    private String normalizeSentence(String value) {
        String trimmed = value.trim();
        if (trimmed.endsWith(".")) {
            return trimmed;
        }

        return trimmed + ".";
    }

    private String formatCurrency(BigDecimal value) {
        return "KES " + value.stripTrailingZeros().toPlainString();
    }

    private String toAvailabilityPhrase(String availabilityStatus) {
        return switch (availabilityStatus) {
            case "AVAILABLE_NOW" -> "available now";
            case "AVAILABLE_SOON" -> "available soon";
            case "OCCUPIED" -> "currently occupied";
            default -> availabilityStatus.replace('_', ' ').toLowerCase(Locale.ROOT);
        };
    }

    private boolean startsWithArticle(String houseType) {
        return houseType.startsWith("A");
    }
}
