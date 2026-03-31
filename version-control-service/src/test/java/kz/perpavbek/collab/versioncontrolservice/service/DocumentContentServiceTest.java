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
    private DocumentSnapshotRepository snapshotRepository;

    @Mock
    private EditOperationRepository operationRepository;

    @InjectMocks
    private DocumentContentService service;

    @Test
    void shouldBuildDocument() {

        UUID docId = UUID.randomUUID();

        DocumentSnapshot snapshot = new DocumentSnapshot();
        snapshot.setContent("Hello");
        snapshot.setLastOperationSequence(1);

        EditOperation op = new EditOperation();
        op.setType(OperationType.INSERT);
        op.setPosition(5);
        op.setContent(" World");

        when(snapshotRepository
                .findTopByDocumentIdOrderByLastOperationSequenceDesc(docId))
                .thenReturn(Optional.of(snapshot));

        when(operationRepository
                .findByDocumentIdAndSequenceNumberGreaterThanOrderBySequenceNumberAsc(docId,1))
                .thenReturn(List.of(op));

        String result = service.buildDocument(docId);

        assertEquals("Hello World", result);
    }

    @Test
    void shouldReturnLength() {

        UUID docId = UUID.randomUUID();

        when(snapshotRepository
                .findTopByDocumentIdOrderByLastOperationSequenceDesc(docId))
                .thenReturn(Optional.empty());

        when(operationRepository
                .findByDocumentIdAndSequenceNumberGreaterThanOrderBySequenceNumberAsc(docId,0))
                .thenReturn(List.of());

        assertEquals(0, service.getDocumentLength(docId));
    }
}