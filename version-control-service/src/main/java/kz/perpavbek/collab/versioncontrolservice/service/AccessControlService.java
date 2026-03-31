package kz.perpavbek.collab.versioncontrolservice.service;

import kz.perpavbek.collab.versioncontrolservice.client.DocumentClient;
import kz.perpavbek.collab.versioncontrolservice.dto.client.PermissionResponse;
import kz.perpavbek.collab.versioncontrolservice.enums.CollaboratorRole;
import kz.perpavbek.collab.versioncontrolservice.exception.AccessDeniedException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccessControlService {

    private final DocumentClient documentClient;

    public void checkAccess(UUID documentId, CollaboratorRole... allowedRoles) {

        PermissionResponse permission = documentClient.getPermission(documentId);

        boolean allowed = Arrays.stream(allowedRoles)
                .anyMatch(role -> role == permission.getRole());

        if (permission.getRole() == null || !allowed) {
            throw new AccessDeniedException("User has no access to this document");
        }
    }
}
