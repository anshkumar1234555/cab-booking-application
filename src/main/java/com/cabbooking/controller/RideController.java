
package com.cabbooking.controller;

import com.cabbooking.dto.RideRequest;
import com.cabbooking.dto.RideResponse;
import com.cabbooking.service.RideService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rides")
public class RideController {

    private final RideService rideService;

    public RideController(RideService rideService) {
        this.rideService = rideService;
    }

    @PostMapping
    public ResponseEntity<RideResponse> createRide(
            @Valid @RequestBody RideRequest request,
            Authentication authentication) {

        RideResponse response = rideService.createRide(
                request, authentication.getName());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/my")
    public ResponseEntity<List<RideResponse>> getMyRides(
            Authentication authentication) {

        return ResponseEntity.ok(
                rideService.getMyRides(authentication.getName()));
    }

    @GetMapping("/history")
    public ResponseEntity<?> getRideHistory(Authentication authentication) {
        String email = authentication.getName();

        return ResponseEntity.ok(
                rideService.getMyRides(email)
        );
    }

}
