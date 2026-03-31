package kz.perpavbek.collab.versioncontrolservice.service;

import kz.perpavbek.collab.versioncontrolservice.entity.DocumentSnapshot;
import kz.perpavbek.collab.versioncontrolservice.entity.EditOperation;
import kz.perpavbek.collab.versioncontrolservice.repository.DocumentSnapshotRepository;
import kz.perpavbek.collab.versioncontrolservice.repository.EditOperationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DocumentContentService {

    private final DocumentSnapshotRepository snapshotRepository;
    private final EditOperationRepository operationRepository;

    public String buildDocument(UUID documentId) {

        DocumentSnapshot snapshot =
                snapshotRepository
                        .findTopByDocumentIdOrderByLastOperationSequenceDesc(documentId)
                        .orElse(null);

        String content = snapshot != null ? snapshot.getContent() : "";
        long lastSeq = snapshot != null ? snapshot.getLastOperationSequence() : 0;

        List<EditOperation> operations =
                operationRepository
                        .findByDocumentIdAndSequenceNumberGreaterThanOrderBySequenceNumberAsc(
                                documentId,
                                lastSeq
                        );

        StringBuilder builder = new StringBuilder(content);

        for (EditOperation op : operations) {

            switch (op.getType()) {

                case INSERT ->
                        builder.insert(op.getPosition(), op.getContent());

                case DELETE ->
                        builder.delete(
                                op.getPosition(),
                                op.getPosition() + op.getLength()
                        );

                case REPLACE ->
                        builder.replace(
                                op.getPosition(),
                                op.getPosition() + op.getLength(),
                                op.getContent()
                        );
            }
        }

        return builder.toString();
    }

    public int getDocumentLength(UUID documentId) {
        return buildDocument(documentId).length();
    }
}
