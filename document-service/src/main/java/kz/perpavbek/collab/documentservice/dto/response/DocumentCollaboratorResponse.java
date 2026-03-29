package kz.perpavbek.collab.documentservice.dto.response;

import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;
import kz.perpavbek.collab.documentservice.enums.Role;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentCollaboratorResponse {
    private UUID id;
    private UUID userId;
    private Role role;
    private LocalDateTime addedAt;
}