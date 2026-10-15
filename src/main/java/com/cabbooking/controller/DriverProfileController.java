package com.cabbooking.controller;

import com.cabbooking.entity.Driver;
import com.cabbooking.repository.DriverRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/driver/profile")
public class DriverProfileController {

    private final DriverRepository driverRepository;

    public DriverProfileController(DriverRepository driverRepository) {
        this.driverRepository = driverRepository;
    }

    @GetMapping
    public ResponseEntity<?> getDriverProfile(Authentication authentication) {

        Driver driver = driverRepository
                .findByUser_Email(authentication.getName())
                .orElse(null);

        if (driver == null) {
            throw new IllegalArgumentException("Driver profile not found for this account");
        }

        if (!driver.getUser().getRole().name().equals("DRIVER")) {
            throw new AccessDeniedException("Only drivers can access this profile");
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("driverId", driver.getId());
        response.put("fullName", driver.getUser().getFullName());
        response.put("email", driver.getUser().getEmail());
        response.put("licenseNumber", driver.getLicenseNumber());
        response.put("vehicleNumber", driver.getVehicleNumber());
        response.put("vehicleModel", driver.getVehicleModel());
        response.put("available", driver.isAvailable());

        return ResponseEntity.ok(response);
    }
}