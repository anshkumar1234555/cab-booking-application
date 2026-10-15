
package com.cabbooking.controller;

import com.cabbooking.dto.DriverRegistrationRequest;
import com.cabbooking.entity.Driver;
import com.cabbooking.service.DriverService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/drivers")
public class DriverController {

    private final DriverService driverService;

    public DriverController(DriverService driverService) {
        this.driverService = driverService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerDriver(
            @Valid @RequestBody DriverRegistrationRequest request,
            Authentication authentication) {

        try {
            Driver driver = driverService.registerDriver(
                    request, authentication.getName());

            return ResponseEntity.status(HttpStatus.CREATED).body(
                    Map.of(
                            "message", "Driver registered successfully",
                            "driverId", driver.getId(),
                            "licenseNumber", driver.getLicenseNumber(),
                            "vehicleNumber", driver.getVehicleNumber(),
                            "vehicleModel", driver.getVehicleModel(),
                            "available", driver.isAvailable()
                    )
            );

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", e.getMessage()));
        }
    }
}
