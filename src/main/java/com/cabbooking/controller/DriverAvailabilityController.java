package com.cabbooking.controller;

import com.cabbooking.entity.Driver;
import com.cabbooking.repository.DriverRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/driver/availability")
public class DriverAvailabilityController {

    private final DriverRepository driverRepository;

    public DriverAvailabilityController(DriverRepository driverRepository) {
        this.driverRepository = driverRepository;
    }

    @PutMapping
    public ResponseEntity<?> updateAvailability(
            @RequestParam boolean available,
            Authentication authentication) {

        Driver driver = driverRepository
                .findByUser_Email(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("Driver profile not found"));

        if (!"DRIVER".equals(driver.getUser().getRole().name())) {
            throw new AccessDeniedException("Only drivers can update availability");
        }

        driver.setAvailable(available);
        Driver savedDriver = driverRepository.save(driver);

        return ResponseEntity.ok(Map.of(
                "message", "Driver availability updated successfully",
                "driverId", savedDriver.getId(),
                "available", savedDriver.isAvailable()
        ));
    }
}