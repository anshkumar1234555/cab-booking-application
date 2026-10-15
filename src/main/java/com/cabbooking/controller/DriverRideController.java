
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/driver/rides")
public class DriverRideController {

    private final RideRepository rideRepository;
    private final DriverRepository driverRepository;
    private final UserRepository userRepository;

    public DriverRideController(
            RideRepository rideRepository,
            DriverRepository driverRepository,
            UserRepository userRepository) {
        this.rideRepository = rideRepository;
        this.driverRepository = driverRepository;
        this.userRepository = userRepository;
    }

    // View all rides that are waiting for a driver
    @GetMapping("/available")
    public ResponseEntity<?> getAvailableRides(
            Authentication authentication) {

        String email = authentication.getName();

        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null || user.getRole() != User.Role.DRIVER) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of(
                            "message",
                            "Only drivers can view available rides"
                    ));
        }

        Driver driver = driverRepository
                .findByUser_Email(email)
                .orElse(null);

        if (driver == null || !driver.isAvailable()) {
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "message",
                            "Register your driver profile and be available"
                    ));
        }

        List<Ride> rides = rideRepository
                .findByStatusOrderByCreatedAtDesc(
                        Ride.RideStatus.REQUESTED
                );

        return ResponseEntity.ok(
                rides.stream()
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

    // Accept a ride and assign it to the authenticated driver
    @PutMapping("/{rideId}/accept")
    @Transactional
    public ResponseEntity<?> acceptRide(
            @PathVariable Long rideId,
            Authentication authentication) {

        String email = authentication.getName();

        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null || user.getRole() != User.Role.DRIVER) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of(
                            "message",
                            "Only drivers can accept rides"
                    ));
        }

        Driver driver = driverRepository
                .findByUser_Email(email)
                .orElse(null);

        if (driver == null || !driver.isAvailable()) {
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "message",
                            "Driver profile not found or driver unavailable"
                    ));
        }

        Ride ride = rideRepository
                .findByIdAndStatus(
                        rideId,
                        Ride.RideStatus.REQUESTED
                )
                .orElse(null);

        if (ride == null) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of(
                            "message",
                            "Ride not found or already accepted"
                    ));
        }

        // Link the ride to this driver
        ride.setDriver(driver);
        ride.setStatus(Ride.RideStatus.ACCEPTED);

        rideRepository.saveAndFlush(ride);

        // Mark the driver as unavailable while handling this ride
        driver.setAvailable(false);
        driverRepository.save(driver);

        return ResponseEntity.ok(
                Map.of(
                        "message", "Ride accepted successfully",
                        "rideId", ride.getId(),
                        "driverId", driver.getId(),
                        "status", ride.getStatus().name()
                )
        );
    }
}
