package kz.perpavbek.collab.versioncontrolservice.service;

import jakarta.transaction.Transactional;
import kz.perpavbek.collab.versioncontrolservice.dto.request.EditOperationRequest;
import kz.perpavbek.collab.versioncontrolservice.dto.response.DocumentVersionResponse;
import kz.perpavbek.collab.versioncontrolservice.dto.response.EditOperationResponse;
import kz.perpavbek.collab.versioncontrolservice.entity.DocumentSnapshot;
import kz.perpavbek.collab.versioncontrolservice.entity.EditOperation;
import kz.perpavbek.collab.versioncontrolservice.enums.CollaboratorRole;
import kz.perpavbek.collab.versioncontrolservice.mapper.EditOperationMapper;
import kz.perpavbek.collab.versioncontrolservice.repository.EditOperationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VersionService {

    private final AccessControlService accessService;
    private final DocumentContentService contentService;
    private final EditOperationService operationService;
    private final EditOperationRepository operationRepository;
    private final EditOperationMapper mapper;
    private final SnapshotService snapshotService;;

    public String getFullDocument(UUID documentId) {

        accessService.checkAccess(
                documentId,
                CollaboratorRole.OWNER,
                CollaboratorRole.VIEWER,
                CollaboratorRole.EDITOR
        );

        return contentService.buildDocument(documentId);
    }

    public EditOperationResponse saveOperation(EditOperationRequest request) {

        accessService.checkAccess(
                request.getDocumentId(),
                CollaboratorRole.OWNER,
                CollaboratorRole.EDITOR
        );

        return mapper.toResponse(operationService.saveOperation(request));
    }

    public List<EditOperationResponse> getOperationsAfter(UUID documentId, long seq) {

        accessService.checkAccess(
                documentId,
                CollaboratorRole.OWNER,
                CollaboratorRole.VIEWER,
                CollaboratorRole.EDITOR
        );

        return mapper.toResponseList(
                operationRepository
                        .findByDocumentIdAndSequenceNumberGreaterThanOrderBySequenceNumberAsc(
                                documentId,
                                seq
                        )
        );
    }

    @Transactional
    public void rollbackDocument(UUID documentId, long targetSequence) {
        accessService.checkAccess(documentId, CollaboratorRole.OWNER);

        long lastSeq = operationRepository
                .findTopByDocumentIdOrderBySequenceNumberDesc(documentId)
                .map(EditOperation::getSequenceNumber)
                .orElse(0L);

        if (targetSequence > lastSeq) {
            throw new IllegalArgumentException("Target version does not exist");
        }

        String content = contentService.buildDocumentBySequenceNumber(documentId, targetSequence);

        operationRepository.deleteByDocumentIdAndSequenceNumberGreaterThan(documentId, targetSequence);

        snapshotService.createSnapshot(documentId, content, targetSequence);
    }

    @Transactional
    public DocumentVersionResponse getDocumentAtVersion(UUID documentId, long sequenceNumber) {
        accessService.checkAccess(documentId, CollaboratorRole.OWNER, CollaboratorRole.VIEWER, CollaboratorRole.EDITOR);

        String content = contentService.buildDocumentBySequenceNumber(documentId, sequenceNumber);

        return DocumentVersionResponse.builder()
                .content(content)
                .sequenceNumber(sequenceNumber)
                .build();
    }

    @Transactional
    public void deleteDocumentHistory(UUID documentId) {

        accessService.checkAccess(
                documentId,
                CollaboratorRole.OWNER
        );

        operationService.deleteOperations(documentId);

        snapshotService.deleteSnapshots(documentId);
    }
}