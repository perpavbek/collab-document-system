package kz.perpavbek.collab.versioncontrolservice.service;

import kz.perpavbek.collab.versioncontrolservice.mapper.EditOperationMapper;
import kz.perpavbek.collab.versioncontrolservice.repository.EditOperationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
    void shouldDeleteHistory() {

        UUID docId = UUID.randomUUID();

        service.deleteDocumentHistory(docId);

        verify(operationService).deleteOperations(docId);
        verify(snapshotService).deleteSnapshots(docId);
    }
}