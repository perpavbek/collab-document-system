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
@RequiredArgsConstructor
public class VersionController {

    private final VersionService versionService;

    @GetMapping("/document/id/{documentId}/full")
    public ResponseEntity<String> getFullDocument(@PathVariable UUID documentId) {
        return ResponseEntity.ok(versionService.getFullDocument(documentId));
    }

    @GetMapping("/document/id/{documentId}/operations")
    public ResponseEntity<List<EditOperationResponse>> getOperations(
            @PathVariable UUID documentId,
            @RequestParam(defaultValue = "0") long after
    ) {
        return ResponseEntity.ok(versionService.getOperationsAfter(documentId, after));
    }

    @PostMapping("/operations")
    public ResponseEntity saveOperation(@Valid @RequestBody EditOperationRequest request) {
        versionService.saveOperation(request);
        return ResponseEntity.ok().build();
    }
}
