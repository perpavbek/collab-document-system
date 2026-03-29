package kz.perpavbek.collab.documentservice.repository;

import kz.perpavbek.collab.documentservice.entity.DocumentSession;
import kz.perpavbek.collab.documentservice.entity.DocumentCollaborator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DocumentSessionRepository extends JpaRepository<DocumentSession, UUID> {

    List<DocumentSession> findByCollaborator_DocumentId(UUID documentId);

    boolean existsByCollaborator(DocumentCollaborator collaborator);

    Optional<DocumentSession> findByCollaborator(DocumentCollaborator collaborator);

    void deleteByCollaborator(DocumentCollaborator collaborator);
}
