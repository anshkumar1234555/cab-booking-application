
package com.cabbooking.controller;

import com.cabbooking.entity.Driver;
import com.cabbooking.entity.Ride;
import com.cabbooking.entity.User;
import com.cabbooking.repository.DriverRepository;
import com.cabbooking.repository.RideRepository;
import com.cabbooking.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/driver/rides")
public class DriverRideHistoryController {

    private final RideRepository rideRepository;
    private final DriverRepository driverRepository;
    private final UserRepository userRepository;

    public DriverRideHistoryController(
            RideRepository rideRepository,
            DriverRepository driverRepository,
            UserRepository userRepository) {
        this.rideRepository = rideRepository;
        this.driverRepository = driverRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/history")
    public ResponseEntity<?> getDriverRideHistory(
            Authentication authentication) {

        String email = authentication.getName();

        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null || user.getRole() != User.Role.DRIVER) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Only drivers can view ride history"));
        }

        Driver driver = driverRepository.findByUser_Email(email).orElse(null);

        if (driver == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Driver profile not found"));
        }

        return ResponseEntity.ok(
                rideRepository
                        .findByDriver_User_EmailOrderByCreatedAtDesc(email)
                        .stream()
                        .map(ride -> Map.of(
                                "rideId", ride.getId(),
                                "riderName", ride.getRider().getFullName(),
                                "pickupLocation", ride.getPickupLocation(),
                                "dropoffLocation", ride.getDropoffLocation(),
                                "status", ride.getStatus().name(),
                                "createdAt", ride.getCreatedAt().toString()
                        ))
                        .toList()
        );
    }
}
