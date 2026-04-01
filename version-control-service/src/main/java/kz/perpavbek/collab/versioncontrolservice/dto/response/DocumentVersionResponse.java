package kz.perpavbek.collab.versioncontrolservice.dto.response;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentVersionResponse {
    private String content;
    private long sequenceNumber;
}
