package kz.perpavbek.collab.versioncontrolservice.controller;

import kz.perpavbek.collab.versioncontrolservice.dto.response.DocumentVersionResponse;
import kz.perpavbek.collab.versioncontrolservice.dto.response.EditOperationResponse;
import kz.perpavbek.collab.versioncontrolservice.service.VersionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class VersionControllerTest {

    @Mock
    private VersionService versionService;

    @InjectMocks
    private VersionController versionController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getFullDocument_shouldReturnContent() {

        UUID documentId = UUID.randomUUID();
        String content = "document content";

        when(versionService.getFullDocument(documentId)).thenReturn(content);

        ResponseEntity<String> result =
                versionController.getFullDocument(documentId);

        assertEquals(200, result.getStatusCode().value());
        assertEquals(content, result.getBody());

        verify(versionService).getFullDocument(documentId);
    }

    @Test
    void getOperations_shouldReturnOperationsList() {

        UUID documentId = UUID.randomUUID();

        EditOperationResponse op = new EditOperationResponse();
        List<EditOperationResponse> operations = List.of(op);

        when(versionService.getOperationsAfter(documentId, 0))
                .thenReturn(operations);

        ResponseEntity<List<EditOperationResponse>> result =
                versionController.getOperations(documentId, 0);

        assertEquals(200, result.getStatusCode().value());
        assertEquals(operations, result.getBody());

        verify(versionService).getOperationsAfter(documentId, 0);
    }

    @Test
    void getDocumentAtVersion_shouldReturnDocumentVersion() {

        UUID documentId = UUID.randomUUID();
        long seq = 5;

        DocumentVersionResponse response =
                DocumentVersionResponse.builder()
                        .content("test")
                        .sequenceNumber(seq)
                        .build();

        when(versionService.getDocumentAtVersion(documentId, seq))
                .thenReturn(response);

        ResponseEntity<DocumentVersionResponse> result =
                versionController.getDocumentAtVersion(documentId, seq);

        assertEquals(200, result.getStatusCode().value());
        assertEquals(response, result.getBody());

        verify(versionService)
                .getDocumentAtVersion(documentId, seq);
    }
}