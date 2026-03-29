package kz.perpavbek.collab.documentservice.mapper;

import kz.perpavbek.collab.documentservice.dto.response.DocumentCollaboratorResponse;
import kz.perpavbek.collab.documentservice.entity.DocumentCollaborator;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface DocumentCollaboratorMapper {

    DocumentCollaboratorResponse toResponse(DocumentCollaborator collaborator);

}
