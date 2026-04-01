package kz.perpavbek.collab.documentservice.service;

import jakarta.transaction.Transactional;
import kz.perpavbek.collab.documentservice.client.VersionControlClient;
import kz.perpavbek.collab.documentservice.dto.request.DocumentCreateRequest;
import kz.perpavbek.collab.documentservice.dto.request.DocumentUpdateRequest;
import kz.perpavbek.collab.documentservice.dto.response.DocumentResponse;
import kz.perpavbek.collab.documentservice.entity.Document;
import kz.perpavbek.collab.documentservice.entity.DocumentCollaborator;
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

    @Transactional
    public DocumentResponse createDocument(DocumentCreateRequest request) {

        UUID ownerId = jwtUtils.getIdFromToken(jwtUtils.getCurrentToken());

        userValidationService.validateUser(ownerId);
        userValidationService.validateUsers(request.getCollaboratorIds());

        Document document = Document.builder()
                .title(request.getTitle())
                .ownerId(ownerId)
                .versionSequenceNumber(1L)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        document.setCollaborators(
                request.getCollaboratorIds().stream()
                        .map(id -> createCollaborator(document, id))
                        .toList()
        );

        return documentMapper.toResponse(documentRepository.save(document));
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

            userValidationService.validateUsers(request.getCollaboratorIds());

            document.getCollaborators().clear();

            documentRepository.flush();

            document.getCollaborators().addAll(
                    request.getCollaboratorIds().stream()
                            .map(id -> createCollaborator(document, id))
                            .toList()
            );
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

    private DocumentCollaborator createCollaborator(Document doc, UUID userId) {
        return DocumentCollaborator.builder()
                .document(doc)
                .userId(userId)
                .role(Role.EDITOR)
                .addedAt(LocalDateTime.now())
                .build();
    }
}
