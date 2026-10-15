package com.cabbooking.service;

import com.cabbooking.dto.RideRequest;
import com.cabbooking.dto.RideResponse;
import com.cabbooking.entity.Ride;
import com.cabbooking.entity.User;
import com.cabbooking.repository.RideRepository;
import com.cabbooking.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RideService {

    private final RideRepository rideRepository;
    private final UserRepository userRepository;
    private final FareService fareService;

    public RideService(
            RideRepository rideRepository,
            UserRepository userRepository,
            FareService fareService) {
        this.rideRepository = rideRepository;
        this.userRepository = userRepository;
        this.fareService = fareService;
    }

    @Transactional
    public RideResponse createRide(RideRequest request, String authenticatedEmail) {

        User rider = userRepository
                .findByEmail(authenticatedEmail.toLowerCase().trim())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (request.getDistanceKm() == null) {
            throw new IllegalArgumentException("Distance is required");
        }

        Ride ride = new Ride();
        ride.setRider(rider);
        ride.setPickupLocation(request.getPickupLocation().trim());
        ride.setDropoffLocation(request.getDropoffLocation().trim());
        ride.setFareAmount(fareService.calculateFare(request.getDistanceKm()));
        ride.setStatus(Ride.RideStatus.REQUESTED);

        Ride savedRide = rideRepository.save(ride);

        return new RideResponse(
                savedRide.getId(),
                savedRide.getRider().getFullName(),
                savedRide.getPickupLocation(),
                savedRide.getDropoffLocation(),
                savedRide.getStatus().name(),
                savedRide.getCreatedAt(),
                savedRide.getFareAmount()
        );
    }

    @Transactional(readOnly = true)
    public List<RideResponse> getMyRides(String email) {

        List<Ride> rides = rideRepository
                .findByRider_EmailOrderByCreatedAtDesc(email.toLowerCase().trim());

        return rides.stream()
                .map(ride -> new RideResponse(
                        ride.getId(),
                        ride.getRider().getFullName(),
                        ride.getPickupLocation(),
                        ride.getDropoffLocation(),
                        ride.getStatus().name(),
                        ride.getCreatedAt(),
                        ride.getFareAmount()
                ))
                .toList();
    }
}