package kz.perpavbek.collab.versioncontrolservice.service;

import kz.perpavbek.collab.versioncontrolservice.entity.DocumentSnapshot;
import kz.perpavbek.collab.versioncontrolservice.entity.EditOperation;
import kz.perpavbek.collab.versioncontrolservice.enums.OperationType;
import kz.perpavbek.collab.versioncontrolservice.repository.EditOperationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentBuilderServiceTest {

    @Mock
    private EditOperationRepository operationRepository;

    @InjectMocks
    private DocumentBuilderService builderService;

    @Test
    void shouldBuildDocumentFromSnapshotAndOperations() {

        UUID docId = UUID.randomUUID();

        EditOperation op1 = new EditOperation();
        op1.setType(OperationType.INSERT);
        op1.setPosition(0);
        op1.setContent(" World");

        when(operationRepository
                .findByDocumentIdAndSequenceNumberGreaterThanAndSequenceNumberLessThanEqualOrderBySequenceNumberAsc(
                        any(UUID.class), anyLong(), anyLong()
                )).thenReturn(List.of(op1));

        String content = builderService.buildDocument(docId, null, 2L);

        assertEquals(" World", content);
    }

    @Test
    void shouldApplyAllOperationTypesCorrectly() {

        UUID docId = UUID.randomUUID();
        String snapshotContent = "Hello World";

        DocumentSnapshot snapshot = new DocumentSnapshot();
        snapshot.setDocumentId(docId);
        snapshot.setContent(snapshotContent);
        snapshot.setLastOperationSequence(1);
        snapshot.setCreatedAt(LocalDateTime.now());

        EditOperation insert = new EditOperation();
        insert.setType(OperationType.INSERT);
        insert.setPosition(5);
        insert.setContent(",");

        EditOperation delete = new EditOperation();
        delete.setType(OperationType.DELETE);
        delete.setPosition(6);
        delete.setLength(1);

        EditOperation replace = new EditOperation();
        replace.setType(OperationType.REPLACE);
        replace.setPosition(0);
        replace.setLength(5);
        replace.setContent("Hi");

        when(operationRepository
                .findByDocumentIdAndSequenceNumberGreaterThanAndSequenceNumberLessThanEqualOrderBySequenceNumberAsc(
                        any(UUID.class), anyLong(), anyLong()
                )).thenReturn(List.of(insert, delete, replace));

        String result = builderService.buildDocument(docId, snapshot, 3L);

        assertEquals("Hi,World", result);
    }
}