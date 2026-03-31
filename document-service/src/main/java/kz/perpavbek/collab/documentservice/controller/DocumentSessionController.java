package kz.perpavbek.collab.documentservice.controller;

import kz.perpavbek.collab.documentservice.dto.response.DocumentSessionResponse;
import kz.perpavbek.collab.documentservice.service.DocumentSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class DocumentSessionController {

    private final DocumentSessionService service;

    @GetMapping("/{documentId}/sessions")
    public ResponseEntity<List<DocumentSessionResponse>> getSessions(
            @PathVariable UUID documentId) {

        return ResponseEntity.ok(service.getActiveSessions(documentId));
    }
}
