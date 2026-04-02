package kz.perpavbek.collab.documentservice.dto.response;

import kz.perpavbek.collab.documentservice.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentInvitationDetailsResponse {

    private UUID documentId;
    private String documentTitle;
    private UUID ownerId;
    private UUID invitedUserId;
    private String invitedEmail;
    private Role role;
}
