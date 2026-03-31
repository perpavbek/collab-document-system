package kz.perpavbek.collab.documentservice.dto.client;

import kz.perpavbek.collab.documentservice.enums.OperationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OperationRequest {
    private UUID documentId;

    private int position;

    private String content;

    private Integer length;

    private OperationType type;
}
