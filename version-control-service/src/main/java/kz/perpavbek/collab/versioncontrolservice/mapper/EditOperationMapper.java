package kz.perpavbek.collab.versioncontrolservice.mapper;

import kz.perpavbek.collab.versioncontrolservice.dto.request.EditOperationRequest;
import kz.perpavbek.collab.versioncontrolservice.dto.response.EditOperationResponse;
import kz.perpavbek.collab.versioncontrolservice.entity.EditOperation;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface EditOperationMapper {

    EditOperation toEntity(EditOperationRequest request);

    EditOperationResponse toResponse(EditOperation entity);

    List<EditOperationResponse> toResponseList(List<EditOperation> entities);
}
