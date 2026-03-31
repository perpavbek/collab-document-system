package kz.perpavbek.collab.documentservice.service;

import jakarta.transaction.Transactional;
import kz.perpavbek.collab.documentservice.dto.response.DocumentSessionResponse;
import kz.perpavbek.collab.documentservice.entity.DocumentSession;
import kz.perpavbek.collab.documentservice.repository.DocumentSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DocumentSessionService {

    private final DocumentSessionRepository repository;

    @Transactional
    public void connect(UUID documentId, UUID userId, String wsSessionId) {

        DocumentSession session = DocumentSession.builder()
                .documentId(documentId)
                .userId(userId)
                .websocketSessionId(wsSessionId)
                .connectedAt(LocalDateTime.now())
                .lastActivityAt(LocalDateTime.now())
                .build();

        repository.save(session);
    }

    @Transactional
    public void disconnect(String wsSessionId) {
        repository.deleteByWebsocketSessionId(wsSessionId);
    }

    @Transactional
    public void updateActivity(UUID documentId, UUID userId) {

        List<DocumentSession> sessions =
                repository.findByDocumentIdAndUserId(documentId, userId);

        LocalDateTime now = LocalDateTime.now();

        for (DocumentSession session : sessions) {
            session.setLastActivityAt(now);
        }

        repository.saveAll(sessions);
    }

    public List<DocumentSessionResponse> getActiveSessions(UUID documentId) {

        return repository.findByDocumentId(documentId)
                .stream()
                .map(session -> DocumentSessionResponse.builder()
                        .userId(session.getUserId())
                        .connectedAt(session.getConnectedAt())
                        .lastActivityAt(session.getLastActivityAt())
                        .build())
                .toList();
    }

    @Scheduled(fixedRate = 60000)
    public void cleanup() {

        LocalDateTime threshold = LocalDateTime.now().minusMinutes(10);

        repository.deleteAll(
                repository.findAll()
                        .stream()
                        .filter(s -> s.getLastActivityAt().isBefore(threshold))
                        .toList()
        );
    }
}
