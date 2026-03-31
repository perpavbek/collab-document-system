package kz.perpavbek.collab.documentservice.dto.response;

import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentSessionResponse {

    private UUID documentId;
    private UUID userId;
    private LocalDateTime connectedAt;
    private LocalDateTime lastActivityAt;
}
