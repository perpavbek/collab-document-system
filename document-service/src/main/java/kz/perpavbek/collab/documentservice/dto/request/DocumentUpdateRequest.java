package kz.perpavbek.collab.documentservice.dto.request;

import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentUpdateRequest {

    @Size(max = 255, message = "Title cannot exceed 255 characters")
    private String title;

    private List<UUID> collaboratorIds;
}
