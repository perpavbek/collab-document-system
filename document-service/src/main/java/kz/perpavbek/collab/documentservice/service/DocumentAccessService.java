package kz.perpavbek.collab.documentservice.service;

import kz.perpavbek.collab.documentservice.entity.Document;
import kz.perpavbek.collab.documentservice.entity.DocumentCollaborator;
import kz.perpavbek.collab.documentservice.enums.Role;
import kz.perpavbek.collab.documentservice.exception.AccessDeniedException;
import kz.perpavbek.collab.documentservice.exception.NotFoundException;
import kz.perpavbek.collab.documentservice.repository.DocumentRepository;
import kz.perpavbek.collab.documentservice.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DocumentAccessService {

    private final DocumentRepository documentRepository;
    private final JwtUtils jwtUtils;

    public void checkPermission(Document document, Role... allowedRoles) {

        UUID userId = jwtUtils.getIdFromToken(jwtUtils.getCurrentToken());

        if (document.getOwnerId().equals(userId)) {
            return;
        }

        boolean allowed = document.getCollaborators().stream()
                .filter(c -> c.getUserId().equals(userId))
                .anyMatch(c -> List.of(allowedRoles).contains(c.getRole()));

        if (!allowed) {
            throw new AccessDeniedException("User not authorized for this document");
        }
    }

    public Role getUserRole(UUID documentId, UUID userId) {

        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new NotFoundException("Document not found"));

        if (document.getOwnerId().equals(userId)) {
            return Role.OWNER;
        }

        return document.getCollaborators().stream()
                .filter(c -> c.getUserId().equals(userId))
                .map(DocumentCollaborator::getRole)
                .findFirst()
                .orElse(null);
    }
}
