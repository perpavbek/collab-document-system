package kz.perpavbek.collab.documentservice.dto.response;

import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentSessionResponse {

    private UUID id;
    private DocumentCollaboratorResponse collaborator;
    private LocalDateTime connectedAt;
    private LocalDateTime lastActivityAt;

}
