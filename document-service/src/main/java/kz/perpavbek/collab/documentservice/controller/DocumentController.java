package kz.perpavbek.collab.documentservice.controller;

import jakarta.validation.Valid;
import kz.perpavbek.collab.documentservice.dto.client.OperationRequest;
import kz.perpavbek.collab.documentservice.dto.client.OperationResponse;
import kz.perpavbek.collab.documentservice.dto.request.DocumentCreateRequest;
import kz.perpavbek.collab.documentservice.dto.request.DocumentRollbackRequest;
import kz.perpavbek.collab.documentservice.dto.request.DocumentUpdateRequest;
import kz.perpavbek.collab.documentservice.dto.response.DocumentResponse;
import kz.perpavbek.collab.documentservice.dto.response.PageResponse;
import kz.perpavbek.collab.documentservice.dto.response.PermissionResponse;
import kz.perpavbek.collab.documentservice.enums.Role;
import kz.perpavbek.collab.documentservice.security.JwtUtils;
import kz.perpavbek.collab.documentservice.service.DocumentAccessService;
import kz.perpavbek.collab.documentservice.service.DocumentOperationService;
import kz.perpavbek.collab.documentservice.service.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;
    private final DocumentOperationService documentOperationService;
    private final DocumentAccessService documentAccessService;
    private final JwtUtils jwtUtils;

    @PostMapping
    public ResponseEntity<DocumentResponse> createDocument(
            @Valid @RequestBody DocumentCreateRequest request) {

        return ResponseEntity.ok(documentService.createDocument(request));
    }

    @GetMapping
    public ResponseEntity<PageResponse<DocumentResponse>> getUserDocuments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(
                new PageResponse<>(documentService.getDocumentsForCurrentUser(page, size))
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<DocumentResponse> getDocument(@PathVariable UUID id) {

        return ResponseEntity.ok(documentService.getDocument(id));
    }

    @PostMapping("/{id}/rollback")
    public ResponseEntity<Void> rollbackDocument(
            @PathVariable UUID id,
            @RequestBody DocumentRollbackRequest request) {

        documentOperationService.rollbackDocument(
                id,
                request.getTargetSequence()
        );

        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/permission")
    public ResponseEntity<PermissionResponse> getUserPermission(@PathVariable UUID id) {

        UUID userId = jwtUtils.getIdFromToken(jwtUtils.getCurrentToken());

        Role role = documentAccessService.getUserRole(id, userId);

        return ResponseEntity.ok(new PermissionResponse(role));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DocumentResponse> updateDocument(
            @PathVariable UUID id,
            @Valid @RequestBody DocumentUpdateRequest request) {

        return ResponseEntity.ok(documentService.updateDocumentMeta(id, request));
    }

    @PutMapping("/edit")
    public ResponseEntity<OperationResponse> editDocument(
            @RequestBody OperationRequest request) {

        return ResponseEntity.ok(documentOperationService.applyEdit(request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDocument(@PathVariable UUID id) {

        documentService.deleteDocument(id);

        return ResponseEntity.noContent().build();
    }
}