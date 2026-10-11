
package com.flixo.backend;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final FlixoUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(
            FlixoUserRepository userRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody Map<String, String> data) {
        String username = data.get("username");
        String email = data.get("email");
        String password = data.get("password");

        if (username == null || username.isBlank()
                || email == null || email.isBlank()
                || password == null || password.length() < 8) {
            return ResponseEntity.badRequest().body(
                Map.of("message",
                    "Enter your username, email and a password of at least 8 characters.")
            );
        }

        if (userRepository.existsByEmail(email.trim())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(
                Map.of("message", "Email is already registered.")
            );
        }

        FlixoUser user = new FlixoUser(
            username.trim(),
            email.trim().toLowerCase(),
            passwordEncoder.encode(password)
        );

        FlixoUser savedUser = userRepository.save(user);

        return ResponseEntity.status(HttpStatus.CREATED).body(
            Map.of(
                "message", "Account created successfully!",
                "id", savedUser.getId(),
                "username", savedUser.getUsername(),
                "email", savedUser.getEmail()
            )
        );
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> data) {
        String email = data.get("email");
        String password = data.get("password");

        if (email == null || password == null) {
            return ResponseEntity.badRequest().body(
                Map.of("message", "Email and password are required.")
            );
        }

        return userRepository.findByEmail(email.trim().toLowerCase())
            .filter(user -> passwordEncoder.matches(
                password, user.getPassword()))
            .<ResponseEntity<?>>map(user ->
                ResponseEntity.ok(Map.of(
                    "message", "Login successful!",
                    "id", user.getId(),
                    "username", user.getUsername(),
                    "email", user.getEmail()
                ))
            )
            .orElseGet(() ->
                ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    Map.of("message", "Invalid email or password.")
                )
            );
    }
}
