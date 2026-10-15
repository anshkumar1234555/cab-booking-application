
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
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminDashboardController {

    private final UserRepository userRepository;
    private final DriverRepository driverRepository;
    private final RideRepository rideRepository;

    public AdminDashboardController(
            UserRepository userRepository,
            DriverRepository driverRepository,
            RideRepository rideRepository) {
        this.userRepository = userRepository;
        this.driverRepository = driverRepository;
        this.rideRepository = rideRepository;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboard(Authentication authentication) {

        User user = userRepository.findByEmail(authentication.getName())
                .orElse(null);

        if (user == null || user.getRole() != User.Role.ADMIN) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Admin access only"));
        }

        long totalUsers = userRepository.count();
        long totalDrivers = driverRepository.count();
        long totalRides = rideRepository.count();

        long requestedRides = rideRepository
                .findByStatusOrderByCreatedAtDesc(Ride.RideStatus.REQUESTED)
                .size();

        long completedRides = rideRepository
                .findByStatusOrderByCreatedAtDesc(Ride.RideStatus.COMPLETED)
                .size();

        long cancelledRides = rideRepository
                .findByStatusOrderByCreatedAtDesc(Ride.RideStatus.CANCELLED)
                .size();

        long availableDrivers = driverRepository
                .findAll()
                .stream()
                .filter(Driver::isAvailable)
                .count();

        return ResponseEntity.ok(Map.of(
                "totalUsers", totalUsers,
                "totalDrivers", totalDrivers,
                "totalRides", totalRides,
                "requestedRides", requestedRides,
                "completedRides", completedRides,
                "cancelledRides", cancelledRides,
                "availableDrivers", availableDrivers
        ));
    }
}
