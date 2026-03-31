package kz.perpavbek.collab.documentservice.service;

import jakarta.transaction.Transactional;
import kz.perpavbek.collab.documentservice.client.VersionControlClient;
import kz.perpavbek.collab.documentservice.dto.client.OperationRequest;
import kz.perpavbek.collab.documentservice.dto.client.OperationResponse;
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
    private final DocumentRepository documentRepository;
    private final DocumentEventService eventService;
    private final JwtUtils jwtUtils;
    private final DocumentSessionService documentSessionService;

    public OperationResponse applyEdit(OperationRequest request) {

        UUID userId = jwtUtils.getIdFromToken(jwtUtils.getCurrentToken());

        Role role = accessService.getUserRole(request.getDocumentId(), userId);

        if (role != Role.OWNER && role != Role.EDITOR) {
            throw new AccessDeniedException("User cannot edit this document");
        }

        OperationResponse response = versionControlClient.saveOperation(request);

        updateSequenceNumber(request.getDocumentId(), response.getSequenceNumber());

        eventService.sendDocumentUpdate(response);

        documentSessionService.updateActivity(request.getDocumentId(), userId);

        return response;
    }

    @Transactional
    public void updateSequenceNumber(UUID documentId, long sequenceNumber) {

        Document document = accessService.getDocumentOrThrow(documentId);

        document.setVersionSequenceNumber(sequenceNumber);

        documentRepository.save(document);
    }
}
