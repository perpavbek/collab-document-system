package kz.perpavbek.collab.documentservice.service;

import feign.FeignException;
import jakarta.transaction.Transactional;
import kz.perpavbek.collab.documentservice.client.UserServiceClient;
import kz.perpavbek.collab.documentservice.client.VersionControlClient;
import kz.perpavbek.collab.documentservice.dto.client.OperationRequest;
import kz.perpavbek.collab.documentservice.dto.client.OperationResponse;
import kz.perpavbek.collab.documentservice.dto.client.User;
import kz.perpavbek.collab.documentservice.dto.request.DocumentCreateRequest;
import kz.perpavbek.collab.documentservice.dto.request.DocumentEditRequest;
import kz.perpavbek.collab.documentservice.dto.request.DocumentUpdateRequest;
import kz.perpavbek.collab.documentservice.dto.response.DocumentResponse;
import kz.perpavbek.collab.documentservice.entity.Document;
import kz.perpavbek.collab.documentservice.entity.DocumentCollaborator;
import kz.perpavbek.collab.documentservice.enums.Role;
import kz.perpavbek.collab.documentservice.exception.AccessDeniedException;
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
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final DocumentPermissionService documentPermissionService;
    private final DocumentMapper documentMapper;
    private final UserServiceClient userServiceClient;
    private final VersionControlClient versionControlClient;
    private final JwtUtils jwtUtils;
    private final DocumentEventService documentEventService;


    @Transactional
    public DocumentResponse createDocument(DocumentCreateRequest request) {
        UUID ownerId = jwtUtils.getIdFromToken(jwtUtils.getCurrentToken());

        getUserById(ownerId);
        validateUsers(request.getCollaboratorIds());

        Document document = Document.builder()
                .title(request.getTitle())
                .ownerId(ownerId)
                .versionSequenceNumber(1L)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        for (UUID userId : request.getCollaboratorIds()) {
            DocumentCollaborator collaborator = DocumentCollaborator.builder()
                    .document(document)
                    .userId(userId)
                    .role(kz.perpavbek.collab.documentservice.enums.Role.EDITOR)
                    .addedAt(LocalDateTime.now())
                    .build();
            document.getCollaborators().add(collaborator);
        }

        Document savedDocument = documentRepository.save(document);

        return documentMapper.toResponse(savedDocument);
    }

    @Transactional
    public DocumentResponse getDocument(UUID documentId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new NotFoundException("Document not found"));
        documentPermissionService.checkPermission(document, Role.EDITOR, Role.VIEWER);
        return documentMapper.toResponse(document);
    }

    @Transactional
    public DocumentResponse updateDocumentMeta(UUID documentId, DocumentUpdateRequest request) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new NotFoundException("Document not found"));

        documentPermissionService.checkPermission(document, Role.EDITOR);

        if (request.getTitle() != null) {
            document.setTitle(request.getTitle());
        }

        if (request.getCollaboratorIds() != null) {
            validateUsers(request.getCollaboratorIds());
            document.getCollaborators().clear();

            List<DocumentCollaborator> newCollaborators = request.getCollaboratorIds().stream()
                    .map(userId -> DocumentCollaborator.builder()
                            .document(document)
                            .userId(userId)
                            .role(Role.EDITOR)
                            .addedAt(LocalDateTime.now())
                            .build())
                    .toList();

            document.getCollaborators().addAll(newCollaborators);
        }

        document.setUpdatedAt(LocalDateTime.now());
        documentRepository.save(document);

        return documentMapper.toResponse(document);
    }

    public OperationResponse applyEdit(OperationRequest request) {
        UUID userId = jwtUtils.getIdFromToken(jwtUtils.getCurrentToken());

        Role role = getUserRoleInDocument(request.getDocumentId(), userId);

        if (!(role == Role.OWNER || role == Role.EDITOR)) {
            throw new AccessDeniedException("User cannot edit this document");
        }

        OperationResponse response = versionControlClient.saveOperation(request);

        updateDocumentSequenceNumber(request.getDocumentId(), response.getSequenceNumber());

        documentEventService.sendDocumentUpdate(response);

        return response;
    }

    @Transactional
    public void deleteDocument(UUID documentId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new NotFoundException("Document not found"));

        documentPermissionService.checkPermission(document);

        documentRepository.delete(document);
    }

    private User getUserById(UUID userId){
        try{
            return userServiceClient.getUserById(userId);
        } catch (FeignException.NotFound e) {
            throw new NotFoundException("User does not exist");
        }
    }

    private void validateUsers(List<UUID> userIds){
        List<UUID> missing = userIds.stream()
                .filter(id -> {
                    try { getUserById(id); return false; }
                    catch (NotFoundException e) { return true; }
                })
                .toList();

        if (!missing.isEmpty()) {
            throw new IllegalArgumentException("Users not found: " + missing);
        }
    }

    public Role getCurrentUserRoleInDocument(UUID documentId) {
        UUID userId = jwtUtils.getIdFromToken(jwtUtils.getCurrentToken());
        return getUserRoleInDocument(documentId, userId);
    }

    @Transactional
    public Role getUserRoleInDocument(UUID documentId, UUID userId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new NotFoundException("Document not found"));

        if (document.getOwnerId().equals(userId)) {
            return Role.OWNER;
        }

        return document.getCollaborators().stream()
                .filter(c -> c.getUserId().equals(userId))
                .map(DocumentCollaborator::getRole)
                .findFirst()
                .orElse(null);
    }

    @Transactional
    public void updateDocumentSequenceNumber(UUID documentId, long sequenceNumber) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new NotFoundException("Document not found"));
        document.setVersionSequenceNumber(sequenceNumber);
        documentRepository.save(document);
    }

    @Transactional
    public Page<Document> getDocumentsForUser(UUID userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        return documentRepository.findByOwnerIdOrCollaborators_UserId(userId, userId, pageable);
    }

    public Page<DocumentResponse> getDocumentsForCurrentUser(int page, int size) {
        return getDocumentsForUser(jwtUtils.getIdFromToken(jwtUtils.getCurrentToken()), page, size)
                .map(documentMapper::toResponse);
    }
}
