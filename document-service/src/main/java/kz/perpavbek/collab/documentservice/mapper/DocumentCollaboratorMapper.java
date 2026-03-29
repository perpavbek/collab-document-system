package kz.perpavbek.collab.documentservice.mapper;

import kz.perpavbek.collab.documentservice.dto.response.DocumentCollaboratorResponse;
import kz.perpavbek.collab.documentservice.entity.DocumentCollaborator;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class DocumentCollaboratorMapper {

    public DocumentCollaboratorResponse toResponse(DocumentCollaborator collaborator){
        if (collaborator == null) return null;

        return DocumentCollaboratorResponse.builder()
                .id(collaborator.getId())
                .userId(collaborator.getUserId())
                .role(collaborator.getRole())
                .addedAt(collaborator.getAddedAt())
                .build();
    }

    public List<DocumentCollaboratorResponse> toResponse(List<DocumentCollaborator> collaborators) {
        if (collaborators == null) return null;

        return collaborators.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
}
