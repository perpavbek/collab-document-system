package kz.perpavbek.collab.versioncontrolservice.service;

import jakarta.transaction.Transactional;
import kz.perpavbek.collab.versioncontrolservice.entity.DocumentSnapshot;
import kz.perpavbek.collab.versioncontrolservice.entity.EditOperation;
import kz.perpavbek.collab.versioncontrolservice.repository.DocumentSnapshotRepository;
import kz.perpavbek.collab.versioncontrolservice.repository.EditOperationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SnapshotService {

    private static final long SNAPSHOT_THRESHOLD = 50;

    private final DocumentSnapshotRepository snapshotRepository;
    private final EditOperationRepository operationRepository;
    private final DocumentBuilderService documentBuilderService;

    @Transactional
    public void createSnapshot(UUID documentId) {

        long lastSeq = operationRepository
                .findTopByDocumentIdOrderBySequenceNumberDesc(documentId)
                .map(EditOperation::getSequenceNumber)
                .orElse(0L);

        DocumentSnapshot prevSnapshot = snapshotRepository.findTopByDocumentIdAndLastOperationSequenceLessThanEqualOrderByLastOperationSequenceDesc(documentId, lastSeq).orElse(null);

        String content = documentBuilderService.buildDocument(
                documentId,
                prevSnapshot,
                lastSeq
        );

        DocumentSnapshot snapshot = new DocumentSnapshot();
        snapshot.setDocumentId(documentId);
        snapshot.setContent(content);
        snapshot.setLastOperationSequence(lastSeq);

        snapshotRepository.save(snapshot);
    }

    @Transactional
    public void createSnapshot(UUID documentId, String content, long lastSeq) {
        DocumentSnapshot snapshot = new DocumentSnapshot();
        snapshot.setDocumentId(documentId);
        snapshot.setContent(content);
        snapshot.setLastOperationSequence(lastSeq);
        snapshotRepository.save(snapshot);
    }

    @Transactional
    public DocumentSnapshot findSnapshotBeforeOrAt(UUID documentId, long sequenceNumber) {
        return snapshotRepository
                .findTopByDocumentIdAndLastOperationSequenceLessThanEqualOrderByLastOperationSequenceDesc(documentId, sequenceNumber)
                .orElse(null);
    }

    public void createSnapshotIfNeeded(UUID documentId) {

        long lastSnapshotSeq = snapshotRepository
                .findTopByDocumentIdOrderByLastOperationSequenceDesc(documentId)
                .map(DocumentSnapshot::getLastOperationSequence)
                .orElse(0L);

        long latestOperationSeq = operationRepository
                .findTopByDocumentIdOrderBySequenceNumberDesc(documentId)
                .map(EditOperation::getSequenceNumber)
                .orElse(0L);

        if (latestOperationSeq - lastSnapshotSeq >= SNAPSHOT_THRESHOLD) {
            createSnapshot(documentId);
        }
    }
    @Transactional
    public void deleteSnapshots(UUID documentId) {
        snapshotRepository.deleteByDocumentId(documentId);
    }
}
