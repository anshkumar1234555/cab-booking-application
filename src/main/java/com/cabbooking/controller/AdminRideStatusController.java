
package com.cabbooking.controller;

import com.cabbooking.entity.Ride;
import com.cabbooking.entity.User;
import com.cabbooking.repository.RideRepository;
import com.cabbooking.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/rides")
public class AdminRideStatusController {

    private final RideRepository rideRepository;
    private final UserRepository userRepository;

    public AdminRideStatusController(
            RideRepository rideRepository,
            UserRepository userRepository) {
        this.rideRepository = rideRepository;
        this.userRepository = userRepository;
    }

    @PutMapping("/{rideId}/cancel")
    public ResponseEntity<?> cancelRide(
            @PathVariable Long rideId,
            Authentication authentication) {

        User admin = userRepository.findByEmail(authentication.getName())
                .orElse(null);

        if (admin == null || admin.getRole() != User.Role.ADMIN) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Admin access only"));
        }

        Ride ride = rideRepository.findById(rideId).orElse(null);

        if (ride == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Ride not found"));
        }

        if (ride.getStatus() == Ride.RideStatus.COMPLETED
                || ride.getStatus() == Ride.RideStatus.CANCELLED) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message",
                            "Completed or cancelled rides cannot be cancelled"));
        }

        ride.setStatus(Ride.RideStatus.CANCELLED);

        if (ride.getDriver() != null) {
            ride.getDriver().setAvailable(true);
        }

        rideRepository.save(ride);

        return ResponseEntity.ok(Map.of(
                "message", "Ride cancelled by admin",
                "rideId", ride.getId(),
                "status", ride.getStatus().name()
        ));
    }
}
