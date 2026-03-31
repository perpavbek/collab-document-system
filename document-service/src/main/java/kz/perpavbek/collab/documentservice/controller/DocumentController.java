package kz.perpavbek.collab.documentservice.controller;

import jakarta.validation.Valid;
import kz.perpavbek.collab.documentservice.dto.client.OperationRequest;
import kz.perpavbek.collab.documentservice.dto.client.OperationResponse;
import kz.perpavbek.collab.documentservice.dto.request.DocumentCreateRequest;
import kz.perpavbek.collab.documentservice.dto.request.DocumentEditRequest;
import kz.perpavbek.collab.documentservice.dto.request.DocumentUpdateRequest;
import kz.perpavbek.collab.documentservice.dto.response.DocumentResponse;
import kz.perpavbek.collab.documentservice.dto.response.PageResponse;
import kz.perpavbek.collab.documentservice.dto.response.PermissionResponse;
import kz.perpavbek.collab.documentservice.service.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    @PostMapping
    public ResponseEntity<DocumentResponse> createDocument(
            @Valid @RequestBody DocumentCreateRequest request) {
        DocumentResponse response = documentService.createDocument(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping ResponseEntity<PageResponse<DocumentResponse>> getUserDocuments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size){
        return ResponseEntity.ok(new PageResponse<>(documentService.getDocumentsForCurrentUser(page, size)));
    }

    @GetMapping("/id/{id}")
    public ResponseEntity<DocumentResponse> getDocument(@PathVariable UUID id) {
        DocumentResponse response = documentService.getDocument(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/id/{id}/permission")
    public ResponseEntity<PermissionResponse> getUserPermission(@PathVariable UUID id) {
        return ResponseEntity.ok(new PermissionResponse(documentService.getCurrentUserRoleInDocument(id)));
    }

    @PutMapping("/id/{id}")
    public ResponseEntity<DocumentResponse> updateDocument(
            @PathVariable UUID id,
            @Valid @RequestBody DocumentUpdateRequest request) {
        DocumentResponse response = documentService.updateDocumentMeta(id, request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/edit")
    public ResponseEntity<OperationResponse> editDocument(@RequestBody OperationRequest request) {
        return ResponseEntity.ok(documentService.applyEdit(request));
    }

    @DeleteMapping("/id/{id}")
    public ResponseEntity<Void> deleteDocument(@PathVariable UUID id) {
        documentService.deleteDocument(id);
        return ResponseEntity.noContent().build();
    }
}
