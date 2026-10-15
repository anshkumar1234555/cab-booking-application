
package com.cabbooking.repository;

import com.cabbooking.entity.Ride;
import com.cabbooking.entity.Ride.RideStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RideRepository extends JpaRepository<Ride, Long> {

    List<Ride> findByRider_EmailOrderByCreatedAtDesc(String email);

    List<Ride> findByStatusOrderByCreatedAtDesc(RideStatus status);
    Optional<Ride> findByIdAndRider_Email(Long id, String email);

    List<Ride> findByDriver_User_EmailOrderByCreatedAtDesc(String email);

    Optional<Ride> findByIdAndStatus(Long id, RideStatus status);
}
