package kz.perpavbek.collab.userservice.controller;

import kz.perpavbek.collab.userservice.dto.response.PageResponse;
import kz.perpavbek.collab.userservice.dto.response.UserResponse;
import kz.perpavbek.collab.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(@RequestHeader("Authorization") String authHeader) {
        return ResponseEntity.ok(userService.getCurrentUser(authHeader));
    }

    @GetMapping("/id/{id}")
    public ResponseEntity<UserResponse> getUser(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @GetMapping("/search")
    public ResponseEntity<PageResponse<UserResponse>> searchUsers(
            @RequestParam String name,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<UserResponse> result = userService.searchUsers(name, page, size);
        return ResponseEntity.ok(new PageResponse<>(result));
    }
}
