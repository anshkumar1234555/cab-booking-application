package com.cabbooking.controller;

import com.cabbooking.entity.Driver;
import com.cabbooking.entity.Ride;
import com.cabbooking.repository.DriverRepository;
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
@RequestMapping("/api/driver")
public class DriverCurrentRideController {

    private final DriverRepository driverRepository;
    private final RideRepository rideRepository;

    public DriverCurrentRideController(
            DriverRepository driverRepository,
            RideRepository rideRepository) {
        this.driverRepository = driverRepository;
        this.rideRepository = rideRepository;
    }

    @GetMapping("/current-ride")
    public List<Map<String, Object>> getCurrentRide(Authentication authentication) {

        String email = authentication.getName();

        Driver driver = driverRepository.findByUser_Email(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Driver profile not found"));

        boolean isDriver = authentication.getAuthorities()
                .stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_DRIVER"));

        if (!isDriver) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Driver access required");
        }

        return rideRepository
                .findByDriver_User_EmailOrderByCreatedAtDesc(email)
                .stream()
                .filter(ride ->
                        ride.getStatus() == Ride.RideStatus.ACCEPTED
                                || ride.getStatus() == Ride.RideStatus.DRIVER_ARRIVING
                                || ride.getStatus() == Ride.RideStatus.IN_PROGRESS)
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