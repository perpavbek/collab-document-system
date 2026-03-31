package kz.perpavbek.collab.documentservice.dto.response;

import kz.perpavbek.collab.documentservice.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PermissionResponse {
    private Role role;
}
