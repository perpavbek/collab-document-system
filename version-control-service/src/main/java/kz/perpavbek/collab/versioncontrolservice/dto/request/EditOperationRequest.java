package kz.perpavbek.collab.versioncontrolservice.dto.request;

import jakarta.validation.constraints.Min;
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
public class EditOperationRequest {

    @NotBlank(message = "documentId is required")
    private UUID documentId;

    @NotBlank(message = "userId is required")
    private UUID userId;

    @Min(value = 0, message = "position must be >= 0")
    private int position;

    private String content;

    private Integer length;

    @NotBlank(message = "type is required")
    private OperationType type;
}
