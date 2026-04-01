package kz.perpavbek.collab.versioncontrolservice.dto.request;

import lombok.*;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RollbackRequest {
    private UUID documentId;
    private long targetSequence;
}
