package kz.perpavbek.collab.versioncontrolservice.service;

import kz.perpavbek.collab.versioncontrolservice.entity.DocumentSnapshot;
import kz.perpavbek.collab.versioncontrolservice.entity.EditOperation;
import kz.perpavbek.collab.versioncontrolservice.enums.OperationType;
import kz.perpavbek.collab.versioncontrolservice.repository.DocumentSnapshotRepository;
import kz.perpavbek.collab.versioncontrolservice.repository.EditOperationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentContentServiceTest {

    @Mock
    private EditOperationRepository operationRepository;

    @Mock
    private SnapshotService snapshotService;

    @Mock
    private DocumentBuilderService documentBuilderService;

    @InjectMocks
    private DocumentContentService contentService;

    @Test
    void shouldBuildDocument() {
        UUID docId = UUID.randomUUID();
        DocumentSnapshot snapshot = new DocumentSnapshot();
        snapshot.setContent("Hello");
        snapshot.setLastOperationSequence(1);

        when(snapshotService.findSnapshotBeforeOrAt(docId, 2L)).thenReturn(snapshot);
        when(documentBuilderService.buildDocument(docId, snapshot, 2L)).thenReturn("Hello World");

        String result = contentService.buildDocumentBySequenceNumber(docId, 2L);

        assertEquals("Hello World", result);
    }

    @Test
    void shouldReturnDocumentLength() {
        UUID docId = UUID.randomUUID();

        when(documentBuilderService.buildDocument(docId, null, 0L)).thenReturn("Hello");
        when(operationRepository.findTopByDocumentIdOrderBySequenceNumberDesc(docId)).thenReturn(Optional.empty());

        assertEquals(5, contentService.getDocumentLength(docId));
    }
}