
package com.cabbooking.controller;

import com.cabbooking.entity.Ride;
import com.cabbooking.entity.User;
import com.cabbooking.repository.RideRepository;
import com.cabbooking.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/rides")
public class AdminRideStatsController {

    private final RideRepository rideRepository;
    private final UserRepository userRepository;

    public AdminRideStatsController(
            RideRepository rideRepository,
            UserRepository userRepository) {
        this.rideRepository = rideRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/statistics")
    public ResponseEntity<?> getRideStatistics(
            Authentication authentication) {

        User admin = userRepository.findByEmail(authentication.getName())
                .orElse(null);

        if (admin == null || admin.getRole() != User.Role.ADMIN) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Admin access only"));
        }

        Map<String, Long> statistics = new LinkedHashMap<>();

        for (Ride.RideStatus status : Ride.RideStatus.values()) {
            long count = rideRepository
                    .findByStatusOrderByCreatedAtDesc(status)
                    .size();

            statistics.put(status.name(), count);
        }

        statistics.put("TOTAL", rideRepository.count());

        return ResponseEntity.ok(statistics);
    }
}
