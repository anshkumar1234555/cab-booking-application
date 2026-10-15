package com.cabbooking.repository;

import com.cabbooking.entity.Rating;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RatingRepository extends JpaRepository<Rating, Long> {

    boolean existsByRide_Id(Long rideId);

    List<Rating> findByDriver_IdOrderByCreatedAtDesc(Long driverId);
}