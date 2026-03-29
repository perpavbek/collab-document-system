package kz.perpavbek.collab.versioncontrolservice.dto.response;

import kz.perpavbek.collab.versioncontrolservice.enums.OperationType;
import lombok.Data;

import java.util.UUID;

@Data
public class EditOperationResponse {

    private UUID id;
    private UUID documentId;
    private UUID userId;
    private int position;
    private String content;
    private OperationType type;
    private long sequenceNumber;
}
