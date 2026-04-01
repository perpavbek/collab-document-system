package kz.perpavbek.collab.documentservice.service;

import kz.perpavbek.collab.documentservice.dto.client.OperationResponse;
import kz.perpavbek.collab.documentservice.enums.OperationType;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

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

    public void sendRollback(UUID documentId, long sequenceNumber) {

        OperationResponse response = new OperationResponse();
        response.setDocumentId(documentId);
        response.setType(OperationType.ROLLBACK);
        response.setSequenceNumber(sequenceNumber);

        messagingTemplate.convertAndSend(
                "/topic/document/" + documentId,
                response
        );
    }
}