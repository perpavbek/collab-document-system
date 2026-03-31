package kz.perpavbek.collab.versioncontrolservice.service;

import jakarta.transaction.Transactional;
import kz.perpavbek.collab.versioncontrolservice.dto.request.EditOperationRequest;
import kz.perpavbek.collab.versioncontrolservice.dto.response.EditOperationResponse;
import kz.perpavbek.collab.versioncontrolservice.enums.CollaboratorRole;
import kz.perpavbek.collab.versioncontrolservice.mapper.EditOperationMapper;
import kz.perpavbek.collab.versioncontrolservice.repository.EditOperationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
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
    private final SnapshotService snapshotService;

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
    public void deleteDocumentHistory(UUID documentId) {

        accessService.checkAccess(
                documentId,
                CollaboratorRole.OWNER
        );

        operationService.deleteOperations(documentId);

        snapshotService.deleteSnapshots(documentId);
    }
}