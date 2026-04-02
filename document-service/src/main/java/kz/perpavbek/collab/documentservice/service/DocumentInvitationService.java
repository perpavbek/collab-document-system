package kz.perpavbek.collab.documentservice.service;

import jakarta.transaction.Transactional;
import kz.perpavbek.collab.documentservice.dto.client.User;
import kz.perpavbek.collab.documentservice.dto.response.DocumentInvitationDetailsResponse;
import kz.perpavbek.collab.documentservice.dto.response.DocumentResponse;
import kz.perpavbek.collab.documentservice.entity.Document;
import kz.perpavbek.collab.documentservice.entity.DocumentCollaborator;
import kz.perpavbek.collab.documentservice.entity.DocumentInvitation;
import kz.perpavbek.collab.documentservice.enums.Role;
import kz.perpavbek.collab.documentservice.exception.AccessDeniedException;
import kz.perpavbek.collab.documentservice.exception.NotFoundException;
import kz.perpavbek.collab.documentservice.mapper.DocumentMapper;
import kz.perpavbek.collab.documentservice.repository.DocumentInvitationRepository;
import kz.perpavbek.collab.documentservice.repository.DocumentRepository;
import kz.perpavbek.collab.documentservice.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DocumentInvitationService {

    private final DocumentRepository documentRepository;
    private final DocumentInvitationRepository invitationRepository;
    private final DocumentInvitationEmailService invitationEmailService;
    private final UserValidationService userValidationService;
    private final DocumentMapper documentMapper;
    private final JwtUtils jwtUtils;

    public List<DocumentInvitation> synchronizeInvitations(Document document, List<UUID> requestedCollaboratorIds) {

        List<UUID> normalizedIds = normalizeRequestedCollaborators(document.getOwnerId(), requestedCollaboratorIds);
        Map<UUID, User> desiredUsers = userValidationService.getUsersByIds(normalizedIds);
        Set<UUID> desiredUserIds = desiredUsers.keySet();

        document.getCollaborators().removeIf(collaborator -> !desiredUserIds.contains(collaborator.getUserId()));
        document.getPendingInvitations().removeIf(invitation -> !desiredUserIds.contains(invitation.getInvitedUserId()));

        Set<UUID> collaboratorIds = document.getCollaborators().stream()
                .map(DocumentCollaborator::getUserId)
                .collect(Collectors.toSet());
        Set<UUID> pendingInvitationIds = document.getPendingInvitations().stream()
                .map(DocumentInvitation::getInvitedUserId)
                .collect(Collectors.toSet());

        List<DocumentInvitation> newInvitations = normalizedIds.stream()
                .filter(userId -> !collaboratorIds.contains(userId) && !pendingInvitationIds.contains(userId))
                .map(userId -> createInvitation(document, desiredUsers.get(userId)))
                .toList();

        document.getPendingInvitations().addAll(newInvitations);

        return newInvitations;
    }

    public void sendInvitationEmails(Document document, List<DocumentInvitation> invitations, User inviter) {

        invitations.forEach(invitation -> invitationEmailService.sendInvitationEmail(document, invitation, inviter));
    }

    @Transactional
    public DocumentInvitationDetailsResponse getInvitationDetails(String token) {

        DocumentInvitation invitation = getInvitationForCurrentUser(token);
        Document document = invitation.getDocument();

        return DocumentInvitationDetailsResponse.builder()
                .documentId(document.getId())
                .documentTitle(document.getTitle())
                .ownerId(document.getOwnerId())
                .invitedUserId(invitation.getInvitedUserId())
                .invitedEmail(invitation.getInvitedEmail())
                .role(invitation.getRole())
                .build();
    }

    @Transactional
    public DocumentResponse acceptInvitation(String token) {

        DocumentInvitation invitation = getInvitationForCurrentUser(token);
        Document document = invitation.getDocument();

        boolean alreadyCollaborator = document.getCollaborators().stream()
                .anyMatch(collaborator -> collaborator.getUserId().equals(invitation.getInvitedUserId()));

        if (!alreadyCollaborator) {
            document.getCollaborators().add(
                    DocumentCollaborator.builder()
                            .document(document)
                            .userId(invitation.getInvitedUserId())
                            .role(invitation.getRole())
                            .addedAt(LocalDateTime.now())
                            .build()
            );
        }

        document.getPendingInvitations().removeIf(entry -> entry.getId().equals(invitation.getId()));
        document.setUpdatedAt(LocalDateTime.now());

        return documentMapper.toResponse(documentRepository.save(document));
    }

    private DocumentInvitation getInvitationForCurrentUser(String token) {

        UUID currentUserId = jwtUtils.getIdFromToken(jwtUtils.getCurrentToken());
        DocumentInvitation invitation = invitationRepository.findByToken(token)
                .orElseThrow(() -> new NotFoundException("Invitation not found"));

        if (!invitation.getInvitedUserId().equals(currentUserId)) {
            throw new AccessDeniedException("This invitation is not for the current user");
        }

        return invitation;
    }

    private List<UUID> normalizeRequestedCollaborators(UUID ownerId, List<UUID> requestedCollaboratorIds) {

        if (requestedCollaboratorIds == null) {
            return List.of();
        }

        return requestedCollaboratorIds.stream()
                .filter(Objects::nonNull)
                .filter(userId -> !ownerId.equals(userId))
                .distinct()
                .toList();
    }

    private DocumentInvitation createInvitation(Document document, User user) {
        return DocumentInvitation.builder()
                .document(document)
                .invitedUserId(user.getId())
                .invitedEmail(user.getEmail())
                .role(Role.EDITOR)
                .token(UUID.randomUUID().toString())
                .createdAt(LocalDateTime.now())
                .build();
    }
}
