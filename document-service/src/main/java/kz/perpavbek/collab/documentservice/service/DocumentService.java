package kz.perpavbek.collab.documentservice.service;

import jakarta.transaction.Transactional;
import kz.perpavbek.collab.documentservice.client.VersionControlClient;
import kz.perpavbek.collab.documentservice.dto.client.User;
import kz.perpavbek.collab.documentservice.dto.request.DocumentCreateRequest;
import kz.perpavbek.collab.documentservice.dto.request.DocumentUpdateRequest;
import kz.perpavbek.collab.documentservice.dto.response.DocumentResponse;
import kz.perpavbek.collab.documentservice.entity.Document;
import kz.perpavbek.collab.documentservice.entity.DocumentInvitation;
import kz.perpavbek.collab.documentservice.enums.Role;
import kz.perpavbek.collab.documentservice.exception.NotFoundException;
import kz.perpavbek.collab.documentservice.mapper.DocumentMapper;
import kz.perpavbek.collab.documentservice.repository.DocumentRepository;
import kz.perpavbek.collab.documentservice.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final DocumentMapper documentMapper;
    private final DocumentAccessService documentAccessService;
    private final UserValidationService userValidationService;
    private final JwtUtils jwtUtils;
    private final VersionControlClient versionControlClient;
    private final DocumentInvitationService documentInvitationService;

    @Transactional
    public DocumentResponse createDocument(DocumentCreateRequest request) {

        User currentUser = userValidationService.getCurrentUser();
        UUID ownerId = currentUser.getId();

        Document document = Document.builder()
                .title(request.getTitle())
                .ownerId(ownerId)
                .collaborators(new ArrayList<>())
                .pendingInvitations(new ArrayList<>())
                .versionSequenceNumber(1L)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        List<DocumentInvitation> newInvitations = documentInvitationService.synchronizeInvitations(
                document,
                request.getCollaboratorIds()
        );

        Document savedDocument = documentRepository.save(document);
        documentInvitationService.sendInvitationEmails(savedDocument, newInvitations, currentUser);

        return documentMapper.toResponse(savedDocument);
    }

    @Transactional
    public DocumentResponse getDocument(UUID documentId) {

        Document document = getDocumentOrThrow(documentId);

        documentAccessService.checkPermission(document, Role.EDITOR, Role.VIEWER);

        return documentMapper.toResponse(document);
    }

    @Transactional
    public DocumentResponse updateDocumentMeta(UUID documentId, DocumentUpdateRequest request) {

        Document document = getDocumentOrThrow(documentId);

        documentAccessService.checkPermission(document, Role.EDITOR);

        if (request.getTitle() != null) {
            document.setTitle(request.getTitle());
        }

        if (request.getCollaboratorIds() != null) {

            User currentUser = userValidationService.getCurrentUser();
            List<DocumentInvitation> newInvitations = documentInvitationService.synchronizeInvitations(
                    document,
                    request.getCollaboratorIds()
            );

            document.setUpdatedAt(LocalDateTime.now());
            Document savedDocument = documentRepository.save(document);
            documentInvitationService.sendInvitationEmails(savedDocument, newInvitations, currentUser);
            return documentMapper.toResponse(savedDocument);
        }

        document.setUpdatedAt(LocalDateTime.now());

        return documentMapper.toResponse(documentRepository.save(document));
    }

    @Transactional
    public void deleteDocument(UUID documentId) {

        Document document = getDocumentOrThrow(documentId);

        documentAccessService.checkPermission(document,  Role.OWNER);

        versionControlClient.deleteDocumentVersions(documentId);

        documentRepository.delete(document);
    }

    public Document getDocumentOrThrow(UUID id) {
        return documentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Document not found"));
    }

    @Transactional
    public Page<DocumentResponse> getDocumentsForCurrentUser(int page, int size) {

        UUID userId = jwtUtils.getIdFromToken(jwtUtils.getCurrentToken());

        Pageable pageable = PageRequest.of(page, size);

        return documentRepository
                .findByOwnerIdOrCollaborators_UserId(userId, userId, pageable)
                .map(documentMapper::toResponse);
    }
}
