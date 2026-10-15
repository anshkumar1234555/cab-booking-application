
package com.cabbooking.controller;

import com.cabbooking.entity.Ride;
import com.cabbooking.repository.RideRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/rides")
public class RideCancellationController {

    private final RideRepository rideRepository;

    public RideCancellationController(RideRepository rideRepository) {
        this.rideRepository = rideRepository;
    }

    @PutMapping("/{rideId}/cancel")
    @Transactional
    public ResponseEntity<?> cancelRide(
            @PathVariable Long rideId,
            Authentication authentication) {

        String email = authentication.getName();

        Ride ride = rideRepository
                .findByIdAndRider_Email(rideId, email)
                .orElse(null);

        if (ride == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Ride not found for this rider"));
        }

        if (ride.getStatus() != Ride.RideStatus.REQUESTED) {
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "message",
                            "Only rides with REQUESTED status can be cancelled"
                    ));
        }

        ride.setStatus(Ride.RideStatus.CANCELLED);
        rideRepository.save(ride);

        return ResponseEntity.ok(Map.of(
                "message", "Ride cancelled successfully",
                "rideId", ride.getId(),
                "status", ride.getStatus().name()
        ));
    }
}
