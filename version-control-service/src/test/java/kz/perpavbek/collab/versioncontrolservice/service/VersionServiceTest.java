package kz.perpavbek.collab.versioncontrolservice.service;

import kz.perpavbek.collab.versioncontrolservice.dto.request.EditOperationRequest;
import kz.perpavbek.collab.versioncontrolservice.dto.response.DocumentVersionResponse;
import kz.perpavbek.collab.versioncontrolservice.dto.response.EditOperationResponse;
import kz.perpavbek.collab.versioncontrolservice.entity.EditOperation;
import kz.perpavbek.collab.versioncontrolservice.mapper.EditOperationMapper;
import kz.perpavbek.collab.versioncontrolservice.repository.EditOperationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class VersionServiceTest {

    @Mock
    private AccessControlService accessService;

    @Mock
    private DocumentContentService contentService;

    @Mock
    private EditOperationService operationService;

    @Mock
    private EditOperationRepository repository;

    @Mock
    private EditOperationMapper mapper;

    @Mock
    private SnapshotService snapshotService;

    @InjectMocks
    private VersionService service;

    @Test
    void shouldReturnFullDocument() {

        UUID docId = UUID.randomUUID();

        when(contentService.buildDocument(docId)).thenReturn("text");

        String result = service.getFullDocument(docId);

        assertEquals("text", result);

        verify(accessService).checkAccess(any(), any(), any(), any());
    }

    @Test
    void shouldSaveOperation() {

        EditOperationRequest request = new EditOperationRequest();
        request.setDocumentId(UUID.randomUUID());

        EditOperation entity = new EditOperation();
        EditOperationResponse response = new EditOperationResponse();

        when(operationService.saveOperation(request)).thenReturn(entity);
        when(mapper.toResponse(entity)).thenReturn(response);

        EditOperationResponse result = service.saveOperation(request);

        assertEquals(response, result);

        verify(accessService).checkAccess(any(), any(), any());
        verify(operationService).saveOperation(request);
        verify(mapper).toResponse(entity);
    }

    @Test
    void shouldReturnOperationsAfterSequence() {

        UUID docId = UUID.randomUUID();
        long seq = 5;

        List<EditOperation> entities = List.of(new EditOperation());
        List<EditOperationResponse> responses = List.of(new EditOperationResponse());

        when(repository
                .findByDocumentIdAndSequenceNumberGreaterThanOrderBySequenceNumberAsc(docId, seq))
                .thenReturn(entities);

        when(mapper.toResponseList(entities)).thenReturn(responses);

        var result = service.getOperationsAfter(docId, seq);

        assertEquals(responses, result);

        verify(accessService).checkAccess(any(), any(), any(), any());
    }

    @Test
    void shouldRollbackDocument() {

        UUID docId = UUID.randomUUID();

        EditOperation op = new EditOperation();
        op.setSequenceNumber(10L);

        when(repository
                .findTopByDocumentIdOrderBySequenceNumberDesc(docId))
                .thenReturn(Optional.of(op));

        when(contentService.buildDocumentBySequenceNumber(docId, 5))
                .thenReturn("rolled content");

        service.rollbackDocument(docId, 5);

        verify(repository)
                .deleteByDocumentIdAndSequenceNumberGreaterThan(docId, 5);

        verify(snapshotService)
                .createSnapshot(docId, "rolled content", 5);
    }

    @Test
    void shouldThrowIfRollbackVersionDoesNotExist() {

        UUID docId = UUID.randomUUID();

        EditOperation op = new EditOperation();
        op.setSequenceNumber(5L);

        when(repository
                .findTopByDocumentIdOrderBySequenceNumberDesc(docId))
                .thenReturn(Optional.of(op));

        assertThrows(
                IllegalArgumentException.class,
                () -> service.rollbackDocument(docId, 10)
        );
    }

    @Test
    void shouldReturnDocumentAtVersion() {

        UUID docId = UUID.randomUUID();

        when(contentService.buildDocumentBySequenceNumber(docId, 7))
                .thenReturn("content");

        DocumentVersionResponse result = service.getDocumentAtVersion(docId, 7);

        assertEquals("content", result.getContent());
        assertEquals(7, result.getSequenceNumber());

        verify(accessService).checkAccess(any(), any(), any(), any());
    }

    @Test
    void shouldDeleteHistory() {

        UUID docId = UUID.randomUUID();

        service.deleteDocumentHistory(docId);

        verify(operationService).deleteOperations(docId);
        verify(snapshotService).deleteSnapshots(docId);
    }
}