package kz.perpavbek.collab.versioncontrolservice.controller;

import kz.perpavbek.collab.versioncontrolservice.dto.response.DocumentVersionResponse;
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

    @GetMapping("/{documentId}/version/{seq}")
    public ResponseEntity<DocumentVersionResponse> getDocumentAtVersion(
            @PathVariable UUID documentId,
            @PathVariable long seq
    ) {
        return ResponseEntity.ok(versionService.getDocumentAtVersion(documentId, seq));
    }
}