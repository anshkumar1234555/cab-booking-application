package com.cabbooking.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record RideResponse(
        Long id,
        String riderName,
        String pickupLocation,
        String dropoffLocation,
        String status,
        LocalDateTime createdAt,
        BigDecimal fareAmount
) {
}