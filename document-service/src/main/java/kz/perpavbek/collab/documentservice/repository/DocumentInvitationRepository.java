package kz.perpavbek.collab.documentservice.repository;

import kz.perpavbek.collab.documentservice.entity.DocumentInvitation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface DocumentInvitationRepository extends JpaRepository<DocumentInvitation, UUID> {

    Optional<DocumentInvitation> findByToken(String token);
}
