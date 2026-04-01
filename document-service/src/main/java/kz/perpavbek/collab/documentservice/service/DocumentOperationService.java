package kz.perpavbek.collab.documentservice.service;

import feign.FeignException;
import jakarta.transaction.Transactional;
import kz.perpavbek.collab.documentservice.client.VersionControlClient;
import kz.perpavbek.collab.documentservice.dto.client.OperationRequest;
import kz.perpavbek.collab.documentservice.dto.client.OperationResponse;
import kz.perpavbek.collab.documentservice.dto.client.RollbackRequest;
import kz.perpavbek.collab.documentservice.entity.Document;
import kz.perpavbek.collab.documentservice.enums.Role;
import kz.perpavbek.collab.documentservice.exception.AccessDeniedException;
import kz.perpavbek.collab.documentservice.repository.DocumentRepository;
import kz.perpavbek.collab.documentservice.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DocumentOperationService {

    private final VersionControlClient versionControlClient;
    private final DocumentAccessService accessService;
    private final DocumentService documentService;
    private final DocumentEventService eventService;
    private final JwtUtils jwtUtils;
    private final DocumentSessionService documentSessionService;
    private final DocumentRepository documentRepository;

    public OperationResponse applyEdit(OperationRequest request) {

        UUID userId = jwtUtils.getIdFromToken(jwtUtils.getCurrentToken());

        Role role = accessService.getUserRole(request.getDocumentId(), userId);

        if (role != Role.OWNER && role != Role.EDITOR) {
            throw new AccessDeniedException("User cannot edit this document");
        }

        OperationResponse response;

        try{
            response = versionControlClient.saveOperation(request);
        } catch (FeignException.BadRequest e){
            throw new IllegalArgumentException(e.getMessage());
        }

        updateSequenceNumber(request.getDocumentId(), response.getSequenceNumber());

        eventService.sendDocumentUpdate(response);

        documentSessionService.updateActivity(request.getDocumentId(), userId);

        return response;
    }

    @Transactional
    public void rollbackDocument(UUID documentId, long targetSequence) {
        Document document = documentService.getDocumentOrThrow(documentId);

        accessService.checkPermission(document, Role.OWNER);

        RollbackRequest request = RollbackRequest.builder()
                .documentId(documentId)
                .targetSequence(targetSequence)
                .build();

        try {
            versionControlClient.rollbackOperation(request);
        } catch (FeignException.BadRequest e) {
            throw new IllegalArgumentException(e.getMessage());
        }

        updateSequenceNumber(documentId, targetSequence);

        UUID userId = jwtUtils.getIdFromToken(jwtUtils.getCurrentToken());
        documentSessionService.updateActivity(documentId, userId);
        eventService.sendRollback(documentId, targetSequence);
    }

    @Transactional
    public void updateSequenceNumber(UUID documentId, long sequenceNumber) {

        Document document = documentService.getDocumentOrThrow(documentId);

        document.setVersionSequenceNumber(sequenceNumber);

        documentRepository.save(document);
    }


}
