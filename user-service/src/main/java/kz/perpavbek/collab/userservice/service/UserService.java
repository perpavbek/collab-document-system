package kz.perpavbek.collab.userservice.service;

import kz.perpavbek.collab.userservice.dto.request.LoginRequest;
import kz.perpavbek.collab.userservice.dto.request.RegisterRequest;
import kz.perpavbek.collab.userservice.dto.response.AuthResponse;
import kz.perpavbek.collab.userservice.dto.response.UserResponse;
import kz.perpavbek.collab.userservice.entity.User;
import kz.perpavbek.collab.userservice.enums.Role;
import kz.perpavbek.collab.userservice.exception.NotFoundException;
import kz.perpavbek.collab.userservice.mapper.UserMapper;
import kz.perpavbek.collab.userservice.repository.UserRepository;
import kz.perpavbek.collab.userservice.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final JwtUtils jwtUtils;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already in use");
        }
        User user = userMapper.toEntity(request);
        user.setRole(Role.USER);
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        userRepository.save(user);

        String token = jwtUtils.generateToken(user);

        return new AuthResponse(token);
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        String token = jwtUtils.generateToken(user);

        return new AuthResponse(token);
    }

    public UserResponse getCurrentUser(String authHeader) {
        User user = getUserByToken(jwtUtils.extractToken(authHeader));
        return userMapper.toResponse(user);
    }

    public UserResponse getUserById(UUID id) {
        User user = userRepository.findById(id).orElseThrow(() -> new NotFoundException("User not found"));
        return userMapper.toResponse(user);
    }

    public Page<UserResponse> searchUsers(String name, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return userRepository.findByNameContainingIgnoreCase(name, pageable)
                .map(userMapper::toResponse);
    }

    private User getUserByToken(String token) {
        UUID userId = jwtUtils.getIdFromToken(token);
        return userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("Invalid token"));
    }
}
