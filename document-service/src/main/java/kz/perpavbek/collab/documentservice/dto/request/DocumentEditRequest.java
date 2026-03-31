package kz.perpavbek.collab.documentservice.dto.request;

import kz.perpavbek.collab.documentservice.enums.OperationType;
import lombok.Data;

import java.util.UUID;

@Data
public class DocumentEditRequest {

    private UUID documentId;

    private int position;
    private String content;
    private Integer length;

    private OperationType type;
}
