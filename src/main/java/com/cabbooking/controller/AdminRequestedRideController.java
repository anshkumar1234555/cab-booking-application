package com.cabbooking.controller;

import com.cabbooking.entity.Ride;
import com.cabbooking.repository.RideRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/rides")
public class AdminRequestedRideController {

    private final RideRepository rideRepository;

    public AdminRequestedRideController(RideRepository rideRepository) {
        this.rideRepository = rideRepository;
    }

    @GetMapping("/requested")
    public List<Map<String, Object>> getRequestedRides(Authentication authentication) {

        boolean isAdmin = authentication.getAuthorities()
                .stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin access required");
        }

        return rideRepository
                .findByStatusOrderByCreatedAtDesc(Ride.RideStatus.REQUESTED)
                .stream()
                .map(ride -> {
                    Map<String, Object> result = new LinkedHashMap<>();

                    result.put("rideId", ride.getId());
                    result.put("riderName", ride.getRider().getFullName());
                    result.put("pickupLocation", ride.getPickupLocation());
                    result.put("dropoffLocation", ride.getDropoffLocation());
                    result.put("status", ride.getStatus().name());
                    result.put("createdAt", ride.getCreatedAt());

                    return result;
                })
                .toList();
    }
}