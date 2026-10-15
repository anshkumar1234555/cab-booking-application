
package com.cabbooking.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/fares")
public class FareController {

    @GetMapping("/estimate")
    public ResponseEntity<?> estimateFare(
            @RequestParam double distanceKm) {

        if (!Double.isFinite(distanceKm)
                || distanceKm <= 0
                || distanceKm > 1000) {
            return ResponseEntity.badRequest().body(
                    Map.of("message",
                            "Distance must be greater than 0 and at most 1000 km")
            );
        }

        double baseFare = 50.0;
        double perKmRate = 12.0;
        double estimatedFare = baseFare + (distanceKm * perKmRate);

        return ResponseEntity.ok(Map.of(
                "distanceKm", distanceKm,
                "baseFare", baseFare,
                "perKmRate", perKmRate,
                "estimatedFare", Math.round(estimatedFare * 100.0) / 100.0,
                "currency", "INR"
        ));
    }
}
