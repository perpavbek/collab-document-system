package kz.perpavbek.collab.documentservice.dto.websocket;

import kz.perpavbek.collab.documentservice.enums.OperationType;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class DocumentEditMessageResponse {

    private UUID documentId;
    private UUID userId;

    private int position;
    private String content;
    private Integer length;

    private OperationType type;

    private long sequenceNumber;
}
