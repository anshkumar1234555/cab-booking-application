package com.cabbooking.controller;

import com.cabbooking.entity.Driver;
import com.cabbooking.entity.Ride;
import com.cabbooking.repository.DriverRepository;
import com.cabbooking.repository.RideRepository;
import com.cabbooking.service.DriverRideSecurityService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/driver/rides")
public class DriverRideStatusController {

    private final DriverRideSecurityService rideSecurityService;
    private final RideRepository rideRepository;
    private final DriverRepository driverRepository;

    public DriverRideStatusController(
            DriverRideSecurityService rideSecurityService,
            RideRepository rideRepository,
            DriverRepository driverRepository) {
        this.rideSecurityService = rideSecurityService;
        this.rideRepository = rideRepository;
        this.driverRepository = driverRepository;
    }

    @PutMapping("/{rideId}/start")
    public ResponseEntity<?> startRide(
            @PathVariable Long rideId,
            Authentication authentication) {

        Driver driver = getDriver(authentication.getName());

        Ride ride = rideSecurityService.getAssignedRide(rideId, authentication.getName());

        if (ride.getStatus() != Ride.RideStatus.ACCEPTED
                && ride.getStatus() != Ride.RideStatus.DRIVER_ARRIVING) {
            return ResponseEntity.badRequest().body(Map.of(
                    "message", "Only accepted rides can be started",
                    "status", ride.getStatus().name()
            ));
        }

        ride.setStatus(Ride.RideStatus.IN_PROGRESS);
        rideRepository.save(ride);

        return ResponseEntity.ok(Map.of(
                "message", "Ride started successfully",
                "rideId", ride.getId(),
                "status", ride.getStatus().name()
        ));
    }

    @PutMapping("/{rideId}/complete")
    public ResponseEntity<?> completeRide(
            @PathVariable Long rideId,
            Authentication authentication) {

        Driver driver = getDriver(authentication.getName());

        Ride ride = rideSecurityService.getAssignedRide(rideId, authentication.getName());

        if (ride.getStatus() != Ride.RideStatus.IN_PROGRESS) {
            return ResponseEntity.badRequest().body(Map.of(
                    "message", "Only rides in progress can be completed",
                    "status", ride.getStatus().name()
            ));
        }

        ride.setStatus(Ride.RideStatus.COMPLETED);
        rideRepository.save(ride);

        driver.setAvailable(true);
        driverRepository.save(driver);

        return ResponseEntity.ok(Map.of(
                "message", "Ride completed successfully",
                "rideId", ride.getId(),
                "status", ride.getStatus().name(),
                "fareAmount", ride.getFareAmount()
        ));
    }

    private Driver getDriver(String email) {
        Driver driver = driverRepository
                .findByUser_Email(email)
                .orElseThrow(() -> new IllegalArgumentException("Driver profile not found"));

        if (!driver.getUser().getRole().name().equals("DRIVER")) {
            throw new AccessDeniedException("Only drivers can perform this action");
        }

        return driver;
    }
}