package kz.perpavbek.collab.documentservice.mapper;

import kz.perpavbek.collab.documentservice.dto.response.DocumentResponse;
import kz.perpavbek.collab.documentservice.entity.Document;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = DocumentCollaboratorMapper.class)
public interface DocumentMapper {

    @Mapping(target = "collaborators", source = "collaborators")
    DocumentResponse toResponse(Document document);

}
