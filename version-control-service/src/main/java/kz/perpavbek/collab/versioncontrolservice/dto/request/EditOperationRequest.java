package kz.perpavbek.collab.versioncontrolservice.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import kz.perpavbek.collab.versioncontrolservice.enums.OperationType;
import kz.perpavbek.collab.versioncontrolservice.validation.ValidEditOperation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ValidEditOperation
public class EditOperationRequest {

    @NotNull(message = "documentId is required")
    private UUID documentId;

    @NotNull(message = "userId is required")
    private UUID userId;

    @Min(value = 0, message = "position must be >= 0")
    private int position;

    private String content;

    private Integer length;

    @NotNull(message = "type is required")
    private OperationType type;
}
