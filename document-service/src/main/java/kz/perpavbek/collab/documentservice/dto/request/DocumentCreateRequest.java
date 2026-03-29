package kz.perpavbek.collab.documentservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentCreateRequest {
    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title cannot exceed 255 characters")
    private String title;

    @NotEmpty(message = "OwnerId is required")
    private UUID ownerId;

    @NotNull(message = "Collaborators list cannot be null")
    private List<UUID> collaboratorIds;
}