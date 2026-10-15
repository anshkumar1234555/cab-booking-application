package com.cabbooking.controller;

import com.cabbooking.entity.Driver;
import com.cabbooking.repository.DriverRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/driver/profile")
public class DriverProfileUpdateController {

    private final DriverRepository driverRepository;

    public DriverProfileUpdateController(DriverRepository driverRepository) {
        this.driverRepository = driverRepository;
    }

    @PutMapping
    public ResponseEntity<?> updateDriverProfile(
            @RequestBody Map<String, String> request,
            Authentication authentication) {

        Driver driver = driverRepository
                .findByUser_Email(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("Driver profile not found"));

        if (!driver.getUser().getRole().name().equals("DRIVER")) {
            throw new AccessDeniedException("Only drivers can update this profile");
        }

        String licenseNumber = request.get("licenseNumber");
        String vehicleNumber = request.get("vehicleNumber");
        String vehicleModel = request.get("vehicleModel");

        if (licenseNumber == null || licenseNumber.isBlank()
                || vehicleNumber == null || vehicleNumber.isBlank()
                || vehicleModel == null || vehicleModel.isBlank()) {
            return ResponseEntity.badRequest().body(
                    Map.of("message", "License number, vehicle number and vehicle model are required"));
        }

        driver.setLicenseNumber(licenseNumber.trim());
        driver.setVehicleNumber(vehicleNumber.trim());
        driver.setVehicleModel(vehicleModel.trim());

        Driver savedDriver = driverRepository.save(driver);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("message", "Driver profile updated successfully");
        response.put("driverId", savedDriver.getId());
        response.put("licenseNumber", savedDriver.getLicenseNumber());
        response.put("vehicleNumber", savedDriver.getVehicleNumber());
        response.put("vehicleModel", savedDriver.getVehicleModel());

        return ResponseEntity.ok(response);
    }
}