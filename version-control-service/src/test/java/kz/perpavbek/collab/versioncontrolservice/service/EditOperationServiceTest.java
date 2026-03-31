package kz.perpavbek.collab.versioncontrolservice.service;

import kz.perpavbek.collab.versioncontrolservice.dto.request.EditOperationRequest;
import kz.perpavbek.collab.versioncontrolservice.entity.EditOperation;
import kz.perpavbek.collab.versioncontrolservice.enums.OperationType;
import kz.perpavbek.collab.versioncontrolservice.mapper.EditOperationMapper;
import kz.perpavbek.collab.versioncontrolservice.repository.EditOperationRepository;
import kz.perpavbek.collab.versioncontrolservice.security.JwtUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class EditOperationServiceTest {

    @Mock
    private EditOperationRepository repository;

    @Mock
    private EditOperationMapper mapper;

    @Mock
    private DocumentContentService contentService;

    @Mock
    private SnapshotService snapshotService;

    @Mock
    private JwtUtils jwtUtils;

    @InjectMocks
    private EditOperationService service;

    @Test
    void shouldSaveOperation() {

        UUID docId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        EditOperationRequest request = new EditOperationRequest();
        request.setType(OperationType.INSERT);
        request.setDocumentId(docId);
        request.setPosition(0);

        EditOperation entity = new EditOperation();

        when(jwtUtils.getCurrentToken()).thenReturn("token");
        when(jwtUtils.getIdFromToken("token")).thenReturn(userId);
        when(contentService.getDocumentLength(docId)).thenReturn(10);
        when(mapper.toEntity(request)).thenReturn(entity);

        when(repository
                .findTopByDocumentIdOrderBySequenceNumberDesc(docId))
                .thenReturn(Optional.empty());

        when(repository.save(entity)).thenReturn(entity);

        EditOperation result = service.saveOperation(request);

        assertNotNull(result);

        verify(snapshotService).createSnapshotIfNeeded(docId);
    }
}