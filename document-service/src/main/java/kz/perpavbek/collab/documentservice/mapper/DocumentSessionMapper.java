package kz.perpavbek.collab.documentservice.mapper;

import kz.perpavbek.collab.documentservice.dto.response.DocumentSessionResponse;
import kz.perpavbek.collab.documentservice.entity.DocumentSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DocumentSessionMapper {

    private final DocumentCollaboratorMapper documentCollaboratorMapper;

    public DocumentSessionResponse toResponse(DocumentSession session){
        if (session == null) return null;

        return DocumentSessionResponse.builder()
                .id(session.getId())
                .collaborator(documentCollaboratorMapper.toResponse(session.getCollaborator()))
                .connectedAt(session.getConnectedAt())
                .lastActivityAt(session.getLastActivityAt())
                .build();
    }

}