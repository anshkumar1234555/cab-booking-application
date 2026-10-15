package com.cabbooking.controller;

import com.cabbooking.entity.Driver;
import com.cabbooking.entity.Ride;
import com.cabbooking.repository.DriverRepository;
import com.cabbooking.repository.RideRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/driver/earnings")
public class DriverEarningsController {

    private final DriverRepository driverRepository;
    private final RideRepository rideRepository;

    public DriverEarningsController(
            DriverRepository driverRepository,
            RideRepository rideRepository) {
        this.driverRepository = driverRepository;
        this.rideRepository = rideRepository;
    }

    @GetMapping
    public ResponseEntity<?> getDriverEarnings(Authentication authentication) {

        Driver driver = driverRepository
                .findByUser_Email(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("Driver profile not found"));

        if (!"DRIVER".equals(driver.getUser().getRole().name())) {
            throw new AccessDeniedException("Only drivers can view driver earnings");
        }

        List<Ride> rides = rideRepository
                .findByDriver_User_EmailOrderByCreatedAtDesc(authentication.getName());

        List<Ride> completedRides = rides.stream()
                .filter(ride -> ride.getStatus() == Ride.RideStatus.COMPLETED)
                .toList();

        BigDecimal totalEarnings = completedRides.stream()
                .map(Ride::getFareAmount)
                .filter(fare -> fare != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("driverId", driver.getId());
        response.put("driverName", driver.getUser().getFullName());
        response.put("completedRides", completedRides.size());
        response.put("totalEarnings", totalEarnings);

        return ResponseEntity.ok(response);
    }
}