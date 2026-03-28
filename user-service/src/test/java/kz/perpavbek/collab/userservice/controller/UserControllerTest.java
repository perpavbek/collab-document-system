package kz.perpavbek.collab.userservice.controller;

import kz.perpavbek.collab.userservice.dto.response.PageResponse;
import kz.perpavbek.collab.userservice.dto.response.UserResponse;
import kz.perpavbek.collab.userservice.service.UserService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getCurrentUser_shouldReturnUserResponse() {
        UserResponse response = new UserResponse();
        when(userService.getCurrentUser("Bearer token")).thenReturn(response);

        ResponseEntity<UserResponse> result = userController.getCurrentUser("Bearer token");
        assertEquals(response, result.getBody());
    }

    @Test
    void getUser_shouldReturnUserResponse() {
        UUID id = UUID.randomUUID();
        UserResponse response = new UserResponse();
        when(userService.getUserById(id)).thenReturn(response);

        ResponseEntity<UserResponse> result = userController.getUser(id);
        assertEquals(response, result.getBody());
    }

    @Test
    void searchUsers_shouldReturnPageResponse() {
        UserResponse user = new UserResponse();
        Page<UserResponse> page = new PageImpl<>(List.of(user));
        when(userService.searchUsers("name", 0, 10)).thenReturn(page);

        ResponseEntity<PageResponse<UserResponse>> result = userController.searchUsers("name", 0, 10);
        Assertions.assertNotNull(result.getBody());
        assertEquals(1, result.getBody().getContent().size());
    }
}