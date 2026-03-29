package kz.perpavbek.collab.documentservice.mapper;

import kz.perpavbek.collab.documentservice.dto.response.DocumentResponse;
import kz.perpavbek.collab.documentservice.entity.Document;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DocumentMapper {

    private final DocumentCollaboratorMapper documentCollaboratorMapper;

    public DocumentResponse toResponse(Document document){
        if (document == null) return null;

        return DocumentResponse.builder()
                .id(document.getId())
                .title(document.getTitle())
                .ownerId(document.getOwnerId())
                .currentVersionId(document.getCurrentVersionId())
                .createdAt(document.getCreatedAt())
                .updatedAt(document.getUpdatedAt())
                .collaborators(documentCollaboratorMapper.toResponse(document.getCollaborators()))
                .build();
    }

}
