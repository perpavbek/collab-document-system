package kz.perpavbek.collab.documentservice.mapper;

import kz.perpavbek.collab.documentservice.dto.response.DocumentSessionResponse;
import kz.perpavbek.collab.documentservice.entity.DocumentSession;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = DocumentCollaboratorMapper.class)
public interface DocumentSessionMapper {

    DocumentSessionResponse toResponse(DocumentSession session);

}