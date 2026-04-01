package kz.perpavbek.collab.documentservice.dto.client;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RollbackRequest {
    private UUID documentId;
    private long targetSequence;
}
