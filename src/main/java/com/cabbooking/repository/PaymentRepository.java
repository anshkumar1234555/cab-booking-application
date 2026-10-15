package com.cabbooking.repository;

import com.cabbooking.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByRide_Id(Long rideId);

    List<Payment> findByRider_EmailOrderByCreatedAtDesc(String email);
}