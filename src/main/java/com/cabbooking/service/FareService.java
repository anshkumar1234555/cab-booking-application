package com.cabbooking.service;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class FareService {

    private static final BigDecimal BASE_FARE = new BigDecimal("50.00");
    private static final BigDecimal PER_KILOMETER = new BigDecimal("12.00");

    public BigDecimal calculateFare(double distanceKm) {

        if (!Double.isFinite(distanceKm) || distanceKm <= 0 || distanceKm > 1000) {
            throw new IllegalArgumentException(
                    "Distance must be greater than 0 and at most 1000 km");
        }

        return BASE_FARE
                .add(PER_KILOMETER.multiply(BigDecimal.valueOf(distanceKm)))
                .setScale(2, RoundingMode.HALF_UP);
    }
}