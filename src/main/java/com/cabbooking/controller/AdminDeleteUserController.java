
package com.cabbooking.controller;

import com.cabbooking.entity.User;
import com.cabbooking.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/users")
public class AdminDeleteUserController {

    private final UserRepository userRepository;

    public AdminDeleteUserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<?> deleteUser(
            @PathVariable Long userId,
            Authentication authentication) {

        User admin = userRepository.findByEmail(authentication.getName())
                .orElse(null);

        if (admin == null || admin.getRole() != User.Role.ADMIN) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Admin access only"));
        }

        if (admin.getId().equals(userId)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "You cannot delete your own admin account"));
        }

        User user = userRepository.findById(userId).orElse(null);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "User not found"));
        }

        if (user.getRole() == User.Role.ADMIN) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Admin accounts cannot be deleted through this endpoint"));
        }

        userRepository.delete(user);

        return ResponseEntity.ok(Map.of(
                "message", "User deleted successfully",
                "userId", userId
        ));
    }
}
