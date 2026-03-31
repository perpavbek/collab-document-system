package kz.perpavbek.collab.documentservice.dto.client;

import kz.perpavbek.collab.documentservice.enums.OperationType;
import lombok.Data;

import java.util.UUID;

@Data
public class OperationResponse {
    private UUID documentId;
    private UUID userId;
    private int position;
    private Integer length;
    private String content;
    private OperationType type;
    private long sequenceNumber;
}
