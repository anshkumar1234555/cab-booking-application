package com.cabbooking.controller;

import com.cabbooking.entity.Payment;
import com.cabbooking.entity.Ride;
import com.cabbooking.repository.PaymentRepository;
import com.cabbooking.repository.RideRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentRepository paymentRepository;
    private final RideRepository rideRepository;

    public PaymentController(
            PaymentRepository paymentRepository,
            RideRepository rideRepository) {
        this.paymentRepository = paymentRepository;
        this.rideRepository = rideRepository;
    }

    @PostMapping("/rides/{rideId}")
    public ResponseEntity<?> payForRide(
            @PathVariable Long rideId,
            @RequestBody Map<String, String> request,
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
                    .body(Map.of("message", "Only completed rides can be paid"));
        }

        if (paymentRepository.findByRide_Id(rideId).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", "Payment already exists for this ride"));
        }

        String methodText = request.get("method");

        if (methodText == null || methodText.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Payment method is required: CASH, CARD or UPI"));
        }

        Payment.PaymentMethod method;

        try {
            method = Payment.PaymentMethod.valueOf(
                    methodText.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Method must be CASH, CARD or UPI"));
        }

        Payment payment = new Payment();
        payment.setRide(ride);
        payment.setRider(ride.getRider());
        payment.setAmount(ride.getFareAmount());
        payment.setMethod(method);

        // Demo only: no real money is transferred.
        payment.setStatus(Payment.PaymentStatus.PAID);
        payment.setTransactionReference("SIM-" + UUID.randomUUID());

        Payment saved = paymentRepository.save(payment);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("message", "Simulated payment successful");
        response.put("paymentId", saved.getId());
        response.put("rideId", rideId);
        response.put("amount", saved.getAmount());
        response.put("method", saved.getMethod().name());
        response.put("status", saved.getStatus().name());
        response.put("transactionReference", saved.getTransactionReference());
        response.put("createdAt", saved.getCreatedAt());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/my")
    public ResponseEntity<?> getMyPayments(Authentication authentication) {

        List<Payment> payments =
                paymentRepository.findByRider_EmailOrderByCreatedAtDesc(
                        authentication.getName());

        List<Map<String, Object>> items = payments.stream().map(payment -> {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("paymentId", payment.getId());
            item.put("rideId", payment.getRide().getId());
            item.put("amount", payment.getAmount());
            item.put("method", payment.getMethod().name());
            item.put("status", payment.getStatus().name());
            item.put("transactionReference",
                    payment.getTransactionReference());
            item.put("createdAt", payment.getCreatedAt());
            return item;
        }).toList();

        return ResponseEntity.ok(items);
    }
}