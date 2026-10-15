package com.cabbooking.controller;

import com.cabbooking.entity.Ride;
import com.cabbooking.repository.RideRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/rides")
public class AdminRideDetailsController {

    private final RideRepository rideRepository;

    public AdminRideDetailsController(RideRepository rideRepository) {
        this.rideRepository = rideRepository;
    }

    @GetMapping("/{rideId}")
    public ResponseEntity<?> getRideDetails(
            @PathVariable Long rideId,
            Authentication authentication) {

        if (authentication == null || authentication.getAuthorities().stream()
                .noneMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"))) {
            throw new AccessDeniedException("Only administrators can view ride details");
        }

        Ride ride = rideRepository.findById(rideId).orElse(null);

        if (ride == null) {
            return ResponseEntity.notFound().build();
        }

        Map<String, Object> response = new LinkedHashMap<>();

        response.put("rideId", ride.getId());
        response.put("riderId", ride.getRider().getId());
        response.put("riderName", ride.getRider().getFullName());
        response.put("riderEmail", ride.getRider().getEmail());

        response.put("driverId", ride.getDriver() != null
                ? ride.getDriver().getId()
                : null);

        response.put("driverName", ride.getDriver() != null
                ? ride.getDriver().getUser().getFullName()
                : null);

        response.put("pickupLocation", ride.getPickupLocation());
        response.put("dropoffLocation", ride.getDropoffLocation());
        response.put("fareAmount", ride.getFareAmount());
        response.put("status", ride.getStatus().name());
        response.put("createdAt", ride.getCreatedAt());

        return ResponseEntity.ok(response);
    }
}