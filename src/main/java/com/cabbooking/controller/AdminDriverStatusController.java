
package com.cabbooking.controller;

import com.cabbooking.entity.Driver;
import com.cabbooking.entity.User;
import com.cabbooking.repository.DriverRepository;
import com.cabbooking.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/drivers")
public class AdminDriverStatusController {

    private final DriverRepository driverRepository;
    private final UserRepository userRepository;

    public AdminDriverStatusController(
            DriverRepository driverRepository,
            UserRepository userRepository) {
        this.driverRepository = driverRepository;
        this.userRepository = userRepository;
    }

    @PutMapping("/{driverId}/availability")
    public ResponseEntity<?> updateAvailability(
            @PathVariable Long driverId,
            @RequestParam boolean available,
            Authentication authentication) {

        User admin = userRepository.findByEmail(authentication.getName())
                .orElse(null);

        if (admin == null || admin.getRole() != User.Role.ADMIN) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Admin access only"));
        }

        Driver driver = driverRepository.findById(driverId).orElse(null);

        if (driver == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Driver not found"));
        }

        if (!available && !driver.isAvailable()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Driver is already unavailable"));
        }

        if (available && driver.isAvailable()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Driver is already available"));
        }

        driver.setAvailable(available);
        driverRepository.save(driver);

        return ResponseEntity.ok(Map.of(
                "message", "Driver availability updated successfully",
                "driverId", driver.getId(),
                "available", driver.isAvailable()
        ));
    }
}
