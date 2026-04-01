package kz.perpavbek.collab.documentservice.service;

import kz.perpavbek.collab.documentservice.client.VersionControlClient;
import kz.perpavbek.collab.documentservice.dto.client.OperationRequest;
import kz.perpavbek.collab.documentservice.dto.client.OperationResponse;
import kz.perpavbek.collab.documentservice.entity.Document;
import kz.perpavbek.collab.documentservice.enums.Role;
import kz.perpavbek.collab.documentservice.exception.AccessDeniedException;
import kz.perpavbek.collab.documentservice.repository.DocumentRepository;
import kz.perpavbek.collab.documentservice.security.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class DocumentOperationServiceTest {

    @Mock
    private VersionControlClient versionControlClient;

    @Mock
    private DocumentAccessService accessService;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private DocumentService documentService;

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private DocumentEventService documentEventService;

    @Mock
    private DocumentSessionService documentSessionService;

    @InjectMocks
    private DocumentOperationService operationService;

    private UUID docId;
    private UUID userId;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        docId = UUID.randomUUID();
        userId = UUID.randomUUID();
    }

    @Test
    void applyEdit_userCannotEdit_shouldThrow() {
        OperationRequest request = new OperationRequest(docId, 0, "text", null, null);
        when(jwtUtils.getCurrentToken()).thenReturn("token");
        when(jwtUtils.getIdFromToken("token")).thenReturn(userId);
        when(accessService.getUserRole(docId, userId)).thenReturn(Role.VIEWER);

        assertThrows(AccessDeniedException.class, () -> operationService.applyEdit(request));
    }

    @Test
    void applyEdit_allowed_shouldReturnResponse() {
        OperationRequest request = new OperationRequest(docId, 0, "text", null, null);
        OperationResponse response = new OperationResponse();
        response.setDocumentId(docId);

        when(jwtUtils.getCurrentToken()).thenReturn("token");
        when(jwtUtils.getIdFromToken("token")).thenReturn(userId);
        when(accessService.getUserRole(docId, userId)).thenReturn(Role.EDITOR);
        when(versionControlClient.saveOperation(request)).thenReturn(response);
        when(documentService.getDocumentOrThrow(docId)).thenReturn(new Document());

        OperationResponse result = operationService.applyEdit(request);

        assertEquals(docId, result.getDocumentId());
    }
}
