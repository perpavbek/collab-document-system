package kz.perpavbek.collab.documentservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "document_sessions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentSession {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID documentId;

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private String websocketSessionId;

    @Column(nullable = false)
    private LocalDateTime connectedAt;

    @Column(nullable = false)
    private LocalDateTime lastActivityAt;
}