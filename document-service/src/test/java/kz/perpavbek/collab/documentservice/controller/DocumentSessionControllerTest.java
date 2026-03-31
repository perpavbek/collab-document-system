package kz.perpavbek.collab.documentservice.controller;

import kz.perpavbek.collab.documentservice.dto.response.DocumentSessionResponse;
import kz.perpavbek.collab.documentservice.service.DocumentSessionService;
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

class DocumentSessionControllerTest {

    @Mock
    private DocumentSessionService documentSessionService;

    @InjectMocks
    private DocumentSessionController documentSessionController;

    private UUID documentId;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        documentId = UUID.randomUUID();
    }

    @Test
    void getSessions_shouldReturnActiveSessions() {
        DocumentSessionResponse session = new DocumentSessionResponse(documentId, UUID.randomUUID(), null, null);
        when(documentSessionService.getActiveSessions(documentId)).thenReturn(List.of(session));

        ResponseEntity<List<DocumentSessionResponse>> result = documentSessionController.getSessions(documentId);

        assertNotNull(result.getBody());
        assertEquals(1, result.getBody().size());
        assertEquals(documentId, result.getBody().getFirst().getDocumentId());
    }
}
