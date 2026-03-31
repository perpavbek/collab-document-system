package kz.perpavbek.collab.documentservice.service;

import kz.perpavbek.collab.documentservice.entity.Document;
import kz.perpavbek.collab.documentservice.entity.DocumentCollaborator;
import kz.perpavbek.collab.documentservice.enums.Role;
import kz.perpavbek.collab.documentservice.exception.AccessDeniedException;
import kz.perpavbek.collab.documentservice.exception.NotFoundException;
import kz.perpavbek.collab.documentservice.repository.DocumentRepository;
import kz.perpavbek.collab.documentservice.security.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class DocumentAccessServiceTest {

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private JwtUtils jwtUtils;

    @InjectMocks
    private DocumentAccessService accessService;

    private UUID userId;
    private UUID docId;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        userId = UUID.randomUUID();
        docId = UUID.randomUUID();
    }

    @Test
    void getDocumentOrThrow_notFound_shouldThrow() {
        when(documentRepository.findById(docId)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> accessService.getDocumentOrThrow(docId));
    }

    @Test
    void checkPermission_owner_shouldAllow() {
        Document doc = Document.builder().ownerId(userId).build();
        when(jwtUtils.getIdFromToken("token")).thenReturn(userId);
        when(jwtUtils.getCurrentToken()).thenReturn("token");

        assertDoesNotThrow(() -> accessService.checkPermission(doc, Role.EDITOR));
    }

    @Test
    void checkPermission_notAllowed_shouldThrow() {
        DocumentCollaborator collab = DocumentCollaborator.builder()
                .userId(UUID.randomUUID())
                .role(Role.VIEWER)
                .build();
        Document doc = Document.builder().ownerId(UUID.randomUUID())
                .collaborators(List.of(collab)).build();

        when(jwtUtils.getIdFromToken("token")).thenReturn(userId);
        when(jwtUtils.getCurrentToken()).thenReturn("token");

        assertThrows(AccessDeniedException.class, () -> accessService.checkPermission(doc, Role.EDITOR));
    }

    @Test
    void getUserRole_ownerAndCollaborator() {
        DocumentCollaborator collab = DocumentCollaborator.builder()
                .userId(userId)
                .role(Role.EDITOR)
                .build();
        Document doc = Document.builder().ownerId(userId).collaborators(List.of(collab)).build();

        when(documentRepository.findById(docId)).thenReturn(Optional.of(doc));

        assertEquals(Role.OWNER, accessService.getUserRole(docId, userId));
    }
}
