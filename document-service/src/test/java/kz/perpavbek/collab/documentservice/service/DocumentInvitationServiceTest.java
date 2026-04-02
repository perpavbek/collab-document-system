package kz.perpavbek.collab.documentservice.service;

import kz.perpavbek.collab.documentservice.dto.client.User;
import kz.perpavbek.collab.documentservice.dto.response.DocumentResponse;
import kz.perpavbek.collab.documentservice.entity.Document;
import kz.perpavbek.collab.documentservice.entity.DocumentCollaborator;
import kz.perpavbek.collab.documentservice.entity.DocumentInvitation;
import kz.perpavbek.collab.documentservice.enums.Role;
import kz.perpavbek.collab.documentservice.exception.AccessDeniedException;
import kz.perpavbek.collab.documentservice.mapper.DocumentMapper;
import kz.perpavbek.collab.documentservice.repository.DocumentInvitationRepository;
import kz.perpavbek.collab.documentservice.repository.DocumentRepository;
import kz.perpavbek.collab.documentservice.security.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DocumentInvitationServiceTest {

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private DocumentInvitationRepository invitationRepository;

    @Mock
    private DocumentInvitationEmailService invitationEmailService;

    @Mock
    private UserValidationService userValidationService;

    @Mock
    private DocumentMapper documentMapper;

    @Mock
    private JwtUtils jwtUtils;

    @InjectMocks
    private DocumentInvitationService invitationService;

    private UUID ownerId;
    private UUID invitedUserId;
    private UUID anotherUserId;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ownerId = UUID.randomUUID();
        invitedUserId = UUID.randomUUID();
        anotherUserId = UUID.randomUUID();
    }

    @Test
    void synchronizeInvitations_shouldCreateOnlyMissingInvitations() {
        DocumentCollaborator existingCollaborator = DocumentCollaborator.builder()
                .userId(anotherUserId)
                .role(Role.EDITOR)
                .build();
        Document document = Document.builder()
                .ownerId(ownerId)
                .collaborators(new ArrayList<>(List.of(existingCollaborator)))
                .pendingInvitations(new ArrayList<>())
                .build();
        User invitedUser = User.builder()
                .id(invitedUserId)
                .email("invitee@example.com")
                .name("Invitee")
                .build();

        when(userValidationService.getUsersByIds(List.of(anotherUserId, invitedUserId)))
                .thenReturn(Map.of(anotherUserId, User.builder().id(anotherUserId).email("existing@example.com").build(), invitedUserId, invitedUser));

        List<DocumentInvitation> invitations = invitationService.synchronizeInvitations(
                document,
                List.of(anotherUserId, invitedUserId)
        );

        assertEquals(1, invitations.size());
        assertEquals(invitedUserId, invitations.getFirst().getInvitedUserId());
        assertEquals(1, document.getPendingInvitations().size());
        assertEquals(invitedUserId, document.getPendingInvitations().getFirst().getInvitedUserId());
    }

    @Test
    void acceptInvitation_shouldPromoteInvitationToCollaborator() {
        String token = UUID.randomUUID().toString();
        Document document = Document.builder()
                .id(UUID.randomUUID())
                .ownerId(ownerId)
                .collaborators(new ArrayList<>())
                .pendingInvitations(new ArrayList<>())
                .build();
        DocumentInvitation invitation = DocumentInvitation.builder()
                .id(UUID.randomUUID())
                .document(document)
                .invitedUserId(invitedUserId)
                .invitedEmail("invitee@example.com")
                .role(Role.EDITOR)
                .token(token)
                .createdAt(LocalDateTime.now())
                .build();
        document.getPendingInvitations().add(invitation);

        when(jwtUtils.getCurrentToken()).thenReturn("token");
        when(jwtUtils.getIdFromToken("token")).thenReturn(invitedUserId);
        when(invitationRepository.findByToken(token)).thenReturn(Optional.of(invitation));
        when(documentRepository.save(document)).thenReturn(document);
        when(documentMapper.toResponse(document)).thenReturn(new DocumentResponse());

        DocumentResponse response = invitationService.acceptInvitation(token);

        assertNotNull(response);
        assertEquals(1, document.getCollaborators().size());
        assertTrue(document.getPendingInvitations().isEmpty());
        assertEquals(invitedUserId, document.getCollaborators().getFirst().getUserId());
    }

    @Test
    void getInvitationDetails_shouldRejectForeignInvitation() {
        String token = UUID.randomUUID().toString();
        DocumentInvitation invitation = DocumentInvitation.builder()
                .id(UUID.randomUUID())
                .document(Document.builder().ownerId(ownerId).build())
                .invitedUserId(invitedUserId)
                .token(token)
                .build();

        when(jwtUtils.getCurrentToken()).thenReturn("token");
        when(jwtUtils.getIdFromToken("token")).thenReturn(anotherUserId);
        when(invitationRepository.findByToken(token)).thenReturn(Optional.of(invitation));

        assertThrows(AccessDeniedException.class, () -> invitationService.getInvitationDetails(token));
    }
}
