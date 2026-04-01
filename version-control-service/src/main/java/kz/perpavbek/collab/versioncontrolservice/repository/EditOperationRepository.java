package kz.perpavbek.collab.versioncontrolservice.repository;

import kz.perpavbek.collab.versioncontrolservice.entity.EditOperation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EditOperationRepository extends JpaRepository<EditOperation, UUID> {

    List<EditOperation> findByDocumentIdAndSequenceNumberGreaterThanOrderBySequenceNumberAsc(
            UUID documentId,
            long sequenceNumber
    );

    Optional<EditOperation> findTopByDocumentIdOrderBySequenceNumberDesc(UUID documentId);

    List<EditOperation> findByDocumentIdAndSequenceNumberGreaterThanAndSequenceNumberLessThanEqualOrderBySequenceNumberAsc(UUID documentId, long sequenceNumber, long sequenceNumber2);

    void deleteByDocumentIdAndSequenceNumberGreaterThan(UUID documentId, long sequenceNumber);

    void deleteByDocumentId(UUID documentId);
}
