package kz.perpavbek.collab.versioncontrolservice.service;

import jakarta.transaction.Transactional;
import kz.perpavbek.collab.versioncontrolservice.entity.DocumentSnapshot;
import kz.perpavbek.collab.versioncontrolservice.entity.EditOperation;
import kz.perpavbek.collab.versioncontrolservice.repository.EditOperationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DocumentContentService {

    private final EditOperationRepository operationRepository;
    private final SnapshotService snapshotService;
    private final DocumentBuilderService documentBuilderService;


    public String buildDocument(UUID documentId) {

        Long lastSeq = operationRepository
                .findTopByDocumentIdOrderBySequenceNumberDesc(documentId)
                .map(EditOperation::getSequenceNumber)
                .orElse(0L);

        return buildDocumentBySequenceNumber(documentId, lastSeq);
    }

    @Transactional
    public String buildDocumentBySequenceNumber(UUID documentId, long sequenceNumber) {
        DocumentSnapshot snapshot = snapshotService.findSnapshotBeforeOrAt(documentId, sequenceNumber);

        return documentBuilderService.buildDocument(documentId, snapshot, sequenceNumber);
    }

    public int getDocumentLength(UUID documentId) {
        return buildDocument(documentId).length();
    }
}
