package kz.perpavbek.collab.documentservice.mapper;

import kz.perpavbek.collab.documentservice.dto.response.DocumentInvitationResponse;
import kz.perpavbek.collab.documentservice.entity.DocumentInvitation;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface DocumentInvitationMapper {

    DocumentInvitationResponse toResponse(DocumentInvitation invitation);
}
