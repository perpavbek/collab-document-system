package kz.perpavbek.collab.versioncontrolservice.service;

import jakarta.transaction.Transactional;
import kz.perpavbek.collab.versioncontrolservice.dto.request.DocumentEditMessageRequest;
import kz.perpavbek.collab.versioncontrolservice.dto.response.EditOperationResponse;
import kz.perpavbek.collab.versioncontrolservice.entity.DocumentSnapshot;
import kz.perpavbek.collab.versioncontrolservice.entity.EditOperation;
import kz.perpavbek.collab.versioncontrolservice.enums.OperationType;
import kz.perpavbek.collab.versioncontrolservice.mapper.EditOperationMapper;
import kz.perpavbek.collab.versioncontrolservice.repository.DocumentSnapshotRepository;
import kz.perpavbek.collab.versioncontrolservice.repository.EditOperationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VersionService {

    private static final long SNAPSHOT_THRESHOLD = 50;

    private final DocumentSnapshotRepository documentSnapshotRepository;
    private final EditOperationRepository editOperationRepository;
    private final EditOperationMapper editOperationMapper;

    public String getFullDocument(UUID documentId) {

        DocumentSnapshot snapshot = documentSnapshotRepository
                .findTopByDocumentIdOrderByLastOperationSequenceDesc(documentId)
                .orElse(null);

        String content = snapshot != null ? snapshot.getContent() : "";
        long lastSeq = snapshot != null ? snapshot.getLastOperationSequence() : 0;

        List<EditOperation> operations =
                editOperationRepository
                        .findByDocumentIdAndSequenceNumberGreaterThanOrderBySequenceNumberAsc(
                                documentId,
                                lastSeq
                        );

        StringBuilder builder = new StringBuilder(content);

        for (EditOperation op : operations) {

            if (op.getType() == OperationType.INSERT) {
                builder.insert(op.getPosition(), op.getContent());
            }

            if (op.getType() == OperationType.DELETE) {
                builder.delete(op.getPosition(), op.getPosition() + op.getContent().length());
            }
        }

        return builder.toString();
    }

    @Transactional
    public void saveOperation(DocumentEditMessageRequest message) {

        long nextSeq = editOperationRepository
                .findTopByDocumentIdOrderBySequenceNumberDesc(message.getDocumentId())
                .map(op -> op.getSequenceNumber() + 1)
                .orElse(1L);

        EditOperation operation = new EditOperation();
        operation.setDocumentId(message.getDocumentId());
        operation.setUserId(message.getUserId());
        operation.setPosition(message.getPosition());
        operation.setContent(message.getText());
        operation.setType(message.getType());
        operation.setSequenceNumber(nextSeq);

        editOperationRepository.save(operation);

        createSnapshotByThreshold(message.getDocumentId());
    }

    public List<EditOperationResponse> getOperationsAfter(UUID documentId, long sequenceNumber) {
        List<EditOperation> operations =
                editOperationRepository
                        .findByDocumentIdAndSequenceNumberGreaterThanOrderBySequenceNumberAsc(
                                documentId,
                                sequenceNumber
                        );
        return editOperationMapper.toResponseList(operations);
    }

    @Transactional
    public void createSnapshot(UUID documentId) {

        String content = getFullDocument(documentId);

        long lastSeq = editOperationRepository
                .findTopByDocumentIdOrderBySequenceNumberDesc(documentId)
                .map(EditOperation::getSequenceNumber)
                .orElse(0L);

        DocumentSnapshot snapshot = new DocumentSnapshot();
        snapshot.setDocumentId(documentId);
        snapshot.setContent(content);
        snapshot.setLastOperationSequence(lastSeq);

        documentSnapshotRepository.save(snapshot);
    }

    private void createSnapshotByThreshold(UUID documentId) {

        long lastSnapshotSeq = documentSnapshotRepository
                .findTopByDocumentIdOrderByLastOperationSequenceDesc(documentId)
                .map(DocumentSnapshot::getLastOperationSequence)
                .orElse(0L);

        long latestOperationSeq = editOperationRepository
                .findTopByDocumentIdOrderBySequenceNumberDesc(documentId)
                .map(EditOperation::getSequenceNumber)
                .orElse(0L);

        if (latestOperationSeq - lastSnapshotSeq >= SNAPSHOT_THRESHOLD) {
            createSnapshot(documentId);
        }
    }
}
