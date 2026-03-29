package kz.perpavbek.collab.versioncontrolservice.mapper;

import kz.perpavbek.collab.versioncontrolservice.dto.response.EditOperationResponse;
import kz.perpavbek.collab.versioncontrolservice.entity.EditOperation;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface EditOperationMapper {

    EditOperationResponse toResponse(EditOperation entity);

    List<EditOperationResponse> toResponseList(List<EditOperation> entities);
}
