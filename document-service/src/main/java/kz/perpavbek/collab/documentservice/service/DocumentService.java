package kz.perpavbek.collab.documentservice.service;

import jakarta.transaction.Transactional;
import kz.perpavbek.collab.documentservice.dto.request.DocumentCreateRequest;
import kz.perpavbek.collab.documentservice.dto.request.DocumentUpdateRequest;
import kz.perpavbek.collab.documentservice.dto.response.DocumentResponse;
import kz.perpavbek.collab.documentservice.entity.Document;
import kz.perpavbek.collab.documentservice.entity.DocumentCollaborator;
import kz.perpavbek.collab.documentservice.enums.Role;
import kz.perpavbek.collab.documentservice.mapper.DocumentCollaboratorMapper;
import kz.perpavbek.collab.documentservice.mapper.DocumentMapper;
import kz.perpavbek.collab.documentservice.repository.DocumentCollaboratorRepository;
import kz.perpavbek.collab.documentservice.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final DocumentCollaboratorRepository collaboratorRepository;
    private final DocumentMapper documentMapper;
    private final DocumentCollaboratorMapper documentCollaboratorMapper;

    @Transactional
    public DocumentResponse createDocument(DocumentCreateRequest request) {
        Document document = Document.builder()
                .title(request.getTitle())
                .ownerId(request.getOwnerId())
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
                .orElseThrow(() -> new RuntimeException("Document not found"));
        return documentMapper.toResponse(document);
    }

    @Transactional
    public DocumentResponse updateDocument(UUID documentId, DocumentUpdateRequest request) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found"));

        if (request.getTitle() != null) {
            document.setTitle(request.getTitle());
        }

        if (request.getCollaboratorIds() != null) {
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

    @Transactional
    public void deleteDocument(UUID documentId) {
        if (!documentRepository.existsById(documentId)) {
            throw new RuntimeException("Document not found");
        }
        collaboratorRepository.deleteByDocumentId(documentId);
        documentRepository.deleteById(documentId);
    }
}
