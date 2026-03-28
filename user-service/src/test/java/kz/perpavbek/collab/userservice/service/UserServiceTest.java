package kz.perpavbek.collab.userservice.service;

import kz.perpavbek.collab.userservice.dto.request.LoginRequest;
import kz.perpavbek.collab.userservice.dto.request.RegisterRequest;
import kz.perpavbek.collab.userservice.dto.response.UserResponse;
import kz.perpavbek.collab.userservice.entity.User;
import kz.perpavbek.collab.userservice.enums.Role;
import kz.perpavbek.collab.userservice.mapper.UserMapper;
import kz.perpavbek.collab.userservice.repository.UserRepository;
import kz.perpavbek.collab.userservice.security.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserMapper userMapper;

    @Mock
    private JwtUtils jwtUtils;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void register_shouldReturnAuthResponse() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@example.com");
        request.setPassword("password");

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        User user = new User();
        user.setEmail(request.getEmail());
        user.setRole(Role.USER);
        when(userMapper.toEntity(request)).thenReturn(user);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encoded");
        when(jwtUtils.generateToken(user)).thenReturn("jwt-token");

        assertEquals("jwt-token", userService.register(request).getToken());
        verify(userRepository).save(user);
    }

    @Test
    void login_shouldReturnAuthResponse() {
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("password");

        User user = new User();
        user.setPassword("encoded");
        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.getPassword(), user.getPassword())).thenReturn(true);
        when(jwtUtils.generateToken(user)).thenReturn("jwt-token");

        assertEquals("jwt-token", userService.login(request).getToken());
    }

    @Test
    void searchUsers_shouldReturnPage() {
        User user = new User();
        UserResponse response = new UserResponse();
        Page<User> page = new PageImpl<>(List.of(user));
        when(userRepository.findByNameContainingIgnoreCase(anyString(), any(Pageable.class))).thenReturn(page);
        when(userMapper.toResponse(user)).thenReturn(response);

        Page<UserResponse> result = userService.searchUsers("name", 0, 10);
        assertEquals(1, result.getTotalElements());
    }
}
