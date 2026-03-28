package kz.perpavbek.collab.userservice.service;

import kz.perpavbek.collab.userservice.dto.request.LoginRequest;
import kz.perpavbek.collab.userservice.dto.request.RegisterRequest;
import kz.perpavbek.collab.userservice.dto.response.AuthResponse;
import kz.perpavbek.collab.userservice.entity.User;
import kz.perpavbek.collab.userservice.mapper.UserMapper;
import kz.perpavbek.collab.userservice.repository.UserRepository;
import kz.perpavbek.collab.userservice.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        userRepository.save(user);

        String token = jwtUtils.generateToken(user.getEmail());

        return new AuthResponse(token);
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        String token = jwtUtils.generateToken(user.getEmail());

        return new AuthResponse(token);
    }
}
