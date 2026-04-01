package kz.perpavbek.collab.versioncontrolservice.service;

import kz.perpavbek.collab.versioncontrolservice.entity.DocumentSnapshot;
import kz.perpavbek.collab.versioncontrolservice.entity.EditOperation;
import kz.perpavbek.collab.versioncontrolservice.repository.DocumentSnapshotRepository;
import kz.perpavbek.collab.versioncontrolservice.repository.EditOperationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SnapshotServiceTest {

    @Mock
    private DocumentSnapshotRepository snapshotRepository;

    @Mock
    private EditOperationRepository operationRepository;

    @Mock
    private DocumentContentService contentService;

    @Mock
    private DocumentBuilderService documentBuilderService;

    @InjectMocks
    private SnapshotService service;

    @Test
    void shouldCreateSnapshot() {

        UUID docId = UUID.randomUUID();

        when(documentBuilderService.buildDocument(docId, null, 10L)).thenReturn("content");

        EditOperation op = new EditOperation();
        op.setSequenceNumber(10L);

        when(operationRepository
                .findTopByDocumentIdOrderBySequenceNumberDesc(docId))
                .thenReturn(Optional.of(op));

        service.createSnapshot(docId);

        verify(documentBuilderService).buildDocument(docId, null, 10L);
        verify(snapshotRepository).save(any());
    }

    @Test
    void shouldCreateSnapshotWithProvidedContent() {

        UUID docId = UUID.randomUUID();

        service.createSnapshot(docId, "text", 15L);

        verify(snapshotRepository).save(argThat(snapshot ->
                snapshot.getDocumentId().equals(docId) &&
                        snapshot.getContent().equals("text") &&
                        snapshot.getLastOperationSequence() == 15L
        ));
    }

    @Test
    void shouldFindSnapshotBeforeOrAt() {

        UUID docId = UUID.randomUUID();

        when(snapshotRepository
                .findTopByDocumentIdAndLastOperationSequenceLessThanEqualOrderByLastOperationSequenceDesc(docId, 100))
                .thenReturn(Optional.of(new DocumentSnapshot()));

        service.findSnapshotBeforeOrAt(docId, 100);

        verify(snapshotRepository)
                .findTopByDocumentIdAndLastOperationSequenceLessThanEqualOrderByLastOperationSequenceDesc(docId, 100);
    }

    @Test
    void shouldCreateSnapshotIfThresholdReached() {

        UUID docId = UUID.randomUUID();

        when(snapshotRepository
                .findTopByDocumentIdOrderByLastOperationSequenceDesc(docId))
                .thenReturn(Optional.empty());

        EditOperation op = new EditOperation();
        op.setSequenceNumber(50L);

        when(operationRepository
                .findTopByDocumentIdOrderBySequenceNumberDesc(docId))
                .thenReturn(Optional.of(op));

        when(documentBuilderService.buildDocument(docId, null, 50L)).thenReturn("content");

        service.createSnapshotIfNeeded(docId);

        verify(snapshotRepository).save(any());
    }

    @Test
    void shouldNotCreateSnapshotIfThresholdNotReached() {

        UUID docId = UUID.randomUUID();

        when(snapshotRepository
                .findTopByDocumentIdOrderByLastOperationSequenceDesc(docId))
                .thenReturn(Optional.of(new DocumentSnapshot()));

        EditOperation op = new EditOperation();
        op.setSequenceNumber(10L);

        when(operationRepository
                .findTopByDocumentIdOrderBySequenceNumberDesc(docId))
                .thenReturn(Optional.of(op));

        service.createSnapshotIfNeeded(docId);

        verify(snapshotRepository, never()).save(any());
    }

    @Test
    void shouldDeleteSnapshots() {

        UUID docId = UUID.randomUUID();

        service.deleteSnapshots(docId);

        verify(snapshotRepository).deleteByDocumentId(docId);
    }
}