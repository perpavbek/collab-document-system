package kz.perpavbek.collab.documentservice.dto.response;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentResponse {

    private UUID id;
    private String title;
    private UUID ownerId;
    private UUID currentVersionId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private List<DocumentCollaboratorResponse> collaborators;

}
