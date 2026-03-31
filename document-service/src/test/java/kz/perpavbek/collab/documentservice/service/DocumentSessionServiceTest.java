package kz.perpavbek.collab.documentservice.service;

import kz.perpavbek.collab.documentservice.dto.response.DocumentSessionResponse;
import kz.perpavbek.collab.documentservice.entity.DocumentSession;
import kz.perpavbek.collab.documentservice.repository.DocumentSessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

class DocumentSessionServiceTest {

    @Mock
    private DocumentSessionRepository repository;

    @InjectMocks
    private DocumentSessionService sessionService;

    private UUID docId;
    private UUID userId;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        docId = UUID.randomUUID();
        userId = UUID.randomUUID();
    }

    @Test
    void getActiveSessions_shouldReturnMappedResponses() {
        DocumentSession session = DocumentSession.builder()
                .documentId(docId)
                .userId(userId)
                .connectedAt(LocalDateTime.now())
                .lastActivityAt(LocalDateTime.now())
                .build();

        when(repository.findByDocumentId(docId)).thenReturn(List.of(session));

        List<DocumentSessionResponse> result = sessionService.getActiveSessions(docId);

        assertEquals(1, result.size());
        assertEquals(userId, result.get(0).getUserId());
    }
}
