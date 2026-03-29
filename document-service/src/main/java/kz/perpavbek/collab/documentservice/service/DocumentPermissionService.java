package kz.perpavbek.collab.documentservice.service;

import kz.perpavbek.collab.documentservice.entity.Document;
import kz.perpavbek.collab.documentservice.enums.Role;
import kz.perpavbek.collab.documentservice.security.JwtUtils;
import kz.perpavbek.collab.documentservice.exception.AccessDeniedException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.Arrays;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DocumentPermissionService {

    private final JwtUtils jwtUtils;

    public void checkPermission(Document document, Role... allowedRoles) {
        UUID currentUserId = jwtUtils.getIdFromToken(jwtUtils.getCurrentToken());

        if (document.getOwnerId().equals(currentUserId)) {
            return;
        }

        boolean hasRole = document.getCollaborators().stream()
                .filter(c -> c.getUserId().equals(currentUserId))
                .anyMatch(c -> Arrays.asList(allowedRoles).contains(c.getRole()));

        if (!hasRole) {
            throw new AccessDeniedException("User not authorized for this document");
        }
    }
}
