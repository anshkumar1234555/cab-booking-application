package com.cabbooking.controller;

import com.cabbooking.dto.RatingRequest;
import com.cabbooking.entity.Driver;
import com.cabbooking.entity.Rating;
import com.cabbooking.entity.Ride;
import com.cabbooking.repository.DriverRepository;
import com.cabbooking.repository.RatingRepository;
import com.cabbooking.repository.RideRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ratings")
public class RatingController {

    private final RatingRepository ratingRepository;
    private final RideRepository rideRepository;
    private final DriverRepository driverRepository;

    public RatingController(
            RatingRepository ratingRepository,
            RideRepository rideRepository,
            DriverRepository driverRepository) {
        this.ratingRepository = ratingRepository;
        this.rideRepository = rideRepository;
        this.driverRepository = driverRepository;
    }

    @PostMapping("/rides/{rideId}")
    public ResponseEntity<?> rateRide(
            @PathVariable Long rideId,
            @Valid @RequestBody RatingRequest request,
            Authentication authentication) {

        Ride ride = rideRepository.findById(rideId).orElse(null);

        if (ride == null) {
            return ResponseEntity.notFound().build();
        }

        if (!ride.getRider().getEmail()
                .equalsIgnoreCase(authentication.getName())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "This ride does not belong to you"));
        }

        if (!"COMPLETED".equals(ride.getStatus().name())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Only completed rides can be rated"));
        }

        if (ride.getDriver() == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "This ride has no assigned driver"));
        }

        if (ratingRepository.existsByRide_Id(rideId)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", "This ride has already been rated"));
        }

        Rating rating = new Rating();
        rating.setRide(ride);
        rating.setRider(ride.getRider());
        rating.setDriver(ride.getDriver());
        rating.setScore(request.getScore());
        rating.setComment(request.getComment());

        Rating saved = ratingRepository.save(rating);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("message", "Rating submitted successfully");
        response.put("ratingId", saved.getId());
        response.put("rideId", rideId);
        response.put("driverId", ride.getDriver().getId());
        response.put("score", saved.getScore());
        response.put("comment", saved.getComment());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/drivers/{driverId}")
    public ResponseEntity<?> getDriverRatings(
            @PathVariable Long driverId) {

        Driver driver = driverRepository.findById(driverId).orElse(null);

        if (driver == null) {
            return ResponseEntity.notFound().build();
        }

        List<Rating> ratings =
                ratingRepository.findByDriver_IdOrderByCreatedAtDesc(driverId);

        double average = ratings.stream()
                .mapToInt(Rating::getScore)
                .average()
                .orElse(0.0);

        List<Map<String, Object>> items = ratings.stream().map(rating -> {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("ratingId", rating.getId());
            item.put("rideId", rating.getRide().getId());
            item.put("score", rating.getScore());
            item.put("comment", rating.getComment());
            item.put("createdAt", rating.getCreatedAt());
            return item;
        }).toList();

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("driverId", driverId);
        response.put("driverName", driver.getUser().getFullName());
        response.put("totalRatings", ratings.size());
        response.put("averageRating",
                Math.round(average * 100.0) / 100.0);
        response.put("ratings", items);

        return ResponseEntity.ok(response);
    }
}