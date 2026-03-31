package kz.perpavbek.collab.versioncontrolservice.controller;

import jakarta.validation.Valid;
import kz.perpavbek.collab.versioncontrolservice.dto.request.EditOperationRequest;
import kz.perpavbek.collab.versioncontrolservice.dto.response.EditOperationResponse;
import kz.perpavbek.collab.versioncontrolservice.service.VersionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/internal/documents")
@RequiredArgsConstructor
public class InternalController {
    private final VersionService versionService;

    @PostMapping("/operations")
    public ResponseEntity<EditOperationResponse> saveOperation(
            @Valid @RequestBody EditOperationRequest request) {

        return ResponseEntity.ok(
                versionService.saveOperation(request)
        );
    }

    @DeleteMapping("/{documentId}/history")
    public ResponseEntity<Void> deleteDocumentHistory(
            @PathVariable UUID documentId) {

        versionService.deleteDocumentHistory(documentId);

        return ResponseEntity.noContent().build();
    }
}
