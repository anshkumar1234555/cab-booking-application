package com.cabbooking.dto;

public record LoginResponse(
        String message,
        Long id,
        String fullName,
        String email,
        String role,
        String token
) {
}