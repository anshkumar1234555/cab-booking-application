
package com.cabbooking.controller;

import com.cabbooking.entity.Ride;
import com.cabbooking.entity.User;
import com.cabbooking.repository.RideRepository;
import com.cabbooking.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/rides")
public class AdminCancelledRideController {

    private final RideRepository rideRepository;
    private final UserRepository userRepository;

    public AdminCancelledRideController(
            RideRepository rideRepository,
            UserRepository userRepository) {
        this.rideRepository = rideRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/cancelled")
    public ResponseEntity<?> getCancelledRides(
            Authentication authentication) {

        User admin = userRepository.findByEmail(authentication.getName())
                .orElse(null);

        if (admin == null || admin.getRole() != User.Role.ADMIN) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Admin access only"));
        }

        List<Map<String, Object>> rides = rideRepository
                .findByStatusOrderByCreatedAtDesc(Ride.RideStatus.CANCELLED)
                .stream()
                .map(ride -> Map.<String, Object>of(
                        "rideId", ride.getId(),
                        "riderName", ride.getRider().getFullName(),
                        "pickupLocation", ride.getPickupLocation(),
                        "dropoffLocation", ride.getDropoffLocation(),
                        "status", ride.getStatus().name(),
                        "driverName", ride.getDriver() == null
                                ? "Not assigned"
                                : ride.getDriver().getUser().getFullName(),
                        "createdAt", ride.getCreatedAt() == null
                                ? ""
                                : ride.getCreatedAt().toString()
                ))
                .toList();

        return ResponseEntity.ok(rides);
    }
}
