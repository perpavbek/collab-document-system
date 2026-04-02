package kz.perpavbek.collab.documentservice.entity;

import jakarta.persistence.*;
import kz.perpavbek.collab.documentservice.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "document_invitations",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"document_id", "invited_user_id"}),
                @UniqueConstraint(columnNames = {"token"})
        }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentInvitation {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", nullable = false)
    private Document document;

    @Column(name = "invited_user_id", nullable = false)
    private UUID invitedUserId;

    @Column(nullable = false)
    private String invitedEmail;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(nullable = false, unique = true)
    private String token;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
