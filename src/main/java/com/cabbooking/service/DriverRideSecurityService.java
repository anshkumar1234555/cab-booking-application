package com.cabbooking.service;

import com.cabbooking.entity.Driver;
import com.cabbooking.entity.Ride;
import com.cabbooking.repository.DriverRepository;
import com.cabbooking.repository.RideRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service
public class DriverRideSecurityService {

    private final DriverRepository driverRepository;
    private final RideRepository rideRepository;

    public DriverRideSecurityService(
            DriverRepository driverRepository,
            RideRepository rideRepository) {
        this.driverRepository = driverRepository;
        this.rideRepository = rideRepository;
    }

    public Ride getAssignedRide(Long rideId, String email) {

        Driver driver = driverRepository
                .findByUser_Email(email)
                .orElseThrow(() -> new IllegalArgumentException("Driver profile not found"));

        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new IllegalArgumentException("Ride not found"));

        if (ride.getDriver() == null || !ride.getDriver().getId().equals(driver.getId())) {
            throw new AccessDeniedException("This ride is not assigned to you");
        }

        return ride;
    }
}