package kz.perpavbek.collab.documentservice.repository;

import kz.perpavbek.collab.documentservice.entity.DocumentCollaborator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DocumentCollaboratorRepository extends JpaRepository<DocumentCollaborator, UUID> {

    List<DocumentCollaborator> findByDocumentId(UUID documentId);

    boolean existsByDocumentIdAndUserId(UUID documentId, UUID userId);

    Optional<DocumentCollaborator> findByDocumentIdAndUserId(UUID documentId, UUID userId);

    void deleteByDocumentId(UUID documentId);
}