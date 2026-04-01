package kz.perpavbek.collab.documentservice.service;

import kz.perpavbek.collab.documentservice.client.VersionControlClient;
import kz.perpavbek.collab.documentservice.dto.request.DocumentCreateRequest;
import kz.perpavbek.collab.documentservice.dto.request.DocumentUpdateRequest;
import kz.perpavbek.collab.documentservice.dto.response.DocumentResponse;
import kz.perpavbek.collab.documentservice.entity.Document;
import kz.perpavbek.collab.documentservice.enums.Role;
import kz.perpavbek.collab.documentservice.exception.AccessDeniedException;
import kz.perpavbek.collab.documentservice.exception.NotFoundException;
import kz.perpavbek.collab.documentservice.mapper.DocumentMapper;
import kz.perpavbek.collab.documentservice.repository.DocumentRepository;
import kz.perpavbek.collab.documentservice.security.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.*;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DocumentServiceTest {

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private VersionControlClient versionControlClient;

    @Mock
    private DocumentMapper documentMapper;

    @Mock
    private DocumentAccessService documentAccessService;

    @Mock
    private UserValidationService userValidationService;

    @Mock
    private JwtUtils jwtUtils;

    @InjectMocks
    private DocumentService documentService;

    private UUID ownerId;
    private UUID collaboratorId;
    private UUID documentId;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ownerId = UUID.randomUUID();
        collaboratorId = UUID.randomUUID();
        documentId = UUID.randomUUID();
    }

    @Test
    void createDocument_success() {
        DocumentCreateRequest request = new DocumentCreateRequest();
        request.setTitle("My Doc");
        request.setCollaboratorIds(List.of(collaboratorId));

        when(jwtUtils.getCurrentToken()).thenReturn("token");
        when(jwtUtils.getIdFromToken("token")).thenReturn(ownerId);
        doNothing().when(userValidationService).validateUser(ownerId);
        doNothing().when(userValidationService).validateUsers(List.of(collaboratorId));

        Document doc = Document.builder()
                .id(documentId)
                .title("My Doc")
                .ownerId(ownerId)
                .versionSequenceNumber(1L)
                .collaborators(new ArrayList<>())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        when(documentRepository.save(any())).thenReturn(doc);
        when(documentMapper.toResponse(doc)).thenReturn(new DocumentResponse());

        DocumentResponse response = documentService.createDocument(request);

        assertNotNull(response);
        verify(documentRepository).save(any(Document.class));
        verify(userValidationService).validateUsers(List.of(collaboratorId));
    }

    @Test
    void getDocument_success() {
        Document doc = Document.builder().id(documentId).build();

        when(documentRepository.findById(documentId)).thenReturn(Optional.of(doc));
        doNothing().when(documentAccessService).checkPermission(doc, Role.EDITOR, Role.VIEWER);
        when(documentMapper.toResponse(doc)).thenReturn(new DocumentResponse());

        DocumentResponse response = documentService.getDocument(documentId);

        assertNotNull(response);
        verify(documentAccessService).checkPermission(doc, Role.EDITOR, Role.VIEWER);
    }

    @Test
    void getDocument_accessDenied() {
        Document doc = Document.builder().id(documentId).build();

        when(documentRepository.findById(documentId)).thenReturn(Optional.of(doc));
        doThrow(AccessDeniedException.class)
                .when(documentAccessService).checkPermission(doc, Role.EDITOR, Role.VIEWER);

        assertThrows(AccessDeniedException.class, () -> documentService.getDocument(documentId));
    }

    @Test
    void updateDocumentMeta_changeTitleAndCollaborators() {
        DocumentUpdateRequest request = new DocumentUpdateRequest();
        request.setTitle("New Title");
        request.setCollaboratorIds(List.of(collaboratorId));

        Document doc = Document.builder().id(documentId).collaborators(new ArrayList<>()).build();
        when(documentRepository.findById(documentId)).thenReturn(Optional.of(doc));
        doNothing().when(documentAccessService).checkPermission(doc, Role.EDITOR);
        doNothing().when(userValidationService).validateUsers(request.getCollaboratorIds());

        when(documentRepository.save(doc)).thenReturn(doc);
        when(documentMapper.toResponse(doc)).thenReturn(new DocumentResponse());

        DocumentResponse response = documentService.updateDocumentMeta(documentId, request);

        assertNotNull(response);
        assertEquals("New Title", doc.getTitle());
        assertEquals(1, doc.getCollaborators().size());
        verify(userValidationService).validateUsers(request.getCollaboratorIds());
        verify(documentRepository).save(doc);
    }

    @Test
    void deleteDocument_shouldCallVersionControlAndDeleteDocument() {
        Document document = Document.builder().id(documentId).ownerId(ownerId).build();

        when(documentRepository.findById(documentId)).thenReturn(Optional.of(document));

        documentService.deleteDocument(documentId);

        verify(documentAccessService).checkPermission(document, Role.OWNER);
        verify(versionControlClient).deleteDocumentVersions(documentId);
        verify(documentRepository).delete(document);
    }

    @Test
    void getDocumentsForCurrentUser_returnsPage() {
        when(jwtUtils.getCurrentToken()).thenReturn("token");
        when(jwtUtils.getIdFromToken("token")).thenReturn(ownerId);

        Document doc = Document.builder().id(documentId).build();
        Page<Document> page = new PageImpl<>(List.of(doc));
        when(documentRepository.findByOwnerIdOrCollaborators_UserId(ownerId, ownerId, PageRequest.of(0, 10)))
                .thenReturn(page);
        when(documentMapper.toResponse(doc)).thenReturn(new DocumentResponse());

        Page<DocumentResponse> responsePage = documentService.getDocumentsForCurrentUser(0, 10);

        assertEquals(1, responsePage.getTotalElements());
    }

    @Test
    void getDocumentOrThrow_notFound_shouldThrow() {
        when(documentRepository.findById(documentId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> documentService.getDocumentOrThrow(documentId));
    }
}