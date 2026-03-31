package kz.perpavbek.collab.versioncontrolservice.service;

import jakarta.transaction.Transactional;
import kz.perpavbek.collab.versioncontrolservice.dto.request.EditOperationRequest;
import kz.perpavbek.collab.versioncontrolservice.entity.EditOperation;
import kz.perpavbek.collab.versioncontrolservice.enums.OperationType;
import kz.perpavbek.collab.versioncontrolservice.mapper.EditOperationMapper;
import kz.perpavbek.collab.versioncontrolservice.repository.EditOperationRepository;
import kz.perpavbek.collab.versioncontrolservice.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EditOperationService {

    private final EditOperationRepository operationRepository;
    private final EditOperationMapper mapper;
    private final DocumentContentService contentService;
    private final SnapshotService snapshotService;
    private final JwtUtils jwtUtils;

    @Transactional
    public EditOperation saveOperation(EditOperationRequest request) {

        UUID userId = jwtUtils.getIdFromToken(jwtUtils.getCurrentToken());

        int documentLength = contentService.getDocumentLength(request.getDocumentId());

        validateOperation(request, documentLength);

        long nextSeq = operationRepository
                .findTopByDocumentIdOrderBySequenceNumberDesc(request.getDocumentId())
                .map(op -> op.getSequenceNumber() + 1)
                .orElse(1L);

        EditOperation operation = mapper.toEntity(request);

        operation.setUserId(userId);
        operation.setSequenceNumber(nextSeq);

        EditOperation saved = operationRepository.save(operation);

        snapshotService.createSnapshotIfNeeded(request.getDocumentId());

        return saved;
    }

    private void validateOperation(EditOperationRequest request, int documentLength) {

        if (request.getPosition() > documentLength) {
            throw new IllegalArgumentException("Position exceeds document length");
        }

        if (request.getType() != OperationType.INSERT &&
                request.getPosition() + request.getLength() > documentLength) {
            throw new IllegalArgumentException("Operation exceeds document bounds");
        }
    }
    @Transactional
    public void deleteOperations(UUID documentId) {
        operationRepository.deleteByDocumentId(documentId);
    }
}
