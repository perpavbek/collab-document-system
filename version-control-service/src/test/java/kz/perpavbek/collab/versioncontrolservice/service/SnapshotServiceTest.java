package kz.perpavbek.collab.versioncontrolservice.service;

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

    @InjectMocks
    private SnapshotService service;

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

        service.createSnapshotIfNeeded(docId);

        verify(contentService).buildDocument(docId);
        verify(snapshotRepository).save(any());
    }
}