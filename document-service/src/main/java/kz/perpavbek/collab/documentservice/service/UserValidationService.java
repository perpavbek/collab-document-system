package kz.perpavbek.collab.documentservice.service;

import feign.FeignException;
import kz.perpavbek.collab.documentservice.client.UserServiceClient;
import kz.perpavbek.collab.documentservice.dto.client.User;
import kz.perpavbek.collab.documentservice.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserValidationService {

    private final UserServiceClient userServiceClient;

    public User getCurrentUser() {

        try {
            return userServiceClient.getUserByToken();
        } catch (FeignException.NotFound e) {
            throw new NotFoundException("User does not exist");
        }
    }

    public User getUser(UUID userId) {

        try {
            return userServiceClient.getUserById(userId);
        } catch (FeignException.NotFound e) {
            throw new NotFoundException("User does not exist");
        }
    }

    public void validateUser(UUID userId) {

        getUser(userId);
    }

    public void validateUsers(List<UUID> userIds) {

        getUsersByIds(userIds);
    }

    public Map<UUID, User> getUsersByIds(List<UUID> userIds) {

        List<UUID> normalizedIds = userIds == null
                ? List.of()
                : userIds.stream()
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        Map<UUID, User> users = new LinkedHashMap<>();
        List<UUID> missing = new ArrayList<>();

        normalizedIds.forEach(userId -> {
            try {
                users.put(userId, getUser(userId));
            } catch (NotFoundException e) {
                missing.add(userId);
            }
        });

        if (!missing.isEmpty()) {
            throw new IllegalArgumentException("Users not found: " + missing);
        }

        return users;
    }
}
