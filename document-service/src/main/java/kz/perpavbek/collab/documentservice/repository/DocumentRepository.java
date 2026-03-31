package kz.perpavbek.collab.documentservice.repository;

import kz.perpavbek.collab.documentservice.entity.Document;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DocumentRepository extends JpaRepository<Document, UUID> {

    List<Document> findByOwnerId(UUID ownerId);

    boolean existsById(UUID documentId);

    Page<Document> findByOwnerIdOrCollaborators_UserId(UUID ownerId, UUID collaboratorId, Pageable pageable);
}