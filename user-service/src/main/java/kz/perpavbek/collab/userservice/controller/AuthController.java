package kz.perpavbek.collab.userservice.controller;

import jakarta.validation.Valid;
import kz.perpavbek.collab.userservice.dto.request.LoginRequest;
import kz.perpavbek.collab.userservice.dto.request.RegisterRequest;
import kz.perpavbek.collab.userservice.dto.response.AuthResponse;
import kz.perpavbek.collab.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = userService.register(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = userService.login(request);
        return ResponseEntity.ok(response);
    }
}
