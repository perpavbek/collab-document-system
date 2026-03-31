package kz.perpavbek.collab.documentservice.service;

import kz.perpavbek.collab.documentservice.dto.client.OperationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DocumentEventService {

    private final SimpMessagingTemplate messagingTemplate;

    public void sendDocumentUpdate(OperationResponse update) {
        messagingTemplate.convertAndSend(
                "/topic/document/" + update.getDocumentId(),
                update
        );
    }
}