
package com.cabbooking.controller;

import com.cabbooking.entity.Driver;
import com.cabbooking.entity.User;
import com.cabbooking.repository.DriverRepository;
import com.cabbooking.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/drivers")
public class AdminDriverController {

    private final DriverRepository driverRepository;
    private final UserRepository userRepository;

    public AdminDriverController(
            DriverRepository driverRepository,
            UserRepository userRepository) {
        this.driverRepository = driverRepository;
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<?> getAllDrivers(Authentication authentication) {

        User admin = userRepository.findByEmail(authentication.getName())
                .orElse(null);

        if (admin == null || admin.getRole() != User.Role.ADMIN) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Admin access only"));
        }

        List<Map<String, Object>> drivers = driverRepository.findAll()
                .stream()
                .map(driver -> Map.<String, Object>of(
                        "driverId", driver.getId(),
                        "fullName", driver.getUser().getFullName(),
                        "email", driver.getUser().getEmail(),
                        "licenseNumber", driver.getLicenseNumber(),
                        "vehicleNumber", driver.getVehicleNumber(),
                        "vehicleModel", driver.getVehicleModel(),
                        "available", driver.isAvailable(),
                        "createdAt", driver.getCreatedAt() == null
                                ? ""
                                : driver.getCreatedAt().toString()
                ))
                .toList();

        return ResponseEntity.ok(drivers);
    }
}
