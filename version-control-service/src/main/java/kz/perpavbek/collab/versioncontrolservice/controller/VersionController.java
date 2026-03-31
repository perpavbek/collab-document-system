package kz.perpavbek.collab.versioncontrolservice.controller;

import jakarta.validation.Valid;
import kz.perpavbek.collab.versioncontrolservice.dto.request.EditOperationRequest;
import kz.perpavbek.collab.versioncontrolservice.dto.response.EditOperationResponse;
import kz.perpavbek.collab.versioncontrolservice.service.VersionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/documents")
@RequiredArgsConstructor
public class VersionController {

    private final VersionService versionService;

    @GetMapping("/{documentId}/content")
    public ResponseEntity<String> getFullDocument(@PathVariable UUID documentId) {

        return ResponseEntity.ok(
                versionService.getFullDocument(documentId)
        );
    }

    @GetMapping("/{documentId}/operations")
    public ResponseEntity<List<EditOperationResponse>> getOperations(
            @PathVariable UUID documentId,
            @RequestParam(defaultValue = "0") long after
    ) {

        return ResponseEntity.ok(
                versionService.getOperationsAfter(documentId, after)
        );
    }

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