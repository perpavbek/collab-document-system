package kz.perpavbek.collab.versioncontrolservice.dto.client;

import kz.perpavbek.collab.versioncontrolservice.enums.CollaboratorRole;
import lombok.Data;

@Data
public class PermissionResponse {
    CollaboratorRole role;
}
