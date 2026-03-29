package kz.perpavbek.collab.versioncontrolservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import kz.perpavbek.collab.versioncontrolservice.enums.OperationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentEditMessageRequest {

    @NotBlank(message = "documentId is required")
    private UUID documentId;

    @NotBlank(message = "userId is required")
    private UUID userId;

    @NotBlank(message = "position is required")
    private int position;

    @NotBlank(message = "text is required")
    private String text;

    @NotBlank(message = "type is required")
    private OperationType type;
}
