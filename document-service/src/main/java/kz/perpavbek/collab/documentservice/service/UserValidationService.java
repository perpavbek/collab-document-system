package kz.perpavbek.collab.documentservice.service;

import feign.FeignException;
import kz.perpavbek.collab.documentservice.client.UserServiceClient;
import kz.perpavbek.collab.documentservice.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserValidationService {

    private final UserServiceClient userServiceClient;

    public void validateUser(UUID userId) {

        try {
            userServiceClient.getUserById(userId);
        } catch (FeignException.NotFound e) {
            throw new NotFoundException("User does not exist");
        }
    }

    public void validateUsers(List<UUID> userIds) {

        List<UUID> missing = userIds.stream()
                .filter(id -> {
                    try {
                        validateUser(id);
                        return false;
                    } catch (NotFoundException e) {
                        return true;
                    }
                })
                .toList();

        if (!missing.isEmpty()) {
            throw new IllegalArgumentException("Users not found: " + missing);
        }
    }
}
