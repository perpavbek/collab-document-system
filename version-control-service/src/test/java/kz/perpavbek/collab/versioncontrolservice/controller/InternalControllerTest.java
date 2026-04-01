package kz.perpavbek.collab.versioncontrolservice.controller;

import kz.perpavbek.collab.versioncontrolservice.dto.request.EditOperationRequest;
import kz.perpavbek.collab.versioncontrolservice.dto.request.RollbackRequest;
import kz.perpavbek.collab.versioncontrolservice.dto.response.EditOperationResponse;
import kz.perpavbek.collab.versioncontrolservice.service.VersionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class InternalControllerTest {

    @Mock
    private VersionService versionService;

    @InjectMocks
    private InternalController internalController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void saveOperation_shouldReturnResponse() {

        EditOperationRequest request = new EditOperationRequest();
        EditOperationResponse response = new EditOperationResponse();

        when(versionService.saveOperation(request)).thenReturn(response);

        ResponseEntity<EditOperationResponse> result =
                internalController.saveOperation(request);

        assertEquals(200, result.getStatusCode().value());
        assertEquals(response, result.getBody());

        verify(versionService).saveOperation(request);
    }

    @Test
    void rollback_shouldCallService() {

        UUID documentId = UUID.randomUUID();
        long targetSeq = 10;

        RollbackRequest request = new RollbackRequest();
        request.setDocumentId(documentId);
        request.setTargetSequence(targetSeq);

        ResponseEntity<Void> result =
                internalController.rollback(request);

        assertEquals(200, result.getStatusCode().value());

        verify(versionService)
                .rollbackDocument(documentId, targetSeq);
    }

    @Test
    void deleteDocumentHistory_shouldReturnNoContent() {

        UUID documentId = UUID.randomUUID();

        ResponseEntity<Void> result =
                internalController.deleteDocumentHistory(documentId);

        assertEquals(204, result.getStatusCode().value());

        verify(versionService).deleteDocumentHistory(documentId);
    }
}