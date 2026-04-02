package kz.perpavbek.collab.documentservice.controller;

import kz.perpavbek.collab.documentservice.dto.client.OperationRequest;
import kz.perpavbek.collab.documentservice.dto.client.OperationResponse;
import kz.perpavbek.collab.documentservice.dto.request.DocumentCreateRequest;
import kz.perpavbek.collab.documentservice.dto.request.DocumentUpdateRequest;
import kz.perpavbek.collab.documentservice.dto.response.DocumentInvitationDetailsResponse;
import kz.perpavbek.collab.documentservice.dto.response.DocumentResponse;
import kz.perpavbek.collab.documentservice.dto.response.PermissionResponse;
import kz.perpavbek.collab.documentservice.enums.OperationType;
import kz.perpavbek.collab.documentservice.enums.Role;
import kz.perpavbek.collab.documentservice.security.JwtUtils;
import kz.perpavbek.collab.documentservice.service.DocumentAccessService;
import kz.perpavbek.collab.documentservice.service.DocumentInvitationService;
import kz.perpavbek.collab.documentservice.service.DocumentOperationService;
import kz.perpavbek.collab.documentservice.service.DocumentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

class DocumentControllerTest {

    @Mock
    private DocumentService documentService;

    @Mock
    private DocumentOperationService documentOperationService;

    @Mock
    private DocumentAccessService documentAccessService;

    @Mock
    private DocumentInvitationService documentInvitationService;

    @Mock
    private JwtUtils jwtUtils;

    @InjectMocks
    private DocumentController documentController;

    private UUID documentId;
    private UUID userId;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        documentId = UUID.randomUUID();
        userId = UUID.randomUUID();
    }

    @Test
    void createDocument_shouldReturnDocumentResponse() {
        DocumentCreateRequest request = new DocumentCreateRequest("Test Doc", List.of(userId));
        DocumentResponse response = DocumentResponse.builder()
                .id(documentId)
                .title("Test Doc")
                .ownerId(userId)
                .build();

        when(documentService.createDocument(request)).thenReturn(response);

        ResponseEntity<DocumentResponse> result = documentController.createDocument(request);

        assertNotNull(result.getBody());
        assertEquals("Test Doc", result.getBody().getTitle());
        assertEquals(documentId, result.getBody().getId());
    }

    @Test
    void getUserPermission_shouldReturnPermission() {
        when(jwtUtils.getCurrentToken()).thenReturn("token");
        when(jwtUtils.getIdFromToken("token")).thenReturn(userId);
        when(documentAccessService.getUserRole(documentId, userId)).thenReturn(Role.EDITOR);

        ResponseEntity<PermissionResponse> result = documentController.getUserPermission(documentId);

        assertNotNull(result.getBody());
        assertEquals(Role.EDITOR, result.getBody().getRole());
    }

    @Test
    void getInvitationDetails_shouldReturnInvitationPayload() {
        String token = UUID.randomUUID().toString();
        DocumentInvitationDetailsResponse response = DocumentInvitationDetailsResponse.builder()
                .documentId(documentId)
                .documentTitle("Test Doc")
                .ownerId(userId)
                .build();

        when(documentInvitationService.getInvitationDetails(token)).thenReturn(response);

        ResponseEntity<DocumentInvitationDetailsResponse> result = documentController.getInvitationDetails(token);

        assertNotNull(result.getBody());
        assertEquals(documentId, result.getBody().getDocumentId());
        assertEquals("Test Doc", result.getBody().getDocumentTitle());
    }

    @Test
    void acceptInvitation_shouldReturnDocument() {
        String token = UUID.randomUUID().toString();
        DocumentResponse response = DocumentResponse.builder()
                .id(documentId)
                .title("Accepted Doc")
                .ownerId(userId)
                .build();

        when(documentInvitationService.acceptInvitation(token)).thenReturn(response);

        ResponseEntity<DocumentResponse> result = documentController.acceptInvitation(token);

        assertNotNull(result.getBody());
        assertEquals(documentId, result.getBody().getId());
        assertEquals("Accepted Doc", result.getBody().getTitle());
    }

    @Test
    void editDocument_shouldReturnOperationResponse() {
        OperationRequest request = new OperationRequest(documentId, 0, "Hello", null, OperationType.INSERT);
        OperationResponse response = new OperationResponse();
        response.setDocumentId(documentId);
        response.setUserId(userId);
        response.setContent("Hello");
        response.setType(OperationType.INSERT);

        when(documentOperationService.applyEdit(request)).thenReturn(response);

        ResponseEntity<OperationResponse> result = documentController.editDocument(request);

        assertNotNull(result.getBody());
        assertEquals("Hello", result.getBody().getContent());
    }

    @Test
    void updateDocument_shouldReturnUpdatedDocument() {
        DocumentUpdateRequest request = new DocumentUpdateRequest();
        request.setTitle("Updated Title");

        DocumentResponse response = DocumentResponse.builder()
                .id(documentId)
                .title("Updated Title")
                .ownerId(userId)
                .build();

        when(documentService.updateDocumentMeta(documentId, request)).thenReturn(response);

        ResponseEntity<DocumentResponse> result = documentController.updateDocument(documentId, request);

        assertNotNull(result.getBody());
        assertEquals("Updated Title", result.getBody().getTitle());
    }

    @Test
    void deleteDocument_shouldReturnNoContent() {
        ResponseEntity<Void> result = documentController.deleteDocument(documentId);

        assertEquals(204, result.getStatusCode().value());
    }
}
