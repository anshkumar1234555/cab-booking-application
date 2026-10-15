
package com.cabbooking.controller;

import com.cabbooking.entity.User;
import com.cabbooking.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserRoleController {

    private final UserRepository userRepository;

    public AdminUserRoleController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/role/{role}")
    public ResponseEntity<?> getUsersByRole(
            @PathVariable String role,
            Authentication authentication) {

        User admin = userRepository.findByEmail(authentication.getName())
                .orElse(null);

        if (admin == null || admin.getRole() != User.Role.ADMIN) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Admin access only"));
        }

        User.Role selectedRole;

        try {
            selectedRole = User.Role.valueOf(role.toUpperCase());
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "message", "Invalid role. Use RIDER, DRIVER, or ADMIN"
                    ));
        }

        List<Map<String, Object>> users = userRepository.findAll()
                .stream()
                .filter(user -> user.getRole() == selectedRole)
                .map(user -> Map.<String, Object>of(
                        "id", user.getId(),
                        "fullName", user.getFullName(),
                        "email", user.getEmail(),
                        "role", user.getRole().name()
                ))
                .toList();

        return ResponseEntity.ok(users);
    }
}
