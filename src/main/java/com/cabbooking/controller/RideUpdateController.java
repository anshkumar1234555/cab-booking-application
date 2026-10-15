package com.cabbooking.controller;

import com.cabbooking.entity.Ride;
import com.cabbooking.repository.RideRepository;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

@RestController
@RequestMapping("/api/rides")
public class RideUpdateController {

    private final RideRepository rideRepository;

    public RideUpdateController(RideRepository rideRepository) {
        this.rideRepository = rideRepository;
    }

    @GetMapping(
            value = "/{rideId}/stream",
            produces = MediaType.TEXT_EVENT_STREAM_VALUE
    )
    public SseEmitter streamRideUpdates(
            @PathVariable Long rideId,
            Authentication authentication) {

        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Ride not found"));

        String email = authentication.getName();

        boolean isRider = ride.getRider().getEmail()
                .equalsIgnoreCase(email);

        boolean isDriver = ride.getDriver() != null
                && ride.getDriver().getUser().getEmail()
                .equalsIgnoreCase(email);

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(authority ->
                        authority.getAuthority().equals("ROLE_ADMIN"));

        if (!isRider && !isDriver && !isAdmin) {
            throw new AccessDeniedException(
                    "You cannot view updates for this ride");
        }

        SseEmitter emitter = new SseEmitter(0L);

        ScheduledExecutorService scheduler =
                Executors.newSingleThreadScheduledExecutor();

        AtomicReference<String> lastStatus = new AtomicReference<>("");

        Runnable sendUpdate = () -> {
            try {
                Ride currentRide = rideRepository.findById(rideId)
                        .orElseThrow(() ->
                                new IllegalArgumentException("Ride not found"));

                String status = currentRide.getStatus().name();

                // Send an event when the ride status changes.
                if (!status.equals(lastStatus.get())) {
                    Map<String, Object> data = new LinkedHashMap<>();
                    data.put("rideId", currentRide.getId());
                    data.put("status", status);
                    data.put("pickupLocation",
                            currentRide.getPickupLocation());
                    data.put("dropoffLocation",
                            currentRide.getDropoffLocation());
                    data.put("fareAmount",
                            currentRide.getFareAmount());

                    emitter.send(
                            SseEmitter.event()
                                    .name("ride-update")
                                    .data(data)
                    );

                    lastStatus.set(status);
                }

                if (status.equals("COMPLETED")
                        || status.equals("CANCELLED")) {
                    emitter.complete();
                    scheduler.shutdown();
                }

            } catch (IOException | RuntimeException exception) {
                emitter.completeWithError(exception);
                scheduler.shutdown();
            }
        };

        emitter.onCompletion(scheduler::shutdownNow);
        emitter.onTimeout(scheduler::shutdownNow);
        emitter.onError(error -> scheduler.shutdownNow());

        scheduler.scheduleAtFixedRate(
                sendUpdate, 0, 2, TimeUnit.SECONDS);

        return emitter;
    }
}