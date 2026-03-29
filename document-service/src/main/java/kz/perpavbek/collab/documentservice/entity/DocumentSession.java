package kz.perpavbek.collab.documentservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "document_sessions",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"collaborator_id"})})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentSession {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "collaborator_id", nullable = false)
    private DocumentCollaborator collaborator;

    @Column(nullable = false)
    private LocalDateTime connectedAt;

    @Column
    private LocalDateTime lastActivityAt;

}