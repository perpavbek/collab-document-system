package kz.perpavbek.collab.documentservice.mapper;

import kz.perpavbek.collab.documentservice.dto.response.DocumentResponse;
import kz.perpavbek.collab.documentservice.entity.Document;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {DocumentCollaboratorMapper.class, DocumentInvitationMapper.class})
public interface DocumentMapper {

    @Mapping(target = "collaborators", source = "collaborators")
    @Mapping(target = "pendingInvitations", source = "pendingInvitations")
    DocumentResponse toResponse(Document document);

}
