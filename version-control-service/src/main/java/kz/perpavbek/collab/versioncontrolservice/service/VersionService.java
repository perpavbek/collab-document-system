package kz.perpavbek.collab.versioncontrolservice.service;

import jakarta.transaction.Transactional;
import kz.perpavbek.collab.versioncontrolservice.client.DocumentClient;
import kz.perpavbek.collab.versioncontrolservice.dto.client.PermissionResponse;
import kz.perpavbek.collab.versioncontrolservice.dto.request.EditOperationRequest;
import kz.perpavbek.collab.versioncontrolservice.dto.response.EditOperationResponse;
import kz.perpavbek.collab.versioncontrolservice.entity.DocumentSnapshot;
import kz.perpavbek.collab.versioncontrolservice.entity.EditOperation;
import kz.perpavbek.collab.versioncontrolservice.enums.CollaboratorRole;
import kz.perpavbek.collab.versioncontrolservice.enums.OperationType;
import kz.perpavbek.collab.versioncontrolservice.exception.AccessDeniedException;
import kz.perpavbek.collab.versioncontrolservice.mapper.EditOperationMapper;
import kz.perpavbek.collab.versioncontrolservice.repository.DocumentSnapshotRepository;
import kz.perpavbek.collab.versioncontrolservice.repository.EditOperationRepository;
import kz.perpavbek.collab.versioncontrolservice.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VersionService {

    private static final long SNAPSHOT_THRESHOLD = 50;

    private final DocumentSnapshotRepository documentSnapshotRepository;
    private final EditOperationRepository editOperationRepository;
    private final EditOperationMapper editOperationMapper;
    private final DocumentClient documentClient;
    private final JwtUtils jwtUtils;

    public String buildDocument(UUID documentId) {

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

            switch (op.getType()) {

                case INSERT ->
                        builder.insert(op.getPosition(), op.getContent());

                case DELETE ->
                        builder.delete(
                                op.getPosition(),
                                op.getPosition() + op.getLength()
                        );

                case REPLACE ->
                        builder.replace(
                                op.getPosition(),
                                op.getPosition() + op.getLength(),
                                op.getContent()
                        );
            }
        }

        return builder.toString();
    }

    public String getFullDocument(UUID documentId) {
        checkAccess(documentId, CollaboratorRole.OWNER, CollaboratorRole.VIEWER, CollaboratorRole.EDITOR);
        return buildDocument(documentId);
    }

    @Transactional
    public EditOperation save(EditOperationRequest request) {
        UUID userId = jwtUtils.getIdFromToken(jwtUtils.getCurrentToken());

        int documentLength = getDocumentLength(request.getDocumentId());

        if (request.getPosition() > documentLength) {
            throw new IllegalArgumentException("Position exceeds document length");
        }

        if (request.getType() != OperationType.INSERT &&
                request.getPosition() + request.getLength() > documentLength) {
            throw new IllegalArgumentException("Operation exceeds document bounds");
        }

        long nextSeq = editOperationRepository
                .findTopByDocumentIdOrderBySequenceNumberDesc(request.getDocumentId())
                .map(op -> op.getSequenceNumber() + 1)
                .orElse(1L);

        EditOperation operation = editOperationMapper.toEntity(request);
        operation.setUserId(userId);
        operation.setSequenceNumber(nextSeq);

        EditOperation savedEditOperation = editOperationRepository.save(operation);

        createSnapshotByThreshold(request.getDocumentId());

        return savedEditOperation;
    }

    public List<EditOperationResponse> getOperationsAfter(UUID documentId, long sequenceNumber) {
        checkAccess(documentId, CollaboratorRole.OWNER, CollaboratorRole.VIEWER, CollaboratorRole.EDITOR);
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

    public EditOperationResponse saveOperation(EditOperationRequest request) {
        checkAccess(request.getDocumentId(), CollaboratorRole.OWNER, CollaboratorRole.EDITOR);
        return editOperationMapper.toResponse(save(request));
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

    private int getDocumentLength(UUID documentId) {
        return getFullDocument(documentId).length();
    }

    public void checkAccess(UUID documentId, CollaboratorRole... allowedRoles) {
        PermissionResponse permission = documentClient.getPermission(documentId);
        if (permission.getRole() == null || Arrays.stream(allowedRoles)
                .noneMatch(r -> r.equals(permission.getRole()))) {
            throw new AccessDeniedException("User has no access to this document");
        }
    }
}
