package kz.perpavbek.collab.userservice.controller;

import kz.perpavbek.collab.userservice.dto.request.LoginRequest;
import kz.perpavbek.collab.userservice.dto.request.RegisterRequest;
import kz.perpavbek.collab.userservice.dto.response.AuthResponse;
import kz.perpavbek.collab.userservice.service.UserService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

class AuthControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void register_shouldReturnAuthResponse() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@example.com");
        request.setPassword("password");

        AuthResponse response = new AuthResponse("jwt-token");
        when(userService.register(request)).thenReturn(response);

        ResponseEntity<AuthResponse> result = authController.register(request);
        Assertions.assertNotNull(result.getBody());
        assertEquals("jwt-token", result.getBody().getToken());
    }

    @Test
    void login_shouldReturnAuthResponse() {
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("password");

        AuthResponse response = new AuthResponse("jwt-token");
        when(userService.login(request)).thenReturn(response);

        ResponseEntity<AuthResponse> result = authController.login(request);
        Assertions.assertNotNull(result.getBody());
        assertEquals("jwt-token", result.getBody().getToken());
    }
}
