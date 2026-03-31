package kz.perpavbek.collab.versioncontrolservice.repository;

import kz.perpavbek.collab.versioncontrolservice.entity.DocumentSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface DocumentSnapshotRepository extends JpaRepository<DocumentSnapshot, UUID> {
    void deleteByDocumentId(UUID documentId);

    Optional<DocumentSnapshot> findTopByDocumentIdOrderByLastOperationSequenceDesc(UUID documentId);
}
